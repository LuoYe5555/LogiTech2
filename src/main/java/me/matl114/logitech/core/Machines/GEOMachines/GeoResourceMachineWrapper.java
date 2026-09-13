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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * 地理资源机器的包装类
 * 用于为 InfinityExpansion 的 GEO_QUARRY 提供显示配方
 */
public class GeoResourceMachineWrapper implements InvocationHandler {

    private final SlimefunItem originalItem;
    private final String itemId;

    public GeoResourceMachineWrapper(SlimefunItem originalItem) {
        this.originalItem = originalItem;
        this.itemId = originalItem.getId();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // 处理 getMachineRecipes 方法
        if (methodName.equals("getMachineRecipes")) {
            return provideDisplayRecipe();
        }

        // 处理 provideDisplayRecipe 方法
        if (methodName.equals("provideDisplayRecipe")) {
            return provideDisplayRecipe();
        }

        // 处理 getRecipeType 方法 - 返回原始物品的 RecipeType
        if (methodName.equals("getRecipeType")) {
            return originalItem.getRecipeType();
        }

        // 处理 getItem 方法 - 返回原始物品的 ItemStack
        if (methodName.equals("getItem")) {
            return ReflectUtils.invokeGetRecursively(originalItem, Settings.FIELD, "item");
        }

        // 处理 isObtainableFromGEOMiner 方法
        if (methodName.equals("isObtainableFromGEOMiner")) {
            return true;
        }

        // 其他方法直接调用原始对象的方法
        return method.invoke(originalItem, args);
    }

    /**
     * 提供地理资源显示配方
     */
    public List<MachineRecipe> provideDisplayRecipe() {
        List<MachineRecipe> recipes = new ArrayList<>();

        try {
            int ticks = 0;
            try {
                Integer ticksPerOutput = (Integer) ReflectUtils.invokeGetRecursively(
                        originalItem, Settings.FIELD, "ticksPerOutput");
                if (ticksPerOutput != null) {
                    ticks = ticksPerOutput - 1; // 转换为配方 tick
                }
            } catch (Throwable e) {
                ticks = 0;
            }

            // 从 GEO_MINER RecipeType 获取配方
            List<MachineRecipe> geoRecipes = RecipeSupporter.PROVIDED_SHAPED_RECIPES.get(RecipeType.GEO_MINER);
            if (geoRecipes != null && !geoRecipes.isEmpty()) {
                for (MachineRecipe recipe : geoRecipes) {
                    // 创建一个空的输入配方，输出为 GEO_MINER 的全部产物
                    recipes.add(MachineRecipeUtils.mgFrom(ticks, new ItemStack[0], recipe.getOutput()));
                }
            }
        } catch (Throwable e) {
            Debug.logger("Error in loading Slimefun Item %s GeoMachineRecipe: %s"
                    .formatted(itemId, e.getMessage()));
        }

        return recipes;
    }

    /**
     * 创建包装实例
     */
    public static SlimefunItem createWrapper(SlimefunItem originalItem) {
        return (SlimefunItem) Proxy.newProxyInstance(
                originalItem.getClass().getClassLoader(),
                new Class<?>[]{SlimefunItem.class},
                new GeoResourceMachineWrapper(originalItem));
    }
}
