package org.yinan.web.middleware;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.web.exception.ContextException;
import org.yinan.web.response.ResponseInfo;
import org.yinan.web.session.HttpSession;
import org.yinan.web.session.SessionManager;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinan
 * @date 19-6-16
 */
public class BaseMiddleware implements IRequestMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMiddleware.class);

    private static final String CLIENT_COOKIE_NAME = "JSESSIONID";

    private static final String LOGIN = "/login";

    private static Pattern PATTERN = Pattern.compile("^.*?\\.(jpg|jpeg|html|gif|js|css|json|ico|svg|woff2|woff|ttf)$");

    @Override
    public void preRequest(FullHttpRequest request) throws Exception {
        URI uri = new URI(request.uri());
        String path = uri.getPath();
        //如果是login且是post方法直接跳过
        if (LOGIN.equals(path) && HttpMethod.POST == request.method()) {
            return;
        }

        Matcher matcher = PATTERN.matcher(path);
        //有后缀名的文件，直接去查找该文件，拦截器不过滤
        if (matcher.matches()) {
            return;
        }

        String cookieStr = request.headers().get(HttpHeaderNames.COOKIE);
        if (!StringUtil.isNullOrEmpty(cookieStr)) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            Map<String, String> cookieMap = new HashMap<>();
            cookies.forEach(cookie -> cookieMap.put(cookie.name(), cookie.value()));
            if (cookieMap.containsKey(CLIENT_COOKIE_NAME)){
                String sessionId = cookieMap.get(CLIENT_COOKIE_NAME);
                //如果包含这个sessionId
                if (SessionManager.getInstance().containsSession(sessionId)) {
                    if (SessionManager.getInstance().hasExpired(sessionId)) {
                        //权限验证通过，更新session过期时间
                        HttpSession session = SessionManager.getInstance().getSession(sessionId);
                        SessionManager.getInstance().updateSession(session);
                        LOGGER.info("validate session succeed ");
                        return;
                    }
                    //session过期
                    LOGGER.info("session time out ");
                    SessionManager.getInstance().removeSession(cookieMap.get(CLIENT_COOKIE_NAME));
                    throw new ContextException(ResponseInfo.CODE_UNAUTHORIZED, "no authority");
                } else {
                    LOGGER.info("validate session failed ");
                    throw new ContextException(ResponseInfo.CODE_UNAUTHORIZED, "no authority");
                }
            } else { //没有JSESSIONID字段，验证不通过
                LOGGER.info("validate session failed ");
                throw new ContextException(ResponseInfo.CODE_UNAUTHORIZED, "no authority");
            }
        } else {
            throw new ContextException(ResponseInfo.CODE_UNAUTHORIZED, "no authority");
        }

    }

    @Override
    public void afterRequest(FullHttpRequest request) throws Exception {

    }

}
