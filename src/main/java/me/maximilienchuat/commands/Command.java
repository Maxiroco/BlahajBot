package me.maximilienchuat.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface Command {
    void execute(MessageReceivedEvent event, String[] args);
}
