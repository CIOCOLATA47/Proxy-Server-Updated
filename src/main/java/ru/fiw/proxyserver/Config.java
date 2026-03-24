package ru.fiw.proxyserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final String CONFIG_PATH = Minecraft.getInstance().gameDirectory + "/config/ProxyServerConfig.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Map<String, Proxy> accounts = new HashMap<>();
    public static String lastPlayerName = "";

    public static void loadConfig() {
        File configFile = new File(CONFIG_PATH);
        try {
            if (!configFile.exists()) {
                saveConfig();
                return;
            }

            String configString = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            if (!configString.isEmpty()) {
                JsonObject configJson = JsonParser.parseString(configString).getAsJsonObject();

                if (configJson.has("lastPlayerName")) {
                    lastPlayerName = configJson.get("lastPlayerName").getAsString();
                }

                if (configJson.has("proxy-enabled")) {
                    ProxyServer.proxyEnabled = configJson.get("proxy-enabled").getAsBoolean();
                }

                if (configJson.has("proxy")) {
                    ProxyServer.proxy = GSON.fromJson(configJson.get("proxy"), Proxy.class);
                }

                Type type = new TypeToken<HashMap<String, Proxy>>() {}.getType();
                if (configJson.has("accounts")) {
                    accounts = GSON.fromJson(configJson.get("accounts"), type);
                }

                if (accounts == null) {
                    accounts = new HashMap<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            JsonObject configJson = new JsonObject();

            configJson.addProperty("lastPlayerName", lastPlayerName);
            configJson.addProperty("proxy-enabled", ProxyServer.proxyEnabled);
            configJson.add("proxy", GSON.toJsonTree(ProxyServer.proxy));
            configJson.add("accounts", GSON.toJsonTree(accounts));

            FileUtils.write(new File(CONFIG_PATH), GSON.toJson(configJson), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}