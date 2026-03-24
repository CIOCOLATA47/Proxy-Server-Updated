package ru.fiw.proxyserver;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiProxy extends Screen {
    private final Screen parent;
    private boolean isSocks4;
    private EditBox ipPort, username, password;
    private Checkbox enabledCheck;
    private String msg = "";
    private final TestPing testPing = new TestPing();
    private int startY, centerX;

    public GuiProxy(Screen parent) {
        super(Component.literal("Proxy Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.startY = this.height / 2 - 60;
        int x = centerX - 100;
        this.isSocks4 = ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;

        this.addRenderableWidget(Button.builder(
                Component.literal("Type: " + (isSocks4 ? "Socks 4" : "Socks 5")), b -> {
                    isSocks4 = !isSocks4;
                    ProxyServer.proxy.type = isSocks4 ? Proxy.ProxyType.SOCKS4 : Proxy.ProxyType.SOCKS5;
                    this.rebuildWidgets();
                }).bounds(x, startY, 200, 20).build());

        this.ipPort = new EditBox(font, x, startY + 24, 200, 20, Component.empty());
        this.ipPort.setHint(Component.literal("e.g. 125.1.34.1:2555").withStyle(ChatFormatting.DARK_GRAY));
        this.ipPort.setValue(ProxyServer.proxy.ipPort);
        this.addRenderableWidget(ipPort);

        this.username = new EditBox(font, x, startY + 48, 200, 20, Component.empty());
        this.username.setHint(Component.literal(isSocks4 ? "User ID" : "Username").withStyle(ChatFormatting.DARK_GRAY));
        this.username.setValue(ProxyServer.proxy.username);
        this.addRenderableWidget(username);

        if (!isSocks4) {
            this.password = new EditBox(font, x, startY + 72, 200, 20, Component.empty());
            this.password.setHint(Component.literal("Password").withStyle(ChatFormatting.DARK_GRAY));
            this.password.setValue(ProxyServer.proxy.password);
            this.addRenderableWidget(password);
        }

        this.enabledCheck = Checkbox.builder(Component.literal("Enable Proxy"), font)
                .pos(x, startY + 96)
                .selected(ProxyServer.proxyEnabled)
                .onValueChange((cb, checked) -> ProxyServer.proxyEnabled = checked)
                .build();
        this.addRenderableWidget(enabledCheck);

        this.addRenderableWidget(Button.builder(Component.literal("Apply"), b -> {
            apply();
            this.onClose();
        }).bounds(x, startY + 125, 64, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Test"), b -> {
            msg = "";
            if (!ipPort.getValue().contains(":")) {
                msg = ChatFormatting.RED + "Use IP:Port";
                return;
            }
            testPing.run("mc.hypixel.net", 25565, new Proxy(
                    isSocks4,
                    ipPort.getValue(),
                    username.getValue(),
                    password != null ? password.getValue() : ""
            ));
        }).bounds(x + 68, startY + 125, 64, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.onClose())
                .bounds(x + 136, startY + 125, 64, 20).build());
    }

    private void apply() {
        ProxyServer.proxy = new Proxy(
                isSocks4,
                ipPort.getValue(),
                username.getValue(),
                password != null ? password.getValue() : ""
        );
        ProxyServer.proxyEnabled = enabledCheck.selected();
        Config.saveConfig();
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.centeredText(font, this.title, centerX, startY - 20, 0xFFFFFF);

        String status = !msg.isEmpty() ? msg : testPing.state;
        if (status != null && !status.isEmpty()) {
            int boxWidth = 200;
            int boxHeight = 20;
            int x1 = centerX - (boxWidth / 2);
            int y1 = startY + 155;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFFA0A0A0);
            ctx.fill(x1, y1, x2, y2, 0xFF000000);
            ctx.text(font, Component.literal(status), x1 + 4, y1 + (boxHeight - 8) / 2, 0xFFE0E0E0);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}