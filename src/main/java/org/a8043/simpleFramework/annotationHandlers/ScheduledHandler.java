package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

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
    public void thirdHandle() {
        classList.forEach(clazz -> {
            Object object = instance.getBeanByClass(clazz);
            Scheduled scheduled = clazz.getAnnotation(Scheduled.class);
            int time = scheduled.value();
            Timer timer = new Timer();
            timer.schedule((TimerTask) object, 0, time);
        });
    }
}
