package cn.yinan.web.response;

import java.lang.reflect.Method;

/**
 * @author yinan
 * @date 19-6-13
 */
public class ObjectMethod {
    private Class clazz;

    private Method method;

    public ObjectMethod(Class clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
