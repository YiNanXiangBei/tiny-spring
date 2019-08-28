package org.yinan.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinan
 * @date 19-6-17
 */
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private static final Map<String, HttpSession> SESSION_MAP = new ConcurrentHashMap<>();


    private static SessionManager instance = new SessionManager();

    private SessionManager() {

    }

    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * 注册session，返回新注册的session对象
     * @return
     */
    public String addSession() {
        String sessionId = getSessionId();
        HttpSession session = new HttpSession(sessionId);
        updateSession(session);
        SESSION_MAP.put(sessionId, session);
        return sessionId;
    }

    /**
     * 查看session是否存在
     * @param sessionId
     * @return
     */
    public boolean containsSession(String sessionId) {
        return SESSION_MAP.containsKey(sessionId);
    }

    /**
     * 获取session
     * @param sessionId
     * @return
     */
    public HttpSession getSession(String sessionId) {
        return SESSION_MAP.get(sessionId);
    }

    /**
     * 每次请求后将时间戳向后延续20分钟
     * @param session
     */
    public void updateSession(HttpSession session) {
        long timestamp = (long) session.getAttributes("expire");
        long expire = 20 * 60 * 1000;
        timestamp += expire;
        session.setAttributes("expire", timestamp);
    }

    /**
     * 自定义时间间隔
     * @param session
     * @param expire
     */
    public void updateSession(HttpSession session, long expire) {
        long timestamp = (long) session.getAttributes("expire");
        timestamp += expire;
        session.setAttributes("expire", timestamp);
    }


    public boolean hasExpired(String sessionId) {
        HttpSession session = getSession(sessionId);
        long nowTime = System.currentTimeMillis();
        return nowTime < (long)session.getAttributes("expire");
    }

    public boolean removeSession(String sessionId) {
        if (containsSession(sessionId)) {
            SESSION_MAP.remove(sessionId);
            LOGGER.info("remove session success");
            return true;
        }
        return false;
    }

    private String getSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public boolean validateSession(String sessionId) {
        if (containsSession(sessionId)) {
            if (hasExpired(sessionId)) {
                HttpSession session = getSession(sessionId);
                updateSession(session);
                LOGGER.info("validate session succeed ");
                return true;
            }
        }
        return false;
    }

}
