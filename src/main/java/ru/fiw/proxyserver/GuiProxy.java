package ru.fiw.proxyserver;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GuiProxy extends Screen {
    private final Screen parent;
    private boolean isSocks4;
    private TextFieldWidget ipPort, username, password;
    private CheckboxWidget enabledCheck;
    private String msg = "";
    private final TestPing testPing = new TestPing();
    private int startY, centerX;

    public GuiProxy(Screen parent) {
        super(Text.literal("Proxy Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.startY = this.height / 2 - 60;
        int x = centerX - 100;
        this.isSocks4 = ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Type: " + (isSocks4 ? "Socks 4" : "Socks 5")), b -> {
            isSocks4 = !isSocks4;
            ProxyServer.proxy.type = isSocks4 ? Proxy.ProxyType.SOCKS4 : Proxy.ProxyType.SOCKS5;
            this.clearAndInit();
        }).dimensions(x, startY, 200, 20).build());
        this.ipPort = new TextFieldWidget(textRenderer, x, startY + 24, 200, 20, Text.empty());
        this.ipPort.setPlaceholder(Text.literal("e.g. 125.1.34.1:2555").formatted(Formatting.DARK_GRAY));
        this.ipPort.setText(ProxyServer.proxy.ipPort);
        this.addDrawableChild(ipPort);
        this.username = new TextFieldWidget(textRenderer, x, startY + 48, 200, 20, Text.empty());
        this.username.setPlaceholder(Text.literal(isSocks4 ? "User ID" : "Username").formatted(Formatting.DARK_GRAY));
        this.username.setText(ProxyServer.proxy.username);
        this.addDrawableChild(username);
        if (!isSocks4) {
            this.password = new TextFieldWidget(textRenderer, x, startY + 72, 200, 20, Text.empty());
            this.password.setPlaceholder(Text.literal("Password").formatted(Formatting.DARK_GRAY));
            this.password.setText(ProxyServer.proxy.password);
            this.addDrawableChild(password);
        }

        this.enabledCheck = CheckboxWidget.builder(Text.literal("Enable Proxy"), textRenderer)
                .pos(x, startY + 96).checked(ProxyServer.proxyEnabled)
                .callback((cb, checked) -> ProxyServer.proxyEnabled = checked).build();
        this.addDrawableChild(enabledCheck);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), b -> {
            apply();
            this.close();
        }).dimensions(x, startY + 125, 64, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Test"), b -> {
            msg = "";
            if (!ipPort.getText().contains(":")) {
                msg = Formatting.RED + "Use IP:Port";
                return;
            }
            testPing.run("mc.hypixel.net", 25565, new Proxy(isSocks4, ipPort.getText(), username.getText(), password != null ? password.getText() : ""));
        }).dimensions(x + 68, startY + 125, 64, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> this.close()).dimensions(x + 136, startY + 125, 64, 20).build());
    }

    private void apply() {
        ProxyServer.proxy = new Proxy(isSocks4, ipPort.getText(), username.getText(), password != null ? password.getText() : "");
        ProxyServer.proxyEnabled = enabledCheck.isChecked();
        Config.saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, this.title, centerX, startY - 20, 0xFFFFFF);

        String status = !msg.isEmpty() ? msg : testPing.state;

        if (status != null && !status.isEmpty()) {
            int textWidth = textRenderer.getWidth(status);
            int boxWidth = 200;
            int boxHeight = 20;
            int x1 = centerX - (boxWidth / 2);
            int y1 = startY + 155;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFFA0A0A0);

            context.fill(x1, y1, x2, y2, 0xFF000000);

            context.drawTextWithShadow(textRenderer, Text.literal(status), x1 + 4, y1 + (boxHeight - 8) / 2, 0xFFE0E0E0);
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}