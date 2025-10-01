package me.maximilienchuat.commands.impl.money;

import me.maximilienchuat.commands.core.*;

@CommandInfo(
        paths = {"money/beg"},
        description = "Be like Squidward"
)
public class BegCommand extends Command implements PrefixCommand, SlashCommand {

    @Override
    public void executePrefix(CommandContext ctx) {
        
    }

    @Override
    public void executeSlash(CommandContext ctx) {

    }
}
