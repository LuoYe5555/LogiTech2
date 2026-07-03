package me.matl114.logitech.core.Machines.SpecialMachines;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

public class OptimizedGeoQuarry {
    
    private static final Random RANDOM = new Random();
    
    private static final Map<String, List<ItemStack>> cachedGeoResources = new HashMap<>();
    private static volatile boolean geoResourcesCached = false;
    
    public static void optimizeTickMethod(Class<?> geoQuarryClass) {
        try {
            Method originalTick = geoQuarryClass.getDeclaredMethod("tick", Block.class);
            originalTick.setAccessible(true);
            
            Method optimizedTick = OptimizedGeoQuarry.class.getDeclaredMethod("optimizedTick", Block.class, geoQuarryClass);
            optimizedTick.setAccessible(true);
            
            replaceMethod(geoQuarryClass, "tick", optimizedTick);
        } catch (Exception e) {
        }
    }
    
    private static void replaceMethod(Class<?> targetClass, String methodName, Method replacement) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        
        Field methodsField = Class.class.getDeclaredField("methods");
        methodsField.setAccessible(true);
        Method[] methods = (Method[]) methodsField.get(targetClass);
        
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                methods[i] = replacement;
                methodsField.set(targetClass, methods);
                break;
            }
        }
    }
    
    public static void optimizedTick(Block b, Class<?> geoQuarryClass) {
        Location loc = b.getLocation();
        
        try {
            Object machine = BlockStorage.check(b);
            int energyPerTick = (int) geoQuarryClass.getDeclaredField("energyPerTick").get(machine);
            
            if (getCharge(loc) >= energyPerTick) {
                World world = b.getWorld();
                if (world == null) return;
                
                String worldType = getWorldType(world);
                List<ItemStack> resources = cachedGeoResources.getOrDefault(worldType, new ArrayList<>());
                
                if (resources.isEmpty() && !geoResourcesCached) {
                    cacheGeoResources();
                    resources = cachedGeoResources.getOrDefault(worldType, new ArrayList<>());
                }
                
                if (!resources.isEmpty()) {
                    ItemStack output = resources.get(RANDOM.nextInt(resources.size())).clone();
                    
                    BlockMenu menu = BlockStorage.getInventory(b);
                    if (menu != null) {
                        int[] outputSlots = (int[]) geoQuarryClass.getMethod("getOutputSlots").invoke(machine);
                        for (int slot : outputSlots) {
                            if (menu.getItemInSlot(slot) == null) {
                                menu.pushItem(output, outputSlots);
                                removeCharge(loc, energyPerTick);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    
    private static synchronized void cacheGeoResources() {
        if (geoResourcesCached) return;
        
        try {
            Class<?> resourceManagerClass = Class.forName("me.mrCookieSlime.Slimefun.api.ResourceManager");
            Object resourceManager = resourceManagerClass.getMethod("getInstance").invoke(null);
            java.util.Set<?> resources = (java.util.Set<?>) resourceManagerClass.getMethod("getAllResources").invoke(resourceManager);
            
            for (Object resource : resources) {
                try {
                    Class<?> geoResourceClass = Class.forName("me.mrCookieSlime.Slimefun.api.GEOResource");
                    if (geoResourceClass.isInstance(resource)) {
                        Object outputItem = geoResourceClass.getMethod("getItem").invoke(resource);
                        if (outputItem instanceof ItemStack) {
                            String worldType = (String) geoResourceClass.getMethod("getWorldType").invoke(resource);
                            String key = worldType.toLowerCase();
                            cachedGeoResources.computeIfAbsent(key, k -> new ArrayList<>()).add((ItemStack) outputItem);
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        
        geoResourcesCached = true;
    }
    
    private static String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NETHER: return "nether";
            case THE_END: return "the_end";
            default: return "normal";
        }
    }
    
    private static int getCharge(Location loc) {
        try {
            Class<?> energyNetClass = Class.forName("io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet");
            Object energyNet = energyNetClass.getMethod("getInstance").invoke(null);
            return (int) energyNetClass.getMethod("getCharge", Location.class).invoke(energyNet, loc);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static void removeCharge(Location loc, int charge) {
        try {
            Class<?> energyNetClass = Class.forName("io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet");
            Object energyNet = energyNetClass.getMethod("getInstance").invoke(null);
            energyNetClass.getMethod("removeCharge", Location.class, int.class).invoke(energyNet, loc, charge);
        } catch (Exception e) {}
    }
}