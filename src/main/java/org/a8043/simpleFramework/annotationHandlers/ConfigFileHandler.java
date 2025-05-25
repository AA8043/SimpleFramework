package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.ConfigFile;

import java.lang.reflect.Field;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class ConfigFileHandler extends AnnotationHandler {
    public ConfigFileHandler() {
        annotation = ConfigFile.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        ConfigFile configFile = clazz.getAnnotation(ConfigFile.class);
        instance.getConfigFile().getMap(configFile.value()).configMap().forEach((k, v) -> {
            try {
                Field field = clazz.getDeclaredField(k);
                field.setAccessible(true);
                field.set(instance.getBeanByClass(clazz), v);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("配置文件注入失败", e);
            }
        });
    }
}
