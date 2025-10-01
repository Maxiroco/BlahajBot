package me.maximilienchuat;

import me.maximilienchuat.commands.core.*;
import me.maximilienchuat.settings.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BotListener extends ListenerAdapter {

    private final CommandRegistry registry;
    GuildSettingsManager settingsManager;
    String prefix;

    public BotListener(CommandRegistry registry, GuildSettingsManager settingsManager) {
        this.registry = registry;
        this.settingsManager = settingsManager;
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String prefix = getPrefixForGuild(event.getGuild()); // stub for per-guild prefix
        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(prefix)) return;

        String[] parts = msg.substring(prefix.length()).trim().split("\\s+");
        if (parts.length == 0) return;

        // Delegate matching to CommandRegistry
        CommandRegistry.PrefixMatchResult result = registry.matchPrefixCommand(parts);

        if (result.command() != null) {
            String[] args = Arrays.copyOfRange(parts, result.length(), parts.length);
            try {
                ((PrefixCommand) result.command()).executePrefix(new CommandContext(event, args, prefix, registry));
            } catch (Exception e) {
                event.getChannel().sendMessage("Error executing command").queue();
                CommandRegistry.logger.error("Error executing prefix command: {}", String.join(" ", parts), e);
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
            ((SlashCommand) cmd).executeSlash(new CommandContext(event, args, prefix, registry));
        } else {
            event.reply("Command not found or not slashable").queue();
        }
    }

    private String getPrefixForGuild(Guild guild) {
        return settingsManager.getPrefix(guild);
    }


}
