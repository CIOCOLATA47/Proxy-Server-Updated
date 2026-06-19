package ru.fiw.proxyserver;

import net.minecraft.ChatFormatting;
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

    private EditBox ipPort;
    private EditBox username;
    private EditBox password;
    private EditBox nameInput;

    private Checkbox enabledCheck;

    private String msg = "";

    private final TestPing testPing = new TestPing();

    private int startY;
    private int centerX;

    private String savedIp = "";
    private String savedUser = "";
    private String savedPass = "";

    public GuiProxy(Screen parent) {
        super(Component.literal("Proxy Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        this.centerX = this.width / 2;
        this.startY = this.height / 2 - 90;

        int x = centerX - 100;

        this.isSocks4 =
                ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Type: " + (isSocks4 ? "Socks 4" : "Socks 5")
                        ),
                        b -> {

                            updateSavedFields();

                            isSocks4 = !isSocks4;

                            ProxyServer.proxy.type =
                                    isSocks4
                                            ? Proxy.ProxyType.SOCKS4
                                            : Proxy.ProxyType.SOCKS5;

                            this.rebuildWidgets();
                        }
                ).bounds(x, startY, 200, 20).build()
        );

        this.ipPort = new EditBox(
                font,
                x,
                startY + 24,
                200,
                20,
                Component.empty()
        );

        this.ipPort.setMaxLength(256);

        this.ipPort.setHint(
                Component.literal("e.g. 125.1.34.1:2555")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        this.ipPort.setValue(
                savedIp.isEmpty()
                        ? ProxyServer.proxy.ipPort
                        : savedIp
        );

        this.addRenderableWidget(ipPort);

        this.username = new EditBox(
                font,
                x,
                startY + 48,
                200,
                20,
                Component.empty()
        );

        this.username.setMaxLength(256);

        this.username.setHint(
                Component.literal(
                        isSocks4
                                ? "e.g. UserID123"
                                : "e.g. username123"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );

        this.username.setValue(
                savedUser.isEmpty()
                        ? ProxyServer.proxy.username
                        : savedUser
        );

        this.addRenderableWidget(username);

        if (!isSocks4) {

            this.password = new EditBox(
                    font,
                    x,
                    startY + 72,
                    200,
                    20,
                    Component.empty()
            );

            this.password.setMaxLength(256);

            this.password.setHint(
                    Component.literal("e.g. myPassword123")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );

            this.password.setValue(
                    savedPass.isEmpty()
                            ? ProxyServer.proxy.password
                            : savedPass
            );

            this.addRenderableWidget(password);
        }

        this.enabledCheck = Checkbox.builder(
                        Component.literal("Enable Proxy"),
                        font
                )
                .pos(x, startY + (isSocks4 ? 76 : 100))
                .selected(ProxyServer.proxyEnabled)
                .onValueChange((cb, checked) ->
                        ProxyServer.proxyEnabled = checked)
                .build();

        this.addRenderableWidget(enabledCheck);

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Apply"),
                        b -> {

                            apply();

                            this.onClose();
                        }
                ).bounds(
                        x,
                        startY + (isSocks4 ? 105 : 129),
                        64,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Test"),
                        b -> {

                            msg = "";

                            if (!ipPort.getValue().contains(":")) {
                                msg = ChatFormatting.RED + "Use IP:Port";
                                return;
                            }

                            testPing.state =
                                    ChatFormatting.YELLOW + "Connecting...";

                            testPing.run(
                                    "mc.hypixel.net",
                                    25565,
                                    new Proxy(
                                            isSocks4,
                                            ipPort.getValue(),
                                            username.getValue(),
                                            password != null
                                                    ? password.getValue()
                                                    : ""
                                    )
                            );
                        }
                ).bounds(
                        x + 68,
                        startY + (isSocks4 ? 105 : 129),
                        64,
                        20
                ).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        b -> this.onClose()
                ).bounds(
                        x + 136,
                        startY + (isSocks4 ? 105 : 129),
                        64,
                        20
                ).build()
        );

        this.nameInput = new EditBox(
                font,
                x,
                startY + (isSocks4 ? 132 : 156),
                120,
                20,
                Component.empty()
        );

        this.nameInput.setMaxLength(64);

        this.nameInput.setHint(
                Component.literal("Name")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        this.addRenderableWidget(nameInput);

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        b -> {

                            updateSavedFields();

                            String name =
                                    nameInput.getValue().isEmpty()
                                            ? "Proxy_" + System.currentTimeMillis()
                                            : nameInput.getValue();

                            Config.accounts.put(
                                    name,
                                    new Proxy(
                                            isSocks4,
                                            savedIp,
                                            savedUser,
                                            savedPass
                                    )
                            );

                            Config.saveConfig();

                            this.rebuildWidgets();
                        }
                ).bounds(
                        x + 124,
                        startY + (isSocks4 ? 132 : 156),
                        76,
                        20
                ).build()
        );

        int presetY = startY + (isSocks4 ? 160 : 184);

        for (Map.Entry<String, Proxy> entry : Config.accounts.entrySet()) {

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(entry.getKey()),
                            b -> {

                                Proxy p = entry.getValue();

                                this.savedIp = p.ipPort;
                                this.savedUser = p.username;
                                this.savedPass = p.password;

                                this.isSocks4 =
                                        p.type == Proxy.ProxyType.SOCKS4;

                                this.rebuildWidgets();
                            }
                    ).bounds(x, presetY, 175, 20).build()
            );

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal("X"),
                            b -> {

                                updateSavedFields();

                                Config.accounts.remove(entry.getKey());

                                Config.saveConfig();

                                this.rebuildWidgets();
                            }
                    ).bounds(x + 180, presetY, 20, 20).build()
            );

            presetY += 22;
        }
    }

    private void updateSavedFields() {

        this.savedIp = ipPort.getValue();

        this.savedUser = username.getValue();

        this.savedPass =
                password != null
                        ? password.getValue()
                        : "";
    }

    private void apply() {

        ProxyServer.proxy = new Proxy(
                isSocks4,
                ipPort.getValue(),
                username.getValue(),
                password != null
                        ? password.getValue()
                        : ""
        );

        ProxyServer.proxyEnabled =
                enabledCheck.selected();

        Config.saveConfig();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor ctx,
            int mouseX,
            int mouseY,
            float delta
    ) {

        super.extractRenderState(ctx, mouseX, mouseY, delta);

        ctx.centeredText(
                font,
                this.title,
                centerX,
                startY - 20,
                0xFFFFFF
        );

        String status =
                !msg.isEmpty()
                        ? msg
                        : (testPing.state != null
                           ? testPing.state
                           : "");

        if (!status.isEmpty()) {
            ctx.text(
                    font,
                    Component.literal(status),
                    centerX + 105,
                    startY + 6,
                    0xFFE0E0E0
            );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}
