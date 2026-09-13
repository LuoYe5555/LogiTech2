package me.matl114.logitech.core.Machines.GEOMachines;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.matl114.logitech.core.Registries.AddDepends;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.utils.Debug;
import me.matl114.logitech.utils.MachineRecipeUtils;
import me.matl114.logitech.utils.ReflectUtils;
import me.matl114.logitech.utils.Settings;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 地理资源矿机的配方提供器
 */
public class GeoResourceMachines {

    /**
     * 为地理资源矿机提供配方
     */
    public List<MachineRecipe> provideDisplayRecipe() {
        List<MachineRecipe> recipes = new ArrayList<>();

        // 从 GEO_MINER RecipeType 获取配方
        try {
            List<MachineRecipe> geoRecipes = RecipeSupporter.PROVIDED_SHAPED_RECIPES.get(RecipeType.GEO_MINER);
            if (geoRecipes != null && !geoRecipes.isEmpty()) {
                int ticksPerOutput = 0;
                try {
                    Integer ticks = (Integer) ReflectUtils.invokeGetRecursively(
                            this, Settings.FIELD, "ticksPerOutput");
                    if (ticks != null) {
                        ticksPerOutput = ticks - 1; // 转换为配方 tick
                    }
                } catch (Throwable e) {
                    ticksPerOutput = 0;
                }

                for (MachineRecipe recipe : geoRecipes) {
                    // 创建一个空的输入配方，输出为 GEO_MINER 的全部产物
                    recipes.add(MachineRecipeUtils.mgFrom(ticksPerOutput, new ItemStack[0], recipe.getOutput()));
                }
            }
        } catch (Throwable e) {
            Debug.logger("Error in loading Slimefun Item GeoMachineRecipe: %s"
                    .formatted(e.getMessage()));
        }

        return recipes;
    }

    /**
     * 判断是否是本附属的地理资源矿机
     */
    public static boolean isGeoMachine(SlimefunItem item) {
        return item == AddDepends.INFINITY_GEOQURRY || item == AddDepends.ANNIHILATION_GEOQURRY;
    }
}
