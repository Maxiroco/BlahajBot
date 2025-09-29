package me.maximilienchuat;

import me.maximilienchuat.commands.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BotListener extends ListenerAdapter {

    private static final String PREFIX = "b.";
    private final CommandsRegistry registry = new CommandsRegistry();

    public BotListener() {
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(PREFIX)) return;

        String trim = msg.substring(PREFIX.length()).trim();
        String[] parts = trim.split("\\s+");
        if (parts.length == 0) return;

        // First word after prefix is either a command or type of commands, rest is args
        String commandName = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        Command command = registry.get(commandName);
        if (command != null) {
            command.execute(event, args);
        }

        // might want to change how this works using a Handle command if msg.startsWith(PREFIX)
    }

}
