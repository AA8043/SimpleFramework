package org.a8043.test;

import org.a8043.simpleFramework.annotations.Application;
import org.a8043.simpleFramework.annotations.Bean;

@Application
@Bean("app")
public class App {
    public void start(String[] args) {
        System.out.println("Hello World!!!");
    }
}
