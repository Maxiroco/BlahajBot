package me.maximilienchuat.commands.core;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class CommandContext {
    private final MessageReceivedEvent messageEvent;
    private final SlashCommandInteractionEvent slashEvent;
    private final String[] args;
    private final String prefix;

    // Constructor for prefix (message) commands

    public CommandContext(MessageReceivedEvent event, String[] args, String prefix) {
        this.messageEvent = event;
        this.slashEvent = null;
        this.args = args != null ? args : new String[0];
        this.prefix = prefix;
    }


    public CommandContext(SlashCommandInteractionEvent event, String[] args, String prefix) {
        this.messageEvent = null;
        this.slashEvent = event;
        this.args = args != null ? args : new String[0];
        this.prefix = prefix;
    }

    public String getPrefix() { return prefix; }


    public boolean isMessage() { return messageEvent != null; }
    public boolean isSlash() { return slashEvent != null; }

    public MessageChannel getChannel() {
        if (isMessage()) return messageEvent.getChannel();
        if (isSlash()) return slashEvent.getChannel();
        throw new IllegalStateException("No valid channel");
    }

    public String[] getArgs() { return args; }

    public MessageReceivedEvent getMessageEvent() { return messageEvent; }
    public SlashCommandInteractionEvent getSlashEvent() { return slashEvent; }

    // Generic reply helper
    public void reply(String msg) {
        if (isMessage()) getChannel().sendMessage(msg).queue();
        else if (isSlash()) {
            if (!slashEvent.isAcknowledged()) slashEvent.reply(msg).queue();
            else slashEvent.getHook().sendMessage(msg).queue(); // follow-up if already replied
        }
    }

    /**
     * Parse a single prefix argument into a typed object.
     * Supports String, Integer, and User.
     * Throws IllegalArgumentException if invalid.
     */
    public Object parseArg(String raw, Class<?> type) throws IllegalArgumentException {
        if (!isMessage()) {
            throw new IllegalStateException("parseArg is only for prefix commands");
        }

        MessageReceivedEvent event = getMessageEvent();

        if (type == String.class) return raw;

        if (type == Integer.class) {
            try { return Integer.parseInt(raw); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Expected a number, got: " + raw); }
        }

        if (type == User.class) {
            if (!raw.matches("<@!?\\d+>")) throw new IllegalArgumentException("Invalid user mention: " + raw);
            String id = raw.replaceAll("\\D", "");
            User user = event.getJDA().getUserById(id);
            if (user == null) throw new IllegalArgumentException("User not found: " + raw);
            return user;
        }

        throw new IllegalArgumentException("Unsupported type " + type.getSimpleName());
    }
}
