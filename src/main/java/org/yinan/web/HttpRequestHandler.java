package org.yinan.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.common.config.Config;
import org.yinan.common.util.JsonUtil;
import org.yinan.web.middleware.MiddlewareManager;
import org.yinan.web.response.ObjectMethod;
import org.yinan.web.response.ResponseInfo;
import org.yinan.web.routes.RoutesManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinan
 * @date 19-6-11
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    private static Pattern PATTERN = Pattern.compile("^.*?\\.(jpg|jpeg|html|gif|js|css|json|ico|svg|woff2|woff|ttf)$");

    /**
     * shell脚本中有写一些参数变量，这里就是调用shell脚本中的东西 java -D命令
     */
    private static final String PAGE_FOLDER = System.getProperty("app.home", System.getProperty("user.dir"))
            + "/webpages";

    private static final String SERVER_VS = "LPS-0.1";

    private static final String NOT_FOUND_PAGE = "/404.html";

    /**
     * 处理http请求 目前仅支持get和post请求，所有处理返回结果均为json格式，后续可能会推出返回文件数据类型
     * @param channelHandlerContext
     * @param request
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        //预先就进行拦截器部分处理
        ResponseInfo responseInfo = ResponseInfo.build(ResponseInfo.CODE_API_NOT_FOUND, "request invalid");
        ResponseInfo middlewareResult =  MiddlewareManager.run(request);
        //拦截器没有结果，说明拦截器验证通过
        if (middlewareResult != null) {
            responseInfo = middlewareResult;
            responseInfo.headers(HttpHeaderNames.CONTENT_TYPE, "Application/json;charset=utf-8");
        } else {
            if (Config.getInstance().getStringValue("config.websocket.uri", "/ws").equals(request.uri())) {
                channelHandlerContext.fireChannelRead(request.retain());
                return;
            }
            try {
                URI uri = new URI(request.uri());
                logger.info("received request: [{}], type: [{}]", uri.getPath(), request.method());
                if (HttpMethod.GET == request.method()) {
                    //get方法这里需要依据请求的url进行判断,如果是.*后缀名结尾的，那么寻找对应文件，如果没有后缀名，那么去控制层寻找对应url然后依据对应url做出处理
                    Matcher matcher = PATTERN.matcher(uri.getPath());
                    //有后缀名的文件，直接去查找该文件，不经过请求
                    if (matcher.matches()) {
                        outputPages(channelHandlerContext, request);
                        return;
                    } else {
                        //无后缀名的文件需要经过请求去拦截判断相关数据
                        ObjectMethod objectMethod = RoutesManager.INSTANCE.gotGetMethod(uri.getPath());
                        if (objectMethod != null) {
                            responseInfo = (ResponseInfo) objectMethod.getMethod().invoke(objectMethod.getClazz().newInstance(), request);
                            logger.info("response type: {}", objectMethod.getMethod().getGenericReturnType().getTypeName());
                        }
                    }
                } else if (HttpMethod.POST == request.method()) {
                    ObjectMethod objectMethod = RoutesManager.INSTANCE.gotPostMethod(uri.getPath());
                    logger.info("get object method: {}", objectMethod);
                    if (objectMethod != null) {
                        responseInfo = (ResponseInfo)objectMethod.getMethod().invoke(objectMethod.getClazz().newInstance(), request);
                        responseInfo.headers(HttpHeaderNames.CONTENT_TYPE, "Application/json;charset=utf-8");
                    }
                }
            } catch (Exception ex) {
                logger.error("server error : {}", ex.getMessage());
                responseInfo = ResponseInfo.build(ResponseInfo.CODE_SYSTEM_ERROR, "server error");
                responseInfo.headers(HttpHeaderNames.CONTENT_TYPE, "Application/json;charset=utf-8");
            }
            //todo 后续需要将返回类型修改成常用类型，然后经过afterRun方法进行包装之后统一返回
            MiddlewareManager.afterRun(request);
        }
        outputContent(channelHandlerContext, request, responseInfo.getCode() / 100, responseInfo,
                "Application/json;charset=utf-8");
    }

    private void outputContent(ChannelHandlerContext ctx, FullHttpRequest request, int code, ResponseInfo responseInfo,
                               String mimeType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(code),
                Unpooled.wrappedBuffer(JsonUtil.object2json(responseInfo).getBytes(Charset.forName("UTF-8"))));
        //设置返回中的headers
        responseInfo.setHeaders(response);
        responseInfo.setCookies(response);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.SERVER, SERVER_VS);
        ChannelFuture future = ctx.writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(request)) {
            //在future中添加一个监听器，当服务端发送给客户端的数据完成之后，关闭channel
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 输出静态资源
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void outputPages(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HttpResponseStatus status = HttpResponseStatus.OK;
        URI uri = new URI(request.uri());
        String uriPath = uri.getPath();
        String path = PAGE_FOLDER + uriPath;
        File rFile = new File(path);
        if (!rFile.exists()) {
            path = PAGE_FOLDER + NOT_FOUND_PAGE;
            rFile = new File(path);
        }
        if (HttpUtil.is100ContinueExpected(request)) {
            send100Continue(ctx);
        }

        String mimeType = MimeType.getMimeType(MimeType.parseSuffix(path));
        long length = 0;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(rFile, "r");
            length = raf.length();
        } finally {
            if (length < 0) {
                raf.close();
            }
        }

        HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        response.headers().set(HttpHeaderNames.SERVER, SERVER_VS);
        ctx.write(response);
        if (ctx.pipeline().get(SslHandler.class) == null) {
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
        } else {
            ctx.write(new ChunkedNioFile(raf.getChannel()));
        }

        //针对trunked编码，最后需要发送一个编码结束的空消息体，将lasthttpcontent 的empty_last_content发送到缓冲区，标示所有消息都与已经发送完毕
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    /**
     * 客户端在发送一个实体的主题给服务端之前，会先发送一个携带100Continue的except给服务端，服务端2需要进行1响应
     * @param ctx
     */
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server meet error: {}", cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
