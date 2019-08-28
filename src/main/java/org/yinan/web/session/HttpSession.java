package org.yinan.web.session;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinan
 * @date 19-6-17
 * 用户登录session
 */
public class HttpSession {

    private String sessionId;

    private Map<String, Object> attributes = new HashMap<>();

    HttpSession(String sessionId) {
        this.sessionId = sessionId;
        setAttributes("expire", System.currentTimeMillis());
    }

    public Object getAttributes(String name) {
        return attributes.get(name);
    }

    public void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }

    public String getSessionId() {
        return sessionId;
    }

}
