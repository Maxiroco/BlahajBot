package me.maximilienchuat.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.Random;

public class RollCommand implements Command {
    private static final Random random = new Random();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        int sides = 6; // default
        if (args.length > 0) {
            try {
                sides = Integer.parseInt(args[0]);
                if (sides < 1) {
                    event.getChannel().sendMessage("Need at least 1 side").queue();
                    return;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid number of sides").queue();
                return;
            }
        }

        final int finalSides = sides;

        event.getChannel().sendMessage("🎲 Rolling between 1 and " + finalSides).queue(message -> {
            java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
            final int[] count = {0};

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    count[0]++;
                    if (count[0] <= 3) {
                        String dots = ".".repeat(count[0]);
                        message.editMessage("🎲 Rolling between 1 and " + finalSides + dots + "⠀").queue();
                    } else {
                        int roll = random.nextInt(finalSides) + 1;
                        message.editMessage("🎲 Rolling between 1 and " + finalSides + "..."+ "\nResult: " + roll).queue();
                        scheduler.shutdown();
                    }
                }
            };

            scheduler.scheduleAtFixedRate(task, 250, 250, java.util.concurrent.TimeUnit.MILLISECONDS);
        });
    }
}
