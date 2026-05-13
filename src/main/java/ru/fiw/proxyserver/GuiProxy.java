package ru.fiw.proxyserver;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class GuiProxy extends Screen {
    private final Screen parent;
    private boolean isSocks4;
    private EditBox ipPort, username, password, nameInput;
    private Checkbox enabledCheck;
    private int startY, centerX;

    private String savedIp = "", savedUser = "", savedPass = "";
    private boolean savedEnabled;

    public GuiProxy(Screen parent) {
        super(Component.literal("Proxy Settings"));
        this.parent = parent;
        this.isSocks4 = ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;
        this.savedIp = ProxyServer.proxy.ipPort;
        this.savedUser = ProxyServer.proxy.username;
        this.savedPass = ProxyServer.proxy.password;
        this.savedEnabled = ProxyServer.proxyEnabled;
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.startY = this.height / 2 - 80;
        int x = centerX - 100;

        this.addRenderableWidget(Button.builder(Component.literal("Type: " + (isSocks4 ? "Socks 4" : "Socks 5")), b -> {
            updateSavedFields();
            isSocks4 = !isSocks4;
            this.rebuildWidgets();
        }).bounds(x, startY, 200, 20).build());

        this.ipPort = new EditBox(font, x, startY + 24, 200, 20, Component.empty());
        this.ipPort.setValue(savedIp);
        this.addRenderableWidget(ipPort);

        this.username = new EditBox(font, x, startY + 48, 200, 20, Component.empty());
        this.username.setValue(savedUser);
        this.addRenderableWidget(username);

        if (!isSocks4) {
            this.password = new EditBox(font, x, startY + 72, 200, 20, Component.empty());
            this.password.setValue(savedPass);
            this.addRenderableWidget(password);
        }

        this.enabledCheck = Checkbox.builder(Component.literal("Enable Proxy"), font)
                .pos(x, startY + (isSocks4 ? 72 : 96))
                .selected(savedEnabled)
                .onValueChange((cb, checked) -> savedEnabled = checked)
                .build();
        this.addRenderableWidget(enabledCheck);

        this.nameInput = new EditBox(font, x, startY + (isSocks4 ? 96 : 120), 120, 20, Component.empty());
        this.nameInput.setHint(Component.literal("Name"));
        this.addRenderableWidget(nameInput);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            updateSavedFields();
            String name = nameInput.getValue().isEmpty() ? "Proxy_" + System.currentTimeMillis() : nameInput.getValue();
            Config.accounts.put(name, new Proxy(isSocks4, savedIp, savedUser, savedPass));
            Config.saveConfig();
            this.rebuildWidgets();
        }).bounds(x + 124, startY + (isSocks4 ? 96 : 120), 76, 20).build());

        int presetY = startY + (isSocks4 ? 125 : 149);
        for (Map.Entry<String, Proxy> entry : Config.accounts.entrySet()) {
            this.addRenderableWidget(Button.builder(Component.literal(entry.getKey()), b -> {
                Proxy p = entry.getValue();
                this.savedIp = p.ipPort;
                this.savedUser = p.username;
                this.savedPass = p.password;
                this.isSocks4 = (p.type == Proxy.ProxyType.SOCKS4);
                this.rebuildWidgets();
            }).bounds(x, presetY, 175, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                updateSavedFields();
                Config.accounts.remove(entry.getKey());
                Config.saveConfig();
                this.rebuildWidgets();
            }).bounds(x + 180, presetY, 20, 20).build());
            presetY += 22;
        }
    }

    private void updateSavedFields() {
        if (ipPort != null) savedIp = ipPort.getValue();
        if (username != null) savedUser = username.getValue();
        if (password != null) savedPass = password.getValue();
        if (enabledCheck != null) savedEnabled = enabledCheck.selected();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.centeredText(font, this.title, centerX, startY - 20, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        updateSavedFields();
        ProxyServer.proxy = new Proxy(isSocks4, savedIp, savedUser, savedPass);
        ProxyServer.proxyEnabled = savedEnabled;
        minecraft.setScreen(parent);
    }
}