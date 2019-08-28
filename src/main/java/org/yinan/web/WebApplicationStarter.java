package org.yinan.web;

import org.yinan.web.annotation.ComponentScan;
import org.yinan.web.routes.BaseRouteConfig;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author yinan
 * @date 19-8-24
 * web 服务启动类
 */
public class WebApplicationStarter {

    public static void start() {
        StackTraceElement elements[] =  Thread.currentThread().getStackTrace();
        if (elements.length == 0) {
            throw new RuntimeException("can not get stack trace!");
        }
        String declareClass = elements[elements.length - 1].getClassName();
        try {
            Class clazz = Class.forName(declareClass);
            ComponentScan componentScan = (ComponentScan)clazz.getAnnotation(ComponentScan.class);
            if (componentScan == null) {
                throw new RuntimeException("can not get [ComponentScan.class] from init class");
            }
            String packageName = componentScan.value();
            if ("".equals(packageName) || packageName.isEmpty()) {
                throw new RuntimeException("can not find correct value from [ComponentScan.class]");
            }
            new BaseRouteConfig().init(new ArrayList<>(Collections.singletonList(packageName)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
