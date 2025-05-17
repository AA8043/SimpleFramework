package org.a8043.simpleFramework;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.annotationHandlers.ConfigFileHandler;
import org.a8043.simpleFramework.annotationHandlers.RequestMappingHandler;
import org.a8043.simpleFramework.annotationHandlers.ScheduledHandler;
import org.a8043.simpleFramework.annotations.Application;
import org.a8043.simpleUtil.util.Config;
import org.a8043.simpleUtil.util.Timing;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class SimpleFrameworkApplication {
    public static SimpleFrameworkApplication instance;
    private final List<AnnotationHandler> handlerList = new ArrayList<>();
    private final Class<?> clazz;
    private Object appObject;
    private final String[] args;
    private List<Class<?>> classList;
    private Config config;
    private Config configFile;
    private EmbeddedTomcat tomcat;

    public static void run(Class<?> clazz, String[] args) {
        SimpleFrameworkApplication simpleFrameworkApplication = new SimpleFrameworkApplication(clazz, args);
        instance = simpleFrameworkApplication;
        simpleFrameworkApplication.run();
    }

    public static void stop() {
        instance.exit();
    }

    private SimpleFrameworkApplication(Class<?> clazz, String[] args) {
        this.clazz = clazz;
        this.args = args;

        handlerList.add(new RequestMappingHandler());
        handlerList.add(new ConfigFileHandler());
        handlerList.add(new ScheduledHandler());
    }

    public void run() {
        log.info("启动SimpleFrameworkApplication");
        Timing timing = new Timing();

        log.info("加载Application配置");
        URL configUrl = clazz.getResource("/application.yaml");
        if (configUrl == null) {
            log.error("找不到Application配置");
            return;
        }
        Map<String, Object> configMap = new Yaml().load(FileUtil.readString(configUrl, StandardCharsets.UTF_8));
        config = new Config(configMap);

        if (config.getMap("configFile").getBoolean("enable")) {
            Config configFileConfig = config.getMap("configFile");
            log.info("加载配置文件");
            File configFileFile = new File(configFileConfig.getString("path"));
            if (!configFileFile.exists()) {
                try {
                    boolean newFile = configFileFile.createNewFile();
                    if (!newFile) {
                        throw new IOException("创建配置文件失败");
                    }
                } catch (IOException e) {
                    log.error("创建配置文件失败", e);
                }
            }
            configFile = new Config(new Yaml().load(FileUtil.readString(configFileFile, StandardCharsets.UTF_8)));
        }

        if (config.getMap("tomcat").getBoolean("enable")) {
            log.info("初始化Tomcat");
            tomcat = new EmbeddedTomcat();
        }

        log.info("扫描类路径");
        classList = ClassUtil.scanPackage(clazz.getPackageName()).stream().toList();

        log.info("处理注解");
        handleAnnotation();

        if (tomcat != null) {
            log.info("启动Tomcat");
            tomcat.start();
        }

        log.info("创建应用实例: {}", clazz.getName());
        try {
            appObject = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("创建应用实例失败: {}", clazz.getName(), e);
        }

        log.info("启动应用: {}", clazz.getName());
        Application application = clazz.getAnnotation(Application.class);
        if (application == null) {
            log.error("找不到Application注解");
            return;
        }
        try {
            Method start = clazz.getMethod("start", String[].class);
            start.invoke(appObject, (Object) args);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("启动应用失败: {}", clazz.getName(), e);
        }

        log.info("启动防自动退出线程");
        new Thread(() -> {
            while (true) {
                ThreadUtil.sleep(1000);
            }
        }, "NoAutoExitThread").start();

        log.info("二次注解处理");
        secondHandleAnnotation();

        log.info("启动成功, 耗时: {}ms", timing.getTime());
    }

    private void handleAnnotation() {
        classList.forEach(clazz -> handlerList.forEach(handler -> {
            if (handler.annotation != null && clazz.isAnnotationPresent(handler.annotation)) {
                handler.handle(clazz);
            }
        }));

//        classList.forEach(clazz -> {
//            if (clazz.isAnnotationPresent(Application.class)) {
//                appClassList.add(clazz);
//            }
//        });
//        if (appClassList.isEmpty()) {
//            log.error("找不到Application");
//            return;
//        }
//        appClassList.forEach(clazz -> {
//            log.info("启动应用: {}", clazz.getName());
//            try {
//                Method method = clazz.getMethod("start", String[].class);
//                method.invoke(clazz.getDeclaredConstructor().newInstance(), (Object) args);
//            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
//                     InvocationTargetException e) {
//                log.error("启动应用失败: {}", clazz.getName(), e);
//            }
//        });
    }

    private void secondHandleAnnotation() {
        handlerList.forEach(AnnotationHandler::secondHandle);
    }

    public void exit() {
        log.info("关闭SimpleFrameworkApplication");

        if (instance.tomcat != null) {
            log.info("关闭Tomcat");
            instance.tomcat.stop();
        }

        System.exit(0);
    }
}
