package ru.fiw.proxyserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.ProxyConnectionEvent;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.ChatFormatting;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class TestPing {
    private static final EventLoopGroup GROUP = new NioEventLoopGroup();
    public volatile String state = "";

    public void run(String targetIp, int targetPort, Proxy proxy) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                state = ChatFormatting.YELLOW + "Connecting...";

                String[] parts = proxy.ipPort.split(":");
                InetSocketAddress proxyAddr = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));

                Bootstrap b = new Bootstrap().group(GROUP).channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .handler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                if (proxy.type == Proxy.ProxyType.SOCKS5) {
                                    ch.pipeline().addLast(new Socks5ProxyHandler(proxyAddr, proxy.username, proxy.password));
                                } else {
                                    ch.pipeline().addLast(new Socks4ProxyHandler(proxyAddr, proxy.username));
                                }

                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                        if (evt instanceof ProxyConnectionEvent) {
                                            state = ChatFormatting.GREEN + "Success!";
                                            ctx.close();
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        state = ChatFormatting.RED + "Failed: " + cause.getMessage();
                                        ctx.close();
                                    }
                                });
                            }
                        });

                b.connect(targetIp, targetPort).sync();
            } catch (Exception e) {
                state = ChatFormatting.RED + "Error: " + e.getMessage();
            }
        });
    }
}