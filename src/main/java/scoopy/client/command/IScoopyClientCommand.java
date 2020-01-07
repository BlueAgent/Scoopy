package scoopy.client.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.IClientCommand;

/**
 * Can be used on common commands as well (for when they are runnable on the client too)
 */
public interface IScoopyClientCommand extends IClientCommand {
    @Override
    default boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }
}
