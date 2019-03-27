package scoopy.client.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public interface IScoopyClientCommand extends IClientCommand {
    @Override
    default boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }
}
