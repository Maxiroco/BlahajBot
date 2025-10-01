package me.maximilienchuat;

import me.maximilienchuat.commands.core.CommandRegistry;
import me.maximilienchuat.settings.GuildSettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import io.github.cdimascio.dotenv.Dotenv;

public class BlahajBot {

    private static GuildSettingsManager settingsManager;

    public static GuildSettingsManager getSettingsManager(){
        return settingsManager;
    }

    public static void main(String[] args) throws InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_TOKEN");

        CommandRegistry registry = new CommandRegistry();
        registry.discoverAndRegister("me.maximilienchuat.commands.impl");

        settingsManager = new GuildSettingsManager(); // new manager

        JDA bot = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("with your mom"))
                .addEventListeners(new BotListener(registry, settingsManager)) // pass it here
                .build();

        bot.awaitReady();
        registry.registerSlashCommands(bot);
    }

}