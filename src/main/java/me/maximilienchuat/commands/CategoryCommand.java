package me.maximilienchuat.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public class CategoryCommand implements Command {
    private final Map<String, Command> subcommands = new HashMap<>();

    public void add(String name, Command cmd) {
        subcommands.put(name, cmd);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) return; // no subcommand given

        String subName = args[0];
        Command sub = subcommands.get(subName);
        if (sub != null) {
            sub.execute(event, Arrays.copyOfRange(args, 1, args.length));
        }
    }
}
