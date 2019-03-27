package scoopy.client.command;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import scoopy.Scoopy;
import scoopy.common.util.ItemWithMetadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandDumpNutrition extends CommandBase implements IScoopyClientCommand {
    @Override
    public String getName() {
        return "dump_nutrition";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Dump food nutritional information";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        StringBuilder build = new StringBuilder();
        build.append("ID\tName\tFood Value\tSaturation\tNutrition Value");
        // Nutrient Headers
        List<Nutrient> allNutrients = new ArrayList<>(NutrientList.get());
        allNutrients.sort((a, b) -> a.name.compareTo(b.name));
        allNutrients.forEach((nutrient -> build.append('\t').append(nutrient.name)));

        Set<ItemWithMetadata> foodStacks = allNutrients.stream()
                .flatMap((nutrient) -> nutrient.foodItems.stream())
                .map(ItemWithMetadata::new)
                .collect(Collectors.toSet());

        foodStacks.stream()
                .map(ItemWithMetadata::getStack)
                .filter(NutrientUtils::isValidFood)
                .forEach((stack) -> {
                    build.append('\n');
                    // ID
                    Item item = stack.getItem();
                    build.append(ForgeRegistries.ITEMS.getKey(item));
                    if(stack.getMetadata() != 0)
                        build.append(':').append(stack.getMetadata());
                    // Name
                    build.append('\t').append(stack.getDisplayName());
                    // Heal/Food value an saturation
                    if(item instanceof ItemFood) {
                        ItemFood food = (ItemFood) item;
                        int foodValue = food.getHealAmount(stack);
                        build.append('\t').append(foodValue);
                        // Actual saturation is food * saturation mod * 2
                        build.append('\t').append(foodValue * food.getSaturationModifier(stack) * 2.0f);
                    } else {
                        // Cake, milk or other
                        build.append('\t').append(-1);
                        build.append('\t').append(-1);
                    }

                    // Nutrient Value
                    List<Nutrient> nutrientList = NutrientUtils.getFoodNutrients(stack);
                    build.append('\t').append(NutrientUtils.calculateNutrition(stack, nutrientList));
                    // Nutrients
                    Set<Nutrient> stackNutrients = new HashSet<>(nutrientList);
                    allNutrients.forEach(nutrient ->
                            build.append('\t').append(stackNutrients.contains(nutrient) ? '1' : '0')
                    );
                });

        Scoopy.log.info(build);
    }
}
