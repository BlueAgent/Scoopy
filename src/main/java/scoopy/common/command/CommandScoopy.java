package scoopy.common.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandScoopy extends CommandTreeBase {

    public CommandScoopy() {
        addSubcommand(new CommandEventHandlers());
        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName() {
        return "scoopy";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Scoopy Commands";
    }
}
