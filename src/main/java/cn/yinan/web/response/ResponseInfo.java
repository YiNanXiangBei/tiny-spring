package cn.yinan.web.response;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinan
 * @date 19-6-11
 */
public class ResponseInfo {
    public static final int CODE_OK = 20000;

    public static final int CODE_INVALID_PARAMS = 40000;

    public static final int CODE_UNAUTHORIZED = 40100;

    public static final int CODE_API_NOT_FOUND = 40400;

    public static final int CODE_SYSTEM_ERROR = 50000;

    private transient Map<CharSequence, Object> headers = new ConcurrentHashMap<>();

    private transient Map<String, String> cookies = new ConcurrentHashMap<>();

    private int code;

    private String message;

    private Object data;

    private ResponseInfo(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private ResponseInfo(int code, String message) {
        this(code, message, null);
    }

    private ResponseInfo(Object data) {
        this(ResponseInfo.CODE_OK, "success", data);
    }

    public static ResponseInfo build(int code, String message, Object data) {
        return new ResponseInfo(code, message, data);
    }

    public static ResponseInfo build(int code, String message) {
        return new ResponseInfo(code, message);
    }

    public static ResponseInfo build(Object data) {
        return new ResponseInfo(data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ResponseInfo headers(CharSequence name, Object value) {
        headers.put(name, value);
        return this;
    }

    public void setHeaders(FullHttpResponse response) {
        headers.forEach((key, value) -> response.headers().set(key, value));
    }

    public ResponseInfo cookies(String key, String value) {
        cookies.put(key, value);
        return this;
    }

    public void setCookies(FullHttpResponse response) {
        cookies.forEach((key, value) -> {
            response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(key, value));
        });
    }


}
