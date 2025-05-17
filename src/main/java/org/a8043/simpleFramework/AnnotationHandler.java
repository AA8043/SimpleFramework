package org.a8043.simpleFramework;

import java.lang.annotation.Annotation;

public abstract class AnnotationHandler {
    public Class<? extends Annotation> annotation = null;

    public abstract void handle(Class<?> clazz);

    public void secondHandle() {
    }
}
