package scoopy.client.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.IClientCommand;

public interface IScoopyClientCommand extends IClientCommand {
    @Override
    default boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }
}
