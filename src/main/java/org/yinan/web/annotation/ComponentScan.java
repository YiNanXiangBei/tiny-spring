package org.yinan.web.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yinan
 * @date 19-8-24
 * 为路由服务，用在启动类上，用于获取需要获取的包扫描路径
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String value();
}
