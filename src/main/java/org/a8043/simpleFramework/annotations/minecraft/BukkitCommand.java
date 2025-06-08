package org.a8043.simpleFramework.annotations.minecraft;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BukkitCommand {
    String value();
}
