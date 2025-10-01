package me.maximilienchuat;

import me.maximilienchuat.commands.core.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static me.maximilienchuat.commands.core.CommandRegistry.logger;

public class BotListener extends ListenerAdapter {

    private final CommandRegistry registry;
    private final String prefix;

    public BotListener(CommandRegistry registry, String prefix) {
        this.registry = registry;
        this.prefix = prefix;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(prefix)) return;

        String[] parts = msg.substring(prefix.length()).trim().split("\\s+");
        if (parts.length == 0) return;

        CommandRegistry registry = Whatever.getRegistry();
        Command matched = null;
        int matchedLength = 0;

        // Search all commands (direct commands + leaf commands)
        for (Command cmd : registry.getAllCommands().values()) {
            if (!(cmd instanceof PrefixCommand)) continue;

            CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
            if (info != null) {
                // Check declared paths
                for (String path : info.paths()) {
                    String[] pathParts = path.split("/");

                    if (parts.length >= pathParts.length) {
                        boolean match = true;
                        for (int i = 0; i < pathParts.length; i++) {
                            if (!parts[i].equalsIgnoreCase(pathParts[i])) {
                                match = false;
                                break;
                            }
                        }
                        if (match && pathParts.length > matchedLength) {
                            matched = cmd;
                            matchedLength = pathParts.length;
                        }
                    }
                }

                // Check directPaths (single-word commands)
                for (String direct : info.directPaths()) {
                    if (parts[0].equalsIgnoreCase(direct) && 1 > matchedLength) {
                        matched = cmd;
                        matchedLength = 1;
                    }
                }
            }
        }

        // --- If no leaf matched, check root categories ---
        if (matched == null && registry.getRootCategories().containsKey(parts[0])) {
            CategoryCommand cat = registry.getRootCategories().get(parts[0]);
            matched = new CategoryWrapperCommand(parts[0], cat, registry, prefix);
            matchedLength = 1;
        }

        if (matched != null) {
            String[] args = Arrays.copyOfRange(parts, matchedLength, parts.length);
            try {
                ((PrefixCommand) matched).executePrefix(new CommandContext(event, args, prefix));
            } catch (Exception e) {
                event.getChannel().sendMessage("Error executing command").queue();
                CommandRegistry.logger.error("Error executing prefix command: " + String.join(" ", parts), e);
            }
        } else {
            event.getChannel().sendMessage("Command not found: " + String.join(" ", parts)
                    + "\nType " + prefix + "help for the list of available commands").queue();
        }
    }



    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command cmd = registry.getFlatCommand(event.getName());
        if (cmd instanceof SlashCommand) {
            // Pass all provided options as args array (optional)
            String[] args = event.getOptions().stream()
                    .map(OptionMapping::getAsString)
                    .toArray(String[]::new);
            ((SlashCommand) cmd).executeSlash(new CommandContext(event, args, prefix));
        } else {
            event.reply("Command not found or not slashable").queue();
        }
    }

}
