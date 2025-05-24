package org.a8043.simpleFramework;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.annotations.RequestMapping;
import org.a8043.simpleUtil.util.Config;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class EmbeddedTomcat {
    private final Config tomcatConfig = instance.getConfig().getMap("tomcat");
    private final Tomcat tomcat = new Tomcat();
    private final Context mainContext = tomcat.addContext("/",
        new File("./").getAbsolutePath());

    public EmbeddedTomcat() {
        tomcat.setPort(tomcatConfig.getInt("port"));
    }

    public void addWebApp(Class<?> clazz) {
        String clazzName = clazz.getName();
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        Object object = instance.getBeanByClass(clazz);
        Wrapper wrapper = Tomcat.addServlet(mainContext, clazzName, (HttpServlet) object);
        wrapper.setAsyncSupported(requestMapping.asyncSupport());
        mainContext.addServletMapping(requestMapping.value(), clazzName);
    }

    public void start() {
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            log.error("启动Tomcat失败", e);
        }
    }

    public void stop() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            log.error("停止Tomcat失败", e);
        }
    }
}
