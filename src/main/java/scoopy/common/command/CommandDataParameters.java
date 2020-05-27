package scoopy.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.GameData;
import org.apache.commons.lang3.StringUtils;
import scoopy.Scoopy;
import scoopy.client.command.IScoopyClientCommand;
import scoopy.common.util.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDataParameters extends CommandBase implements IScoopyClientCommand {
    // net.minecraft.network.datasync.EntityDataManager.NEXT_ID_MAP
    private static final Map<Class<? extends Entity>, Integer> NEXT_ID_MAP = ReflectionUtils.getField(
            ObfuscationReflectionHelper.findField(
            EntityDataManager.class, "field_187232_a"), null);

    @Override
    public String getName() {
        return "data_parameters";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Checks entity Data Parameters for potential problems";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final Set<Class<? extends Entity>> entityClasses = new HashSet<>();
        final Map<Class<? extends Entity>, Set<Class<? extends Entity>>> entityChildren = new HashMap<>();

        GameData.getEntityRegistry().getValuesCollection().stream().map(EntityEntry::getEntityClass).forEach(entityType -> {
            Class<? extends Entity> current = entityType;
            while (!entityClasses.contains(current)) {
                entityClasses.add(current);
                // Try to force initialization (by accessing a field)
                Arrays.stream(current.getDeclaredFields())
                        .filter(f -> Modifier.isStatic(f.getModifiers()))
                        .findFirst()
                        .ifPresent(f -> {
                            try {
                                f.setAccessible(true);
                                f.get(null);
                            } catch (IllegalAccessException e) {
                                // Ignore
                            }
                        });
                // Update map of parent -> children
                final Class<?> maybeSuperclass = current.getSuperclass();
                if (Entity.class.isAssignableFrom(maybeSuperclass)) {
                    final Class<? extends Entity> superclass = maybeSuperclass.asSubclass(Entity.class);
                    // Add current class as the child
                    entityChildren
                            .computeIfAbsent(superclass, (k) -> new HashSet<>())
                            .add(current);
                    current = superclass;
                } else {
                    break;
                }
            }
        });

        int maxDepth = 0;
        int errors = 0;
        final String tab = "  ";
        final StringBuilder build = new StringBuilder();
        build.append("<max>/<delta>/<expected delta>[ !!!!] <class name>\n");
        final Stack<Element> stack = new Stack<>();
        stack.push(new Element(Entity.class, 0, -1));
        while (!stack.isEmpty()) {
            final Element curr = stack.pop();
            final Class<? extends Entity> current = curr.clazz;
            final int depth = curr.depth;
            maxDepth = Math.max(depth, maxDepth);
            final int parentMaxId = curr.parentMaxId;
            build.append(StringUtils.repeat(tab, depth));
            int tempMaxId = NEXT_ID_MAP.get(Entity.class);
            Class<?> clazz = current;
            while (clazz != Entity.class) {
                if (NEXT_ID_MAP.containsKey(clazz)) {
                    tempMaxId = NEXT_ID_MAP.get(clazz);
                    break;
                }
                clazz = clazz.getSuperclass();
            }
            final int maxId = tempMaxId;
            final int deltaIds = maxId - parentMaxId;
            final int expectedDeltaIds = Arrays.stream(current.getDeclaredFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()))
                    .flatMap(f -> {
                        final Class<?> type = f.getType();
                        // Attempt to convert into data parameter instances
                        if (DataParameter.class.isAssignableFrom(type))
                            return Stream.of((DataParameter) ReflectionUtils.getField(f, null));
                        else if (type.isArray() && DataParameter.class.isAssignableFrom(type.getComponentType()))
                            return Stream.of((DataParameter[]) ReflectionUtils.getField(f, null));
                        else if (Collection.class.isAssignableFrom(type))
                            return ((Collection<?>) ReflectionUtils.getField(f, null)).stream()
                                    .filter(DataParameter.class::isInstance)
                                    .map(DataParameter.class::cast);
                        else return Stream.empty();
                    })
                    .collect(Collectors.toSet())
                    .size();
            // Print information
            build.append(maxId).append("/").append(deltaIds).append("/").append(expectedDeltaIds);
            if (deltaIds != expectedDeltaIds) {
                build.append(" !!!!");
                errors++;
            }
            build.append(" ").append(current.getName()).append("\n");
            // Queue up the children
            if (entityChildren.containsKey(current)) {
                entityChildren.get(current).stream()
                        //.sorted(Comparator.comparing(Class::getName))
                        .sorted((a, b) -> {
                            final String nameA = a.getName();
                            final String nameB = b.getName();
                            boolean mcA = nameA.startsWith("net.minecraft");
                            boolean mcB = nameB.startsWith("net.minecraft");
                            if (mcA) {
                                return mcB ? nameA.compareTo(nameB) : -1;
                            } else {
                                return mcB ? 1 : nameA.compareTo(nameB);
                            }
                        })
                        .forEachOrdered(child -> stack.add(new Element(
                                child,
                                depth + 1,
                                maxId
                        )));
            }
        }

        String message = errors + " potential error(s) found out of " + entityClasses.size() + " entity classes with a max depth of " + maxDepth + ".";
        Scoopy.log.info(message + "\n" + build.toString());
        sender.sendMessage(new TextComponentString(message + " See logs for details."));
    }

    private static class Element {
        public final Class<? extends Entity> clazz;
        public final int depth;
        public final int parentMaxId;

        private Element(Class<? extends Entity> clazz, int depth, int parentMaxId) {
            this.clazz = clazz;
            this.depth = depth;
            this.parentMaxId = parentMaxId;
        }
    }
}
