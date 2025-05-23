package org.a8043.test;

import org.a8043.simpleFramework.annotations.Aspect;
import org.a8043.simpleFramework.annotations.Bean;

@Bean("testBean")
@Aspect
public class TestBean {
    public void aaa() {
        System.out.println("Hello World!");
    }

    @Aspect.Before(Aspect.Before.BeforeType.INVOKE)
    public void bi(Object[] args) {
        System.out.println("Hello World!!");
    }
}
