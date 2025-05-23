package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.Bean;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class BeanHandler extends AnnotationHandler {
    public BeanHandler() {
        annotation = Bean.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        Map<String, Object> beanList = instance.getBeanMap();

        Bean bean = clazz.getAnnotation(Bean.class);
        String name = bean.value();

        if (!beanList.containsKey(name)) {
            Object object;
            try {
                object = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("创建Bean失败", e);
                return;
            }

            beanList.put(name, object);
        }
    }
}
