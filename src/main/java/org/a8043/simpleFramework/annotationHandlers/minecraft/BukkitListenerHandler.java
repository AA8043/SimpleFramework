package org.a8043.simpleFramework.annotationHandlers.minecraft;

import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.minecraft.BukkitListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

public class BukkitListenerHandler extends AnnotationHandler {
    public BukkitListenerHandler() {
        annotation = BukkitListener.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        Object object = instance.getBeanByClass(clazz);
        Bukkit.getPluginManager().registerEvents((Listener) object, instance.getBukkitPlugin());
    }
}
