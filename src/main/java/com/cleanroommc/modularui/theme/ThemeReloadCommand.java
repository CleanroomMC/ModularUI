package com.cleanroommc.modularui.theme;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ThemeReloadCommand extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "reloadThemes";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "/reloadThemes";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        ThemeManager.reload();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
