package me.maximilienchuat.commands.core;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
    void executeSlash(CommandContext ctx);
}