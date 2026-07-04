package me.matl114.logitech.core.Registries;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.matl114.logitech.MyAddon;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Machines.AutoMachines.StackMachine;
import me.matl114.logitech.utils.MachineRecipeUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Bukkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogiTechStackSupport {
    private static final Set<String> LOGITECH_GENERATORS = new HashSet<>();
    private static final Set<String> LOGITECH_MACHINES = new HashSet<>();

    static {
        LOGITECH_GENERATORS.add("BUG_WHOLESALE");
        LOGITECH_GENERATORS.add("PARADOX_WHOLESALE");
        LOGITECH_GENERATORS.add("BOOL_MG");
        LOGITECH_GENERATORS.add("REVERSE_GENERATOR");
        LOGITECH_GENERATORS.add("MAGIC_STONE");
        LOGITECH_GENERATORS.add("OVERWORLD_MINER");
        LOGITECH_GENERATORS.add("NETHER_MINER");
        LOGITECH_GENERATORS.add("END_MINER");
        LOGITECH_GENERATORS.add("DIMENSION_MINER");
        LOGITECH_GENERATORS.add("STONE_FACTORY");
        LOGITECH_GENERATORS.add("FINAL_STONE_MG");
        LOGITECH_GENERATORS.add("VIRTUAL_MINER");
        LOGITECH_GENERATORS.add("VIRTUAL_PLANT");
        LOGITECH_GENERATORS.add("MAGIC_PLANT");
        LOGITECH_GENERATORS.add("OVERWORLD_PLANT");
        LOGITECH_GENERATORS.add("NETHER_PLANT");
        LOGITECH_GENERATORS.add("END_PLANT");

        LOGITECH_MACHINES.add("SOLAR_REACTOR_SIMULATOR");
        LOGITECH_MACHINES.add("VIRTUAL_KILLER");
        LOGITECH_MACHINES.add("FINAL_VIRTUAL_KILLER");
    }

    public static boolean isLogiTechGenerator(SlimefunItem item) {
        return item != null && LOGITECH_GENERATORS.contains(item.getId());
    }

    public static boolean isLogiTechMachine(SlimefunItem item) {
        return item != null && LOGITECH_MACHINES.contains(item.getId());
    }

    public static void registerLogiTechStackable(SlimefunItem item) {
        if (item == null) return;

        String id = item.getId();
        if (LOGITECH_GENERATORS.contains(id)) {
            int energy = RecipeSupporter.tryGetMachineEnergy(item);
            RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
            RecipeSupporter.STACKMACHINE_LIST.remove(item);
            RecipeSupporter.STACKMGENERATOR_LIST.put(item, energy);
        } else if (LOGITECH_MACHINES.contains(id)) {
            int energy = RecipeSupporter.tryGetMachineEnergy(item);
            RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
            RecipeSupporter.STACKMACHINE_LIST.remove(item);
            RecipeSupporter.STACKMACHINE_LIST.put(item, energy);
        }
    }

    public static void registerAllLogiTechStackable() {
        Bukkit.getScheduler().runTaskLater(MyAddon.getInstance(), () -> {
            for (String id : LOGITECH_GENERATORS) {
                SlimefunItem item = SlimefunItem.getById(id);
                if (item != null) {
                    int energy = RecipeSupporter.tryGetMachineEnergy(item);
                    RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
                    RecipeSupporter.STACKMACHINE_LIST.remove(item);
                    RecipeSupporter.STACKMGENERATOR_LIST.put(item, energy);
                }
            }
            for (String id : LOGITECH_MACHINES) {
                SlimefunItem item = SlimefunItem.getById(id);
                if (item != null && !LOGITECH_GENERATORS.contains(id)) {
                    int energy = RecipeSupporter.tryGetMachineEnergy(item);
                    RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
                    RecipeSupporter.STACKMACHINE_LIST.remove(item);
                    RecipeSupporter.STACKMACHINE_LIST.put(item, energy);
                }
            }
            
            for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
                if (item instanceof AbstractMachine) {
                    try {
                        Method getMachineRecipesMethod = item.getClass().getMethod("getMachineRecipes");
                        Object result = getMachineRecipesMethod.invoke(item);
                        if (result instanceof List) {
                            List<MachineRecipe> recipes = (List<MachineRecipe>) result;
                            if (!recipes.isEmpty()) {
                                if (!RecipeSupporter.MACHINE_RECIPELIST.containsKey(item)) {
                                    List<MachineRecipe> resultRecipes = new java.util.ArrayList<>();
                                    for (MachineRecipe machineRecipe : recipes) {
                                        MachineRecipe res = MachineRecipeUtils.stackFromMachine(machineRecipe);
                                        resultRecipes.add(res);
                                    }
                                    RecipeSupporter.MACHINE_RECIPELIST.put(item, resultRecipes);
                                }
                                
                                int energy = RecipeSupporter.tryGetMachineEnergy(item);
                                RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
                                RecipeSupporter.STACKMACHINE_LIST.remove(item);
                                RecipeSupporter.STACKMACHINE_LIST.put(item, energy);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            
            updateStackMachineList();
        }, 1);
    }
    
    public static void registerLogiTechStackableDirectly() {
        for (String id : LOGITECH_GENERATORS) {
            SlimefunItem item = SlimefunItem.getById(id);
            if (item != null) {
                int energy = RecipeSupporter.tryGetMachineEnergy(item);
                RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
                RecipeSupporter.STACKMACHINE_LIST.remove(item);
                RecipeSupporter.STACKMGENERATOR_LIST.put(item, energy);
            }
        }
        for (String id : LOGITECH_MACHINES) {
            SlimefunItem item = SlimefunItem.getById(id);
            if (item != null && !LOGITECH_GENERATORS.contains(id)) {
                int energy = RecipeSupporter.tryGetMachineEnergy(item);
                RecipeSupporter.STACKMGENERATOR_LIST.remove(item);
                RecipeSupporter.STACKMACHINE_LIST.remove(item);
                RecipeSupporter.STACKMACHINE_LIST.put(item, energy);
            }
        }
    }
    
    private static void updateStackMachineList() {
        try {
            Field bwListField = StackMachine.class.getDeclaredField("BW_LIST");
            bwListField.setAccessible(true);
            List<SlimefunItem> bwList = (List<SlimefunItem>) bwListField.get(null);
            
            Field bwSizeField = StackMachine.class.getDeclaredField("BWSIZE");
            bwSizeField.setAccessible(true);
            
            Field bwEnergyField = StackMachine.class.getDeclaredField("BW_LIST_ENERGYCOMSUME");
            bwEnergyField.setAccessible(true);
            
            Field hasInitField = StackMachine.class.getDeclaredField("hasInit");
            hasInitField.setAccessible(true);
            
            synchronized (bwList) {
                bwList.clear();
                int size = RecipeSupporter.STACKMACHINE_LIST.size();
                bwSizeField.setInt(null, size);
                
                int[] energyConsume = new int[size];
                int i = 0;
                for (Map.Entry<SlimefunItem, Integer> e : RecipeSupporter.STACKMACHINE_LIST.entrySet()) {
                    bwList.add(e.getKey());
                    energyConsume[i] = e.getValue();
                    ++i;
                }
                bwEnergyField.set(null, energyConsume);
                hasInitField.setBoolean(null, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}