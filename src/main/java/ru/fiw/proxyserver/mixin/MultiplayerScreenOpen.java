package ru.fiw.proxyserver.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fiw.proxyserver.Config;
import ru.fiw.proxyserver.GuiProxy;
import ru.fiw.proxyserver.ProxyServer;

@Mixin(JoinMultiplayerScreen.class)
public class MultiplayerScreenOpen {
    @Inject(method = "init()V", at = @At("TAIL"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        String playerName = Minecraft.getInstance().getUser().getName();
        if (!playerName.equals(Config.lastPlayerName)) {
            Config.lastPlayerName = playerName;
            if (Config.accounts.containsKey(playerName)) {
                ProxyServer.proxy = Config.accounts.get(playerName);
            } else {
                if (Config.accounts.containsKey("")) {
                    ProxyServer.proxy = Config.accounts.get("");
                }
            }
        }

        JoinMultiplayerScreen ms = (JoinMultiplayerScreen) (Object) this;
        ProxyServer.proxyMenuButton = Button.builder(
                Component.literal("Proxy: " + ProxyServer.getLastUsedProxyIp()),
                (button) -> Minecraft.getInstance().setScreen(new GuiProxy(ms))
        ).bounds(ms.width - 125, 5, 120, 20).build();

        ScreenAccessor si = (ScreenAccessor) ms;
        si.getRenderables().add(ProxyServer.proxyMenuButton);
        si.getNarratables().add(ProxyServer.proxyMenuButton);
        si.getChildren().add(ProxyServer.proxyMenuButton);
    }
}