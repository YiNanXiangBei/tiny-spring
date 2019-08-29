package cn.yinan.web;

import cn.yinan.web.callback.IWebSocketCompleteCallback;
import cn.yinan.web.config.BaseConfig;
import cn.yinan.web.routes.BaseRouteConfig;
import cn.yinan.web.routes.IRouteConfig;
import cn.yinan.web.routes.RoutesManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.yinan.common.config.Config;
import cn.yinan.common.container.Container;

import java.util.List;

/**
 * @author yinan
 * @date 19-6-9
 */
public class WebConfigContainer implements Container {

    private static Logger logger = LoggerFactory.getLogger(WebConfigContainer.class);

    private NioEventLoopGroup workGroup;

    private NioEventLoopGroup bossGroup;

    private IWebSocketCompleteCallback webSocketCallback;

    public WebConfigContainer() {

        //配置管理，并发量很小，使用单线程处理网络事件
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(1);
        RoutesManager.INSTANCE.addRouteConfig(new BaseRouteConfig());
    }

    public WebConfigContainer(IWebSocketCompleteCallback webSocketCallback) {
        this();
        this.webSocketCallback = webSocketCallback;
    }

    public WebConfigContainer(List<IRouteConfig> routeConfigs) {
        this();
        RoutesManager.INSTANCE.addRouteConfig(routeConfigs);
    }

    public WebConfigContainer(List<IRouteConfig> routeConfigs, IWebSocketCompleteCallback webSocketCallback) {
        this();
        this.webSocketCallback = webSocketCallback;
        RoutesManager.INSTANCE.addRouteConfig(routeConfigs);
    }


    @Override
    public void start() {
        logger.info("starting web container ...");
        ServerBootstrap httpServerBootStrap = new ServerBootstrap();
        httpServerBootStrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //将请求和应答消息编码或者解码为HTTP消息
                        pipeline.addLast(new HttpServerCodec());
                        /*
                         * httpObject 解码器,
                         * 它的作用是将多个消息转换为单一的FullHttpRequest或FullHttpResponse
                         * 对象,原因是HTTP 解码器在每个HTTP消息中会生成多个消息对象 (
                         * HttpRequest/HttpResponse
                         * ,HttpContent,LastHttpContent)
                         */
                        pipeline.addLast(new HttpObjectAggregator(512 * 1024));
                        //主要作用是支持异步发送大的码流(例如大文件传输),但不占用过多的内存,防止JAVA内存溢出
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpRequestHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler(Config.getInstance().getStringValue("config.websocket.uri")));
                        if (webSocketCallback == null) {
                            pipeline.addLast(new WebSocketHandler());
                        } else {
                            pipeline.addLast(new WebSocketHandler(webSocketCallback));
                        }
                    }
                });

        try {
            httpServerBootStrap.bind(BaseConfig.getInstance().getConfigServerBind(),
                    BaseConfig.getInstance().getConfigServerPort());
            logger.info("http server start on port: {}", BaseConfig.getInstance().getConfigServerPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //初始化整个web应用，将所有的url全部映射完成
        RoutesManager.INSTANCE.activeConfigs();

    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
