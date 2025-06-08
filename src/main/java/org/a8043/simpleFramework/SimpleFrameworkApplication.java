package org.a8043.simpleFramework;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.annotationHandlers.*;
import org.a8043.simpleFramework.annotationHandlers.minecraft.BukkitCommandHandler;
import org.a8043.simpleFramework.annotationHandlers.minecraft.BukkitListenerHandler;
import org.a8043.simpleFramework.annotations.Application;
import org.a8043.simpleFramework.annotations.Bean;
import org.a8043.simpleUtil.util.Config;
import org.a8043.simpleUtil.util.Timing;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final Map<String, Object> beanMap = new HashMap<>();
    @Setter
    private Plugin bukkitPlugin;

    public static void run(Class<?> clazz, String[] args) {
        SimpleFrameworkApplication simpleFrameworkApplication = new SimpleFrameworkApplication(clazz, args);
        simpleFrameworkApplication.run();
    }

    public static void stop() {
        instance.exit();
    }

    public SimpleFrameworkApplication(Class<?> clazz, String[] args) {
        instance = this;
        this.clazz = clazz;
        this.args = args;

        handlerList.add(new BeanHandler());
        handlerList.add(new EnableAutowiredHandler());
        handlerList.add(new ConfigFileHandler());
        handlerList.add(new RequestMappingHandler());
        handlerList.add(new ScheduledHandler());
        handlerList.add(new BukkitCommandHandler());
        handlerList.add(new BukkitListenerHandler());
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

        log.info("创建应用实例: {}", clazz.getName());
        try {
            appObject = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("创建应用实例失败: {}", clazz.getName(), e);
        }

        log.info("扫描类路径");
        classList = ClassUtil.scanPackage(clazz.getPackageName()).stream().toList();

        log.info("第一次处理注解");
        handleAnnotation();

        log.info("第二次处理注解");
        secondHandleAnnotation();

        if (tomcat != null) {
            log.info("启动Tomcat(端口: {})", config.getMap("tomcat").getInt("port"));
            tomcat.start();
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

        log.info("第三次注解处理");
        thirdHandleAnnotation();

        log.info("启动成功, 耗时: {}ms", timing.getTime());
    }

    private void handleAnnotation() {
        classList.forEach(clazz -> handlerList.forEach(handler -> {
            if (handler.annotation != null && clazz.isAnnotationPresent(handler.annotation)) {
                handler.handle(clazz);
            }
        }));
    }

    private void secondHandleAnnotation() {
        handlerList.forEach(AnnotationHandler::secondHandle);
    }

    private void thirdHandleAnnotation() {
        handlerList.forEach(AnnotationHandler::thirdHandle);
    }

    public Object getBeanByClass(Class<?> clazz) {
        Bean beanAnnotation = clazz.getAnnotation(Bean.class);
        if (beanAnnotation == null) {
            log.error("没有Bean注解: {}", clazz.getName());
            return null;
        }
        return beanMap.get(beanAnnotation.value());
    }

    public void exit() {
        log.info("关闭SimpleFrameworkApplication");

        if (instance.tomcat != null) {
            log.info("关闭Tomcat");
            instance.tomcat.stop();
        }

        log.info("关闭应用");
        try {
            Method exit = clazz.getMethod("exit");
            exit.invoke(appObject);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("关闭应用失败: {}", clazz.getName(), e);
        }

        System.exit(0);
    }
}
