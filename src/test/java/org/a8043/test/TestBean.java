package org.a8043.test;

import org.a8043.simpleFramework.annotations.Bean;

@Bean("testBean")
public class TestBean {
    public void aaa() {
        System.out.println("Hello World!");
    }
}
