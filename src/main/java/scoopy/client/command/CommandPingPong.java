package scoopy.client.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandPingPong extends CommandBase implements IScoopyClientCommand {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Replies with pong";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender.sendMessage(new TextComponentString("pong"));
    }
}
