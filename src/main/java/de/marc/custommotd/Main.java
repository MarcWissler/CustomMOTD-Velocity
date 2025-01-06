package de.marc.custommotd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Plugin(id = "custommotd", name = "CustomMOTD", version = "1.0-SNAPSHOT")
public class Main {

    private final Path configPath;
    private final Logger logger;
    private List<String> secondLineMotds = new ArrayList<>();
    private String firstLineMotd = "§aDefault First Line MOTD";

    @Inject
    public Main(@DataDirectory Path dataDirectory, Logger logger) {
        this.configPath = dataDirectory.resolve("config.json");
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            loadConfig();
        } catch (IOException e) {
            logger.error("Failed to load or create config.json", e);
        }
    }
@Subscribe
public void onPing(ProxyPingEvent event) {

    var randomSecondLine = secondLineMotds.isEmpty()
            ? "§cDefault Second Line MOTD"
            : secondLineMotds.get(new Random().nextInt(secondLineMotds.size()));
    var motd = firstLineMotd + "\n§b" + randomSecondLine;
    event.setPing(event.getPing().asBuilder().description(Component.text(motd)).build());
}


    private void loadConfig() throws IOException {
        if (!Files.exists(configPath)) {
            createDefaultConfig();
        }
        var configContent = Files.readString(configPath);
        parseConfig(configContent);
    }

    private void createDefaultConfig() throws IOException {
        String defaultConfig = """
            {
                "firstLine": "§aWelcome to the Server!",
                "secondLine": [
                    "§eEnjoy your stay!",
                    "§bDon't forget to have fun!",
                    "§dInvite your friends!"
                ]
            }
            """;
        Files.createDirectories(configPath.getParent());
        Files.writeString(configPath, defaultConfig, StandardOpenOption.CREATE);
        logger.info("Default config.json created at {}", configPath.toAbsolutePath());
    }

    private void parseConfig(String configContent) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(configContent, JsonObject.class);

            // Erste Zeile der MOTD laden
            if (json.has("firstLine")) {
                firstLineMotd = json.get("firstLine").getAsString();
            }

            // Zweite Zeile der MOTD als Liste laden
            if (json.has("secondLine")) {
                secondLineMotds.clear();
                JsonArray secondLineArray = json.getAsJsonArray("secondLine");
                secondLineArray.forEach(line -> secondLineMotds.add(line.getAsString()));
            }

            logger.info("Config successfully loaded.");
        } catch (Exception e) {
            logger.error("Failed to parse config.json", e);
        }
    }
}
