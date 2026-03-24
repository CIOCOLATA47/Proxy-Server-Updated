package ru.fiw.proxyserver.mixin;

import io.netty.channel.Channel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fiw.proxyserver.Proxy;
import ru.fiw.proxyserver.ProxyServer;

import java.net.InetSocketAddress;

@Mixin(targets = "net/minecraft/network/Connection$1")
public class ClientConnectionInit {

    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (ProxyServer.proxyEnabled) {
            Proxy proxy = ProxyServer.proxy;
            ProxyServer.lastUsedProxy = proxy;

            InetSocketAddress proxyAddr = new InetSocketAddress(proxy.getIp(), proxy.getPort());

            if (proxy.type == Proxy.ProxyType.SOCKS5) {
                channel.pipeline().addFirst(new Socks5ProxyHandler(
                        proxyAddr,
                        proxy.username.isEmpty() ? null : proxy.username,
                        proxy.password.isEmpty() ? null : proxy.password
                ));
            } else {
                channel.pipeline().addFirst(new Socks4ProxyHandler(
                        proxyAddr,
                        proxy.username.isEmpty() ? null : proxy.username
                ));
            }
        } else {
            ProxyServer.lastUsedProxy = new Proxy();
        }

        if (ProxyServer.proxyMenuButton != null) {
            ProxyServer.proxyMenuButton.setMessage(
                    Component.literal("Proxy: " + ProxyServer.getLastUsedProxyIp())
            );
        }
    }
}