package org.yinan.web;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author yinan
 * @date 19-8-3
 * group包装类，供外部使用
 */
public class WebSocketSupervise {

    private WebSocketSupervise() {

    }

    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void addChannel(Channel channel) {
        group.add(channel);
    }

    public static void removeChannel(Channel channel) {
        group.remove(channel);
    }

    public static Channel findChannel(ChannelId id) {
        return group.find(id);
    }

    public static void send2All(TextWebSocketFrame msg) {
        group.writeAndFlush(msg);
    }

    public boolean isEmpty() {
        return group.isEmpty();
    }
}
