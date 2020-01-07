package scoopy.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import scoopy.Scoopy;
import scoopy.client.command.IScoopyClientCommand;
import scoopy.common.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandEventHandlers extends CommandBase implements IScoopyClientCommand {
    private static final Field eventBus_listeners = ReflectionUtils.getField(EventBus.class, "listeners");
    private static final Field eventBus_listenerOwners = ReflectionUtils.getField(EventBus.class, "listenerOwners");
    private static final Field eventBus_busID = ReflectionUtils.getField(EventBus.class, "busID");

    private static final List<EventBusRef> EVENT_BUS_REFS = Stream.of(
            EventBusRef.get("EVENT", MinecraftForge.EVENT_BUS),
            EventBusRef.get("ORE_GEN", MinecraftForge.ORE_GEN_BUS),
            EventBusRef.get("TERRAIN_GEN", MinecraftForge.TERRAIN_GEN_BUS)
    ).collect(Collectors.toList());

    @Override
    public String getName() {
        return "event_handlers";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "<event class> - Prints handlers registered to the given event";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) throw new WrongUsageException("Missing class param");

        final Event event;
        try {
            final Class<?> clazz = Class.forName(args[0]);
            final Class<? extends Event> eventType;
            if (Event.class.isAssignableFrom(clazz)) {
                eventType = clazz.asSubclass(Event.class);
            } else {
                throw new CommandException("Class not of type Event?: " + args[0]);
            }
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            event = (Event) ctr.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CommandException("Could not find class: " + args[0]);
        } catch (NoSuchMethodException e) {
            throw new CommandException("Event has no valid constructor?: " + args[0]);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new CommandException("Failed to get event instance: " + args[0]);
        }
        List<String> listenerDescriptions = EVENT_BUS_REFS.stream().flatMap(busRef ->
                Arrays.stream(event.getListenerList().getListeners(busRef.busId))
                        .map(listener -> busRef.name + ": " + listener)
        ).collect(Collectors.toList());
        String result = Stream.concat(
                Stream.of(listenerDescriptions.size() + " Event Listeners for " + args[0] + ":"),
                listenerDescriptions.stream()
        ).collect(Collectors.joining("\n"));
        Scoopy.log.info(result);
        sender.sendMessage(new TextComponentString(
                "Found " + listenerDescriptions.size() + "listeners. Event information dumped to logs."));
    }

    private static final class EventBusRef {
        public final String name;
        public final EventBus bus;
        public final ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;
        public final Map<Object, ModContainer> listenerOwners;
        public final int busId;

        private EventBusRef(String name, EventBus bus, ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners, Map<Object, ModContainer> listenerOwners, int busId) {
            this.name = name;
            this.bus = bus;
            this.listeners = listeners;
            this.listenerOwners = listenerOwners;
            this.busId = busId;
        }

        public static EventBusRef get(String name, EventBus bus) {
            final ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;
            final Map<Object, ModContainer> listenerOwners;
            final int busId;
            try {
                //noinspection unchecked
                listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) eventBus_listeners.get(bus);
                //noinspection unchecked
                listenerOwners = (Map<Object, ModContainer>) eventBus_listenerOwners.get(bus);
                busId = (Integer) eventBus_busID.get(bus);
            } catch (IllegalAccessException | ClassCastException e) {
                throw new RuntimeException("Failed to get private fields from EventBus", e);
            }
            return new EventBusRef(name, bus, listeners, listenerOwners, busId);
        }
    }
}
