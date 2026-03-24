package ru.fiw.proxyserver;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.components.Button;

public class ProxyServer implements ModInitializer {
    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static Proxy lastUsedProxy = new Proxy();

    public static Button proxyMenuButton;

    public static String getLastUsedProxyIp() {
        if (lastUsedProxy == null || lastUsedProxy.ipPort == null || lastUsedProxy.ipPort.isEmpty()) {
            return "none";
        }
        return lastUsedProxy.getIp();
    }

    @Override
    public void onInitialize() {
        Config.loadConfig();
    }
}