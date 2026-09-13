package me.matl114.logitech.core.Machines.ManualMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.ArrayList;
import java.util.List;
import me.matl114.logitech.core.Machines.Abstracts.AbstractManual;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.utils.UtilClass.RecipeClass.ImportRecipes;
import me.matl114.logitech.utils.Debug;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;

public class ManualCrafter extends AbstractManual implements ImportRecipes {
    public List<ItemStack> displayedMemory = null;
    protected final RecipeType[] craftType;

    public ManualCrafter(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption,
            RecipeType... craftType) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption, null);
        this.craftType = craftType;
        this.machineRecipeSupplier = () -> {
            try {
                // 确保 RecipeSupporter 已初始化
                RecipeSupporter.init();

                if (this.craftType == null || this.craftType.length <= 0) {
                    return new ArrayList<>();
                }

                List<MachineRecipe> recipes = new ArrayList<>();
                for (int i = 0; i < this.craftType.length; i++) {
                    RecipeType rt = this.craftType[i];
                    if (rt == null) {
                        continue;
                    }

                    List<MachineRecipe> typeRecipes = RecipeSupporter.PROVIDED_UNSHAPED_RECIPES.get(rt);

                    if (typeRecipes == null) {
                        continue;
                    }

                    recipes.addAll(typeRecipes);
                }

                return recipes;
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        };
        // 异步加载配方，确保 RecipeSupporter 已初始化
        PostSetupTasks.addPostRegisterTask(() -> {
            getMachineRecipes();
        });
    }
}
