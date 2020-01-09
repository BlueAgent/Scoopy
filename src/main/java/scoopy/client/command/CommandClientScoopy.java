package scoopy.client.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import scoopy.Scoopy;
import scoopy.common.command.CommandDataParameters;
import scoopy.common.command.CommandEventHandlers;

import static scoopy.Scoopy.DEP_GREGTECH_ID;
import static scoopy.Scoopy.DEP_NUTRITION_ID;

public class CommandClientScoopy extends CommandTreeBase implements IScoopyClientCommand {

    public CommandClientScoopy() {
        addSubcommand(new CommandPingPong());
        if (Loader.isModLoaded(DEP_GREGTECH_ID)) {
            Scoopy.log.info("Client Gregtech Commands Loaded");
            addSubcommand(new CommandTreeGregtech());
        }
        if (Loader.isModLoaded(DEP_NUTRITION_ID)) {
            Scoopy.log.info("Client Nutrition Commands Loaded");
            addSubcommand(new CommandTreeNutrition());
        }
        addSubcommand(new CommandDataParameters());
        addSubcommand(new CommandEventHandlers());
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
