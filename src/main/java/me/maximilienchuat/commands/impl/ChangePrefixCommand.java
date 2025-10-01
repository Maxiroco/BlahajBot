package me.maximilienchuat.commands.impl;

import me.maximilienchuat.Whatever;
import me.maximilienchuat.commands.core.*;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
        paths = {"settings/prefix"},
        description = "Change the server prefix",
        directPaths = {}
)
public class ChangePrefixCommand extends Command implements PrefixCommand {

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 0) {
            ctx.reply("Usage: settings/prefix <new prefix>");
            return;
        }

        String newPrefix = args[0];
        Guild guild = ctx.getMessageEvent().getGuild();
        Whatever.getSettingsManager().setPrefix(guild, newPrefix);

        ctx.reply("Prefix changed to `" + newPrefix + "`");
    }
}
