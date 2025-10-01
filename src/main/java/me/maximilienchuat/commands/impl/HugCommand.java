package me.maximilienchuat.commands.impl;

import me.maximilienchuat.commands.core.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
        paths = {"react/hug", "money/cuddle", "hug"},
        description = "Send a hug",
        directPaths = {"hug"} // only "hug" will appear in direct commands
)
public class HugCommand extends Command implements PrefixCommand, SlashCommand {

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        String authorMention = ctx.getMessageEvent().getAuthor().getAsMention();

        if (ctx.getArgs().length == 0) {
            ctx.reply(authorMention + " hugs everyone");
            return;
        }

        try {
            User user = (User) ctx.parseArg(ctx.getArgs()[0], User.class);
            ctx.reply(authorMention + " hugs " + user.getAsMention());
        } catch (IllegalArgumentException e) {
            ctx.reply(authorMention + " hugs " + ctx.getArgs()[0]); // fallback to raw string
        }
    }

    @Override
    public void executeSlash(@NotNull CommandContext ctx) {
        String authorMention = ctx.getSlashEvent().getUser().getAsMention();

        OptionMapping opt = ctx.getSlashEvent().getOption("target");
        if (opt != null) {
            User user = opt.getAsUser();
            ctx.reply(authorMention + " hugs " + user.getAsMention());
        } else {
            ctx.reply(authorMention + " hugs everyone");
        }
    }
}
