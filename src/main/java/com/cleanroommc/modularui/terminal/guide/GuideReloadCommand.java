package com.cleanroommc.modularui.terminal.guide;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class GuideReloadCommand extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "reloadGuides";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/reloadGuides";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        GuideManager.reload();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
