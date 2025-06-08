package org.a8043.simpleFramework.annotationHandlers.minecraft;

import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.minecraft.BukkitCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

public class BukkitCommandHandler extends AnnotationHandler {
    public BukkitCommandHandler() {
        annotation = BukkitCommand.class;
    }

    @Override
    public void handle(Class<?> clazz) {
        BukkitCommand bukkitCommand = clazz.getAnnotation(BukkitCommand.class);
        PluginCommand pluginCommand = Bukkit.getPluginCommand(bukkitCommand.value());
        Object object = instance.getBeanByClass(clazz);
        if (pluginCommand != null) {
            pluginCommand.setExecutor((CommandExecutor) object);
            pluginCommand.setTabCompleter((TabCompleter) object);
        }
    }
}
