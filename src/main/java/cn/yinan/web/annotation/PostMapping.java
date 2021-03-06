package cn.yinan.web.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yinan
 * @date 19-6-12
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    String value() default "/";
}
