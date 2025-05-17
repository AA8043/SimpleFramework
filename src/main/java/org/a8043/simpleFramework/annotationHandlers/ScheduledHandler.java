package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.Scheduled;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class ScheduledHandler extends AnnotationHandler {
    private final List<Class<?>> classList = new ArrayList<>();

    public ScheduledHandler() {
        annotation = Scheduled.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        classList.add(clazz);
    }

    @Override
    public void secondHandle() {
        classList.forEach(clazz -> {
            Object object;
            try {
                object = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("实例化类失败", e);
                return;
            }
            Scheduled scheduled = clazz.getAnnotation(Scheduled.class);
            int time = scheduled.value();
            Timer timer = new Timer();
            timer.schedule((TimerTask) object, 0, time);
        });
    }
}
