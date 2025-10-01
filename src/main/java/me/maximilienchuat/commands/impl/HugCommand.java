package me.maximilienchuat.commands.impl;

import me.maximilienchuat.commands.core.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

@CommandInfo(
        paths = {"react/hug", "money/cuddle", "ball/balls/balled", "hug"},
        description = "Send a hug",
        directPaths = {"hug"}, // only "hug" will appear in direct commands
        args = {
                @ArgInfo(name = "target", type = User.class, required = false)
        },
        options = {
                @SlashArg(
                        name = "target",
                        description = "User to hug",
                        type = OptionType.USER,
                        required = false
                )
        }
)
public class HugCommand extends Command implements PrefixCommand, SlashCommand {

    @Override
    public void executePrefix(@NotNull CommandContext ctx) {
        User target = null;

        // AUTOMATIC: check if parsed argument exists (via ArgInfo)
        if (ctx.getArgs().length > 0) {
            try {
                Object parsed = ctx.parseArg(ctx.getArgs()[0], User.class);
                if (parsed instanceof User user) target = user;
            } catch (IllegalArgumentException ignored) {}
        }

        String authorMention = ctx.getMessageEvent().getAuthor().getAsMention();
        if (target != null) {
            ctx.reply(authorMention + " hugs " + target.getAsMention());
        } else {
            ctx.reply(authorMention + " hugs everyone");
        }
    }

    @Override
    public void executeSlash(@NotNull CommandContext ctx) {
        String authorMention = ctx.getSlashEvent().getUser().getAsMention();
        OptionMapping targetOption = ctx.getSlashEvent().getOption("target");

        if (targetOption != null) {
            User user = targetOption.getAsUser();
            ctx.reply(authorMention + " hugs " + user.getAsMention());
        } else {
            ctx.reply(authorMention + " hugs everyone");
        }
    }
}
