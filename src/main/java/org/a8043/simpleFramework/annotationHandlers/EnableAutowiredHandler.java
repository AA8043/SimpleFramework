package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.Autowired;
import org.a8043.simpleFramework.annotations.EnableAutowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class EnableAutowiredHandler extends AnnotationHandler {
    private final List<Class<?>> classList = new ArrayList<>();

    public EnableAutowiredHandler() {
        annotation = EnableAutowired.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        classList.add(clazz);
    }

    @Override
    public void secondHandle() {
        Map<String, Object> beanList = instance.getBeanMap();
        classList.forEach(clazz -> Arrays.stream(clazz.getDeclaredFields()).toList().forEach(field -> {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (autowired != null) {
                Object bean = beanList.get(field.getName());
                if (bean != null) {
                    field.setAccessible(true);
                    try {
                        field.set(instance.getBeanByClass(clazz), bean);
                    } catch (IllegalAccessException e) {
                        log.error("注入失败: {}", field.getName(), e);
                    }
                } else {
                    log.error("找不到Bean: {}", field.getName());
                }
            }
        }));
    }
}
