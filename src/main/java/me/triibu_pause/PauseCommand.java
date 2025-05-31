package me.triibu_pause;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.triibu_pause.PauseConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("triibu_pause")
                        .requires(source -> source.hasPermissionLevel(4)) // Require operator permission
                        .then(CommandManager.literal("reload")
                                .executes(PauseCommand::reloadConfig))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.literal("enabled")
                                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setEnabled(context, BoolArgumentType.getBool(context, "value")))))
                                .then(CommandManager.literal("interval")
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                                                .executes(context -> setInterval(context, IntegerArgumentType.getInteger(context, "seconds"))))))
                        .then(CommandManager.literal("status")
                                .executes(PauseCommand::showStatus))
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        context.load();
        context.getSource().sendFeedback(() -> Text.literal("Triibu Pause config reloaded."), true);
        return 1;
    }

    private static int setEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
        PauseConfig config = PauseConfig.getInstance();
        config.setEnablePauseWhenEmpty(enabled);
        PauseConfig.save();

        String status = enabled ? "enabled" : "disabled";
        context.getSource().sendFeedback(() -> Text.literal("Pause when empty is now " + status + "."), true);
        return 1;
    }

    private static int setInterval(CommandContext<ServerCommandSource> context, int seconds) {
        PauseConfig config = PauseConfig.getInstance();
        config.setPauseWhenEmptySeconds(seconds);
        PauseConfig.save();

        context.getSource().sendFeedback(() -> Text.literal("Pause when empty interval set to " + seconds + " seconds."), true);
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        PauseConfig config = PauseConfig.getInstance();
        String status = config.isEnablePauseWhenEmpty() ? "enabled" : "disabled";

        context.getSource().sendFeedback(() -> Text.literal("Triibu Pause Status:"), false);
        context.getSource().sendFeedback(() -> Text.literal("- Enabled: " + status), false);
        context.getSource().sendFeedback(() -> Text.literal("- Interval: " + config.getPauseWhenEmptySeconds() + " seconds"), false);
        return 1;
    }
}
