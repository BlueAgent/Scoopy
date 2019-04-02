package scoopy.client.command;

import gregtech.api.worldgen.config.WorldGenRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import scoopy.Scoopy;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandTreeGregtech extends CommandTreeBase implements IScoopyClientCommand {
    public CommandTreeGregtech() {
        addSubcommand(new CommandVeins());
        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName() {
        return "gregtech";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "GregTechCE mod related commands";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private static class CommandVeins extends CommandBase implements IScoopyClientCommand {
        @Override
        public String getName() {
            return "veins";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "Dump all the types of veins";
        }

        private static class BiomeWeight {
            public final Biome biome;
            public final int weight;

            private BiomeWeight(Biome biome, int weight) {
                this.biome = biome;
                this.weight = weight;
            }
        }

        public String describeBiomeSet(Set<Biome> biomes) {
            // Get all the types that include the given biomes
            Set<BiomeDictionary.Type> potentialTypes = biomes.stream()
                    .map(BiomeDictionary::getTypes)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            // Keep only the types that are a sub set
            Set<BiomeDictionary.Type> matchingTypes = potentialTypes.stream()
                    .filter((type) -> biomes.containsAll(BiomeDictionary.getBiomes(type)))
                    .collect(Collectors.toSet());
            // TODO: Filter out redundant types? Ones that are subtypes of the existing types
            // Set of biomes that are covered by the types
            Set<Biome> typeMatchedBiomes = matchingTypes.stream()
                    .map(BiomeDictionary::getBiomes)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            // Set of biomes that aren't coverted by the types
            Set<Biome> typeUnmatchedBiomes = new HashSet<>(biomes);
            typeUnmatchedBiomes.removeAll(typeMatchedBiomes);

            StringBuilder build = new StringBuilder();
            // Types first
            matchingTypes.stream()
                    .sorted(Comparator.comparing(BiomeDictionary.Type::getName))
                    .forEachOrdered((type) -> build.append(type.getName()).append('|'));
            // Other Biomes after
            typeUnmatchedBiomes.stream()
                    .sorted(Comparator.comparing(IForgeRegistryEntry.Impl::getRegistryName))
                    .forEachOrdered((biome) -> build.append(biome.getRegistryName()).append('|'));
            if(build.length() > 0)
                build.setLength(build.length() - 1);
            return build.toString();
        }

        public String describeBiomeWeights(Function<Biome, Integer> biomeWeight) {
            StringBuilder build = new StringBuilder();
            // Get the biomes that have non-zero weights
            Map<Integer, Set<Biome>> weightsToBiomes = GameRegistry.findRegistry(Biome.class).getValuesCollection().stream()
                    .map((biome) -> new BiomeWeight(biome, biomeWeight.apply(biome)))
                    .filter((bw) -> bw.weight > 0)
                    .collect(Collectors.toMap(
                            (bw)->bw.weight,
                            (bw)->{HashSet<Biome> single = new HashSet<>(); single.add(bw.biome); return single; },
                            (a, b)-> {a.addAll(b); return a; }
                    ));
            weightsToBiomes.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEachOrdered((entry)-> {
                int weight = entry.getKey();
                Set<Biome> biomes = entry.getValue();
                build.append(weight).append('*')
                        .append(biomes.size()).append('[')
                        .append(describeBiomeSet(biomes))
                        .append("],");
            });
            if(build.length() > 0)
                build.setLength(build.length() - 1);
            else
                build.append("ANY");
            return build.toString();
        }

        public String describeDimensionFilter(Predicate<WorldProvider> dimensionPredicate) {
            StringBuilder build = new StringBuilder();
            // TODO: Implement
            DimensionManager.getRegisteredDimensions();
            return build.toString();
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            StringBuilder build = new StringBuilder();
            build.append("Name\tWeight\tDensity\tIsVein\tMin Height\tMax Height\tBiome Weight Modifier");

            WorldGenRegistry.getOreDeposits().forEach((deposit) -> {
                build.append('\n');
                build.append(deposit.getDepositName());
                build.append('\t');
                build.append(deposit.getWeight());
                build.append('\t');
                build.append(deposit.getDensity());
                build.append('\t');
                build.append(deposit.isVein());
                build.append('\t');
                build.append(deposit.getMinimumHeight());
                build.append('\t');
                build.append(deposit.getMaximumHeight());
                build.append('\t');
                build.append(describeBiomeWeights(deposit.getBiomeWeightModifier()));
                //build.append('\t');
                //build.append(describeDimensionFilter(deposit.getDimensionFilter()));
            });

            Scoopy.log.info(build);
            sender.sendMessage(new TextComponentString("Vein information dumped to logs."));
        }
    }
}
