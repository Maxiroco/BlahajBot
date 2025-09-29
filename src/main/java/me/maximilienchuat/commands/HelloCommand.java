package me.maximilienchuat.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelloCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("Hello bleh").queue();
    }
}
