package me.maximilienchuat;

import me.maximilienchuat.commands.core.CommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import io.github.cdimascio.dotenv.Dotenv;

public class Whatever {

    private static CommandRegistry registry;

    public static CommandRegistry getRegistry() {
        return registry;
    }

    public static void main(String[] args) throws InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_TOKEN");

        registry = new CommandRegistry();
        registry.discoverAndRegister("me.maximilienchuat.commands.impl");

        JDA bot = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("with your mom"))
                .addEventListeners(new BotListener(registry)) // no prefix here
                .build();

        bot.awaitReady();
        registry.registerSlashCommands(bot);
    }
}
