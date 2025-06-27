/*
 * Copyright (c) 2025. Triibunupsik
 * SPDX-License-Identifier: Apache-2.0
 */

package me.triibu_pause;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TriibuPauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("pause")
                        .requires(source -> source.hasPermissionLevel(4)) // Require operator permission
                        .then(CommandManager.literal("reload")
                                .executes(TriibuPauseCommand::reloadConfig))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.literal("enabled")
                                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setEnabled(context, BoolArgumentType.getBool(context, "value")))))
                                .then(CommandManager.literal("delay")
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                                                .executes(context -> setInterval(context, IntegerArgumentType.getInteger(context, "seconds"))))))
                        .then(CommandManager.literal("status")
                                .executes(TriibuPauseCommand::showStatus))
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        TriibuPauseConfig.load();
        context.getSource().sendFeedback(() -> Text.literal("Auto Pause config reloaded."), true);
        return 1;
    }

    private static int setEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
        TriibuPauseConfig.getInstance().setEnablePauseWhenEmpty(enabled);

        String status = enabled ? "enabled" : "disabled";
        context.getSource().sendFeedback(() -> Text.literal("Pause when empty is now " + status + "."), true);
        return 1;
    }

    private static int setInterval(CommandContext<ServerCommandSource> context, int seconds) {
        TriibuPauseConfig.getInstance().setPauseWhenEmptySeconds(seconds);

        context.getSource().sendFeedback(() -> Text.literal("Pause when empty interval set to " + seconds + " seconds."), true);
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        String status = TriibuPauseConfig.getInstance().getEnablePauseWhenEmpty() ? "enabled" : "disabled";

        context.getSource().sendFeedback(() -> Text.literal("Auto Pause Status:"), false);
        context.getSource().sendFeedback(() -> Text.literal("- Enabled: " + status), false);
        context.getSource().sendFeedback(() -> Text.literal("- Interval: " + TriibuPauseConfig.getInstance().getPauseWhenEmptySeconds() + " seconds"), false);
        return 1;
    }
}