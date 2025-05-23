package org.a8043.simpleFramework.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aspect {
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface After {
        AfterType value();

        String method() default "*";

        enum AfterType {
            RUN,
            THROW
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Before {
        BeforeType value();
        String method() default "*";

        enum BeforeType {
            INVOKE,
            RETURN
        }
    }
}
