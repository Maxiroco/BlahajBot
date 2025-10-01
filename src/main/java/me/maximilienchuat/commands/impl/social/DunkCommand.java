package me.maximilienchuat.commands.impl.social;

import me.maximilienchuat.commands.core.*;

@CommandInfo(
        paths = {"fun/dunk", "ball/balls/balled"},
        description = "Do a dunk!"
)
public class DunkCommand extends Command implements PrefixCommand, SlashCommand {
    private static final String DUNK_URL = "https://tenor.com/view/niko-oneshot-ballin-teste-niko-ballin-gif-26110464";

    @Override
    public void executePrefix(CommandContext ctx) {
        ctx.reply("Dunkin Donuts");
        ctx.reply(DUNK_URL);
    }

    @Override
    public void executeSlash(CommandContext ctx) {
        ctx.reply(DUNK_URL);
    }
}
