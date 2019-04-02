package scoopy.client.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

import static scoopy.Scoopy.DEP_GREGTECH_ID;
import static scoopy.Scoopy.DEP_NUTRITION_ID;

public class CommandClientScoopy extends CommandTreeBase implements IScoopyClientCommand {

    public CommandClientScoopy() {
        addSubcommand(new CommandPingPong());
        if (Loader.isModLoaded(DEP_NUTRITION_ID))
            addSubcommand(new CommandTreeNutrition());
        if (Loader.isModLoaded(DEP_GREGTECH_ID))
            addSubcommand(new CommandTreeGregtech());
        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName() {
        return "cscoopy";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Client Scoopy Commands";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
