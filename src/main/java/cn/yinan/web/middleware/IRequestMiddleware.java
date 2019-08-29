package cn.yinan.web.middleware;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 拦截器
 * @author yinan
 * @date 19-6-11
 */
public interface IRequestMiddleware {

    /**
     * 请求预处理
     * @param request 请求
     * @throws Exception
     */
    void preRequest(FullHttpRequest request) throws Exception;

    /**
     * 请求结束之后的包装
     * @param request 请求
     * @throws Exception
     */
    void afterRequest(FullHttpRequest request) throws Exception;

}
