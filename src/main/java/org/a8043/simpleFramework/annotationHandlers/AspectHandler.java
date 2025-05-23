package org.a8043.simpleFramework.annotationHandlers;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleFramework.AnnotationHandler;
import org.a8043.simpleFramework.annotations.Aspect;
import org.a8043.simpleFramework.annotations.Bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.a8043.simpleFramework.SimpleFrameworkApplication.instance;

@Slf4j
public class AspectHandler extends AnnotationHandler {
    public AspectHandler() {
        annotation = Aspect.class;
    }

    @Override
    public void frontHandle(Class<?> clazz) {
    }

    @Override
    public void handle(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Bean.class)) {
            log.error("没有Bean注解: {}", clazz.getName());
            return;
        }

        List<Method> methodList = Arrays.stream(clazz.getMethods()).toList();

        Bean bean = clazz.getAnnotation(Bean.class);
        String beanName = bean.value();
        Object object = instance.getBeanMap().get(beanName);

        List<Method> afterMethodList = new ArrayList<>();
        List<Method> beforeMethodList = new ArrayList<>();
        methodList.forEach(method -> {
            if (method.isAnnotationPresent(Aspect.After.class)) {
                afterMethodList.add(method);
            }
            if (method.isAnnotationPresent(Aspect.Before.class)) {
                beforeMethodList.add(method);
            }
        });

        Object proxy = createProxy(object, afterMethodList, beforeMethodList, methodList);
        instance.getBeanMap().put(beanName, proxy);
    }

    public static Object createProxy(Object target,
                                     List<Method> afterMethodList,
                                     List<Method> beforeMethodList,
                                     List<Method> methodList) {
        return Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxy, method0, args) -> {
                beforeMethodList.forEach(method -> {
                    Aspect.Before before = method.getAnnotation(Aspect.Before.class);
                    Aspect.Before.BeforeType type = before.value();
                    String targetMethodStr = before.method();
                    List<Method> targetMethodList = getMethodListByStr(targetMethodStr, methodList);
                    targetMethodList.forEach(targetMethod -> {
                        if (type == Aspect.Before.BeforeType.INVOKE) {
                            try {
                                method.invoke(target, args);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                log.error("执行方法失败", e);
                            }
                        }
                    });
                });

                Object result = null;
                try {
                    result = method0.invoke(target, args);
                } catch (InvocationTargetException e) {
                    Throwable realException = e.getCause();
                    afterMethodList.forEach(afterMethod -> {
                        try {
                            afterMethod.invoke(target, realException);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            log.error("执行方法失败", ex);
                        }
                    });
                }

                Object finalResult = result;
                afterMethodList.forEach(method -> {
                    Aspect.After after = method.getAnnotation(Aspect.After.class);
                    Aspect.After.AfterType type = after.value();
                    String targetMethodStr = after.method();
                    List<Method> targetMethodList = getMethodListByStr(targetMethodStr, methodList);
                    targetMethodList.forEach(targetMethod -> {
                        if (type == Aspect.After.AfterType.RUN) {
                            try {
                                method.invoke(target, finalResult);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                log.error("执行方法失败", e);
                            }
                        }
                    });
                });

                return result;
            }
        );
    }

    private static List<Method> getMethodListByStr(String str, List<Method> methodList) {
        List<Method> returnMethodList = new ArrayList<>();
        if (str.equals("*")) {
            returnMethodList.addAll(methodList);
        } else if (str.startsWith("{") && str.endsWith("}")) {
            String methodListStr = str.substring(1, str.length() - 1);
            String[] methodStrList = methodListStr.split(",");
            for (String methodStr : methodStrList) {
                Method[] targetMethods = getMethodsByName(methodList, methodStr);
                returnMethodList.addAll(Arrays.stream(targetMethods).toList());
            }
        } else {
            Method[] targetMethods = getMethodsByName(methodList, str);
            returnMethodList.addAll(Arrays.stream(targetMethods).toList());
        }
        return returnMethodList;
    }

    private static Method[] getMethodsByName(List<Method> methodList, String name) {
        List<Method> returnMethodList = new ArrayList<>();
        methodList.forEach(method -> {
            if (method.getName().equals(name)) {
                returnMethodList.add(method);
            }
        });
        return returnMethodList.toArray(new Method[0]);
    }
}
