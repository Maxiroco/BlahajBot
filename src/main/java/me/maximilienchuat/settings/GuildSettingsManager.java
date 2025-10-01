package me.maximilienchuat.settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GuildSettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
    private static final String FILE_PATH = "guildSettings.json";

    private final Gson gson = new Gson();
    private final Map<String, GuildSettings> guildSettings = new HashMap<>();

    public GuildSettingsManager() {
        load();
    }

    public String getPrefix(Guild guild) {
        return guildSettings.getOrDefault(guild.getId(), new GuildSettings()).prefix;
    }

    public void setPrefix(Guild guild, String newPrefix) {
        GuildSettings settings = guildSettings.computeIfAbsent(guild.getId(), k -> new GuildSettings());
        settings.prefix = newPrefix;
        save();
    }

    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, GuildSettings>>() {}.getType();
            Map<String, GuildSettings> loaded = gson.fromJson(reader, type);
            if (loaded != null) guildSettings.putAll(loaded);
        } catch (Exception e) {
            logger.error("Failed to load guild settings", e);
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(guildSettings, writer);
        } catch (IOException e) {
            logger.error("Failed to save guild settings", e);
        }
    }

    public static class GuildSettings {
        public String prefix = "b."; // default prefix
    }
}
