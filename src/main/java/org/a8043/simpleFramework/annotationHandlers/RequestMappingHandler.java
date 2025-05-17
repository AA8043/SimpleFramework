package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.EmbeddedTomcat;
import org.a8043.simpleFramework.annotations.RequestMapping;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class RequestMappingHandler extends AnnotationHandler {
    public RequestMappingHandler() {
        annotation = RequestMapping.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        if (!instance.getConfig().getMap("tomcat").getBoolean("enable")) {
            log.warn("{}: Tomcat没有启用", clazz.getName());
        }
        EmbeddedTomcat tomcat = instance.getTomcat();
        if (tomcat != null) {
            tomcat.addWebApp(clazz);
        }
    }
}
