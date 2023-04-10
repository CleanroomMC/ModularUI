package com.cleanroommc.modularui.theme;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class ThemeReloadCommand extends CommandBase {

    @Override
    public @NotNull String getCommandName() {
        return "reloadThemes";
    }

    @Override
    public @NotNull String getCommandUsage(@NotNull ICommandSender sender) {
        return "/reloadThemes";
    }

    @Override
    public void processCommand(@NotNull ICommandSender sender, String @NotNull [] args) {
        try {
            ThemeManager.reload();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Successfully reloaded themes"));
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error reloaded themes:"));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + e.getMessage()));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
