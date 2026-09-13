package me.matl114.logitech.core.Depends;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.NotImplementedException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import me.matl114.logitech.core.Cargo.Storages;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.Debug;
import me.matl114.logitech.utils.UtilClass.StorageClass.LocationProxy;
import me.matl114.logitech.utils.UtilClass.StorageClass.StorageType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NetworksDrawerProxyAdapter extends StorageType implements LocationProxy {
    public static final NamespacedKey KEY_LOC = AddUtils.getNameKey("cache_loc");
    public static final String[] DRAWER_IDS = {
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_1",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_2",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_3",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_4",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_5",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_6",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_7",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_8",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_9",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_10",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_11",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_12",
            "NTW_EXPANSION_CARGO_STORAGE_UNIT_13"
    };
    public static final SlimefunItem INSTANCE = SlimefunItem.getById("NTW_EXPANSION_CARGO_STORAGE_UNIT_1");
    public final Map cacheMap;
    int ExceptionTimes = 0;

    {
        if (INSTANCE == null) {
            disableNetworkDrawer(new Exception("SlimefunItem instance NTW_EXPANSION_CARGO_STORAGE_UNIT_1 not found!"));
            throw new NotImplementedException("NetworksDrawer Instance not found");
        }
        cacheMap = getDrawerCacheMap(INSTANCE);
    }

    public NetworksDrawerProxyAdapter() {
        super();
    }

    private static Map getDrawerCacheMap(SlimefunItem itemInstance) {
        try {
            Class<?> drawerClass = Class.forName("com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer");
            Field field = drawerClass.getDeclaredField("storages");
            field.setAccessible(true);
            return (Map) field.get(null);
        } catch (Throwable e) {
            Debug.debug("invoke failed getDrawerCacheMap");
            return null;
        }
    }

    private boolean isDrawerLocation(Location loc) {
        if (cacheMap == null) return false;
        return cacheMap.containsKey(loc);
    }

    private Object getStorageUnitData(Location loc) {
        if (cacheMap == null) return null;
        return cacheMap.get(loc);
    }

    @Override
    public boolean isStorage(ItemMeta meta) {
        Location loc = getLocation(meta);
        if (loc == null) return false;
        return isDrawerLocation(loc);
    }

    @Override
    public boolean canStorage(ItemMeta meta) {
        return false;
    }

    @Override
    public boolean canStorage(SlimefunItem item) {
        return false;
    }

    @Override
    public void setStorage(ItemMeta meta, ItemStack content) {
        throw new NotImplementedException("Drawer's content shouldn't be set");
    }

    @Override
    public ItemStack getStorageContent(ItemMeta meta) {
        Location loc = getLocation(meta);
        if (loc == null) return null;
        return getItemStack(loc);
    }

    @Override
    public void clearStorage(ItemMeta meta) {
        throw new NotImplementedException("Drawer's content shouldn't be cleared");
    }

    @Override
    public long getStorageAmount(ItemMeta meta) {
        Location loc = getLocation(meta);
        if (loc == null) return 0;
        return getAmount(loc);
    }

    @Override
    public long getStorageMaxSize(ItemMeta meta) {
        Location loc = getLocation(meta);
        if (loc == null) return 0;
        return getMaxAmount(loc);
    }

    @Override
    public void onStorageAmountWrite(ItemMeta meta, long amount) {
        Location loc = getLocation(meta);
        if (loc == null) return;
        setAmount(loc, amount);
    }

    @Override
    public void onStorageDisplayWrite(ItemMeta meta, long amount) {
        Location loc = getLocation(meta);
        if (loc == null) return;
        updateLocation(loc);
    }

    @Override
    public boolean isStorageProxy() {
        return true;
    }

    @Override
    public ItemStack getItemStack(Location loc) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null) return null;

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                return null;
            }

            Object firstItem = storedItems.get(0);
            java.lang.reflect.Method getSampleMethod = firstItem.getClass().getDeclaredMethod("getSample");
            ItemStack sample = (ItemStack) getSampleMethod.invoke(firstItem);

            return sample != null ? sample.clone() : null;
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return null;
        }
    }

    @Override
    public long getAmount(Location loc) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null) return 0;

            java.lang.reflect.Method getTotalAmountLongMethod = storageData.getClass().getDeclaredMethod("getTotalAmountLong");
            return (Long) getTotalAmountLongMethod.invoke(storageData);
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return 0;
        }
    }

    @Override
    public void setAmount(Location loc, long amount) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null) return;

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                return;
            }

            long currentAmount = 0;
            for (Object itemObj : storedItems) {
                java.lang.reflect.Method getAmountMethod = itemObj.getClass().getDeclaredMethod("getAmount");
                int itemAmount = (Integer) getAmountMethod.invoke(itemObj);
                currentAmount += itemAmount;
            }

            if (currentAmount == amount) {
                return;
            }

            Object firstItem = storedItems.get(0);
            java.lang.reflect.Method getSampleMethod = firstItem.getClass().getDeclaredMethod("getSample");
            ItemStack sample = (ItemStack) getSampleMethod.invoke(firstItem);

            if (amount > currentAmount) {
                long addAmount = amount - currentAmount;
                java.lang.reflect.Method addStoredItemMethod = storageData.getClass().getDeclaredMethod("addStoredItem", ItemStack.class, int.class, boolean.class);
                while (addAmount > 0) {
                    int addBatch = (int) Math.min(addAmount, Integer.MAX_VALUE);
                    addStoredItemMethod.invoke(storageData, sample, addBatch, true);
                    addAmount -= addBatch;
                }
            } else {
                long removeAmount = currentAmount - amount;
                java.lang.reflect.Method getItemIdMethod = firstItem.getClass().getDeclaredMethod("getId");
                int itemId = (Integer) getItemIdMethod.invoke(firstItem);
                java.lang.reflect.Method removeAmountMethod = storageData.getClass().getDeclaredMethod("removeAmount", int.class, int.class);
                while (removeAmount > 0) {
                    int removeBatch = (int) Math.min(removeAmount, Integer.MAX_VALUE);
                    removeAmountMethod.invoke(storageData, itemId, removeBatch);
                    removeAmount -= removeBatch;
                }
            }
        } catch (Throwable e) {
            disableNetworkDrawer(e);
        }
    }

    @Override
    public ItemStack getItemStack(Location loc, ItemStack hint) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null || hint == null) return getItemStack(loc);

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                return null;
            }

            for (Object itemObj : storedItems) {
                java.lang.reflect.Method getSampleMethod = itemObj.getClass().getDeclaredMethod("getSample");
                ItemStack sample = (ItemStack) getSampleMethod.invoke(itemObj);

                if (sample != null && sample.isSimilar(hint)) {
                    return sample.clone();
                }
            }

            return getItemStack(loc);
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return getItemStack(loc);
        }
    }

    @Override
    public long getItemAmount(Location loc, ItemStack item) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null || item == null) return getAmount(loc);

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                return 0;
            }

            for (Object itemObj : storedItems) {
                java.lang.reflect.Method getSampleMethod = itemObj.getClass().getDeclaredMethod("getSample");
                ItemStack sample = (ItemStack) getSampleMethod.invoke(itemObj);

                if (sample != null && sample.isSimilar(item)) {
                    java.lang.reflect.Method getAmountMethod = itemObj.getClass().getDeclaredMethod("getAmount");
                    return (Integer) getAmountMethod.invoke(itemObj);
                }
            }

            return 0;
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return 0;
        }
    }

    @Override
    public void setItemAmount(Location loc, ItemStack item, long amount) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null || item == null) {
                setAmount(loc, amount);
                return;
            }

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                java.lang.reflect.Method addStoredItemMethod = storageData.getClass().getDeclaredMethod("addStoredItem", ItemStack.class, int.class, boolean.class);
                ItemStack clone = item.clone();
                clone.setAmount(1);
                while (amount > 0) {
                    int addBatch = (int) Math.min(amount, Integer.MAX_VALUE);
                    addStoredItemMethod.invoke(storageData, clone, addBatch, true);
                    amount -= addBatch;
                }
                return;
            }

            boolean found = false;
            for (Object itemObj : storedItems) {
                java.lang.reflect.Method getSampleMethod = itemObj.getClass().getDeclaredMethod("getSample");
                ItemStack sample = (ItemStack) getSampleMethod.invoke(itemObj);

                if (sample != null && sample.isSimilar(item)) {
                    java.lang.reflect.Method getAmountMethod = itemObj.getClass().getDeclaredMethod("getAmount");
                    int currentAmount = (Integer) getAmountMethod.invoke(itemObj);

                    if (currentAmount == amount) {
                        return;
                    }

                    if (amount > currentAmount) {
                        long addAmount = amount - currentAmount;
                        java.lang.reflect.Method addStoredItemMethod = storageData.getClass().getDeclaredMethod("addStoredItem", ItemStack.class, int.class, boolean.class);
                        while (addAmount > 0) {
                            int addBatch = (int) Math.min(addAmount, Integer.MAX_VALUE);
                            addStoredItemMethod.invoke(storageData, sample, addBatch, true);
                            addAmount -= addBatch;
                        }
                    } else {
                        long removeAmount = currentAmount - amount;
                        java.lang.reflect.Method getItemIdMethod = itemObj.getClass().getDeclaredMethod("getId");
                        int itemId = (Integer) getItemIdMethod.invoke(itemObj);
                        java.lang.reflect.Method removeAmountMethod = storageData.getClass().getDeclaredMethod("removeAmount", int.class, int.class);
                        while (removeAmount > 0) {
                            int removeBatch = (int) Math.min(removeAmount, Integer.MAX_VALUE);
                            removeAmountMethod.invoke(storageData, itemId, removeBatch);
                            removeAmount -= removeBatch;
                        }
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                java.lang.reflect.Method addStoredItemMethod = storageData.getClass().getDeclaredMethod("addStoredItem", ItemStack.class, int.class, boolean.class);
                ItemStack clone = item.clone();
                clone.setAmount(1);
                while (amount > 0) {
                    int addBatch = (int) Math.min(amount, Integer.MAX_VALUE);
                    addStoredItemMethod.invoke(storageData, clone, addBatch, true);
                    amount -= addBatch;
                }
            }
        } catch (Throwable e) {
            disableNetworkDrawer(e);
        }
    }

    @Override
    public long getItemMaxAmount(Location loc, ItemStack item) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null) return getMaxAmount(loc);

            java.lang.reflect.Method getSizeTypeMethod = storageData.getClass().getDeclaredMethod("getSizeType");
            Object sizeType = getSizeTypeMethod.invoke(storageData);

            java.lang.reflect.Method getMaxAmountMethod = sizeType.getClass().getDeclaredMethod("getMaxAmount");
            Number maxAmount = (Number) getMaxAmountMethod.invoke(sizeType);

            return maxAmount.longValue();
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return Long.MAX_VALUE;
        }
    }

    @Override
    public boolean canAcceptItem(Location loc, ItemStack item) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null || item == null) return false;

            java.lang.reflect.Method getStoredItemsMethod = storageData.getClass().getDeclaredMethod("getStoredItems");
            List<?> storedItems = (List<?>) getStoredItemsMethod.invoke(storageData);

            if (storedItems == null || storedItems.isEmpty()) {
                return true;
            }

            for (Object itemObj : storedItems) {
                java.lang.reflect.Method getSampleMethod = itemObj.getClass().getDeclaredMethod("getSample");
                ItemStack sample = (ItemStack) getSampleMethod.invoke(itemObj);

                if (sample != null && sample.isSimilar(item)) {
                    return true;
                }
            }

            java.lang.reflect.Method getSizeTypeMethod = storageData.getClass().getDeclaredMethod("getSizeType");
            Object sizeType = getSizeTypeMethod.invoke(storageData);

            java.lang.reflect.Method getMaxTypeCountMethod = sizeType.getClass().getDeclaredMethod("getMaxTypeCount");
            Number maxTypeCount = (Number) getMaxTypeCountMethod.invoke(sizeType);

            return storedItems.size() < maxTypeCount.intValue();
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return false;
        }
    }

    @Override
    public long getMaxAmount(Location loc) {
        try {
            Object storageData = getStorageUnitData(loc);
            if (storageData == null) return 0;

            java.lang.reflect.Method getSizeTypeMethod = storageData.getClass().getDeclaredMethod("getSizeType");
            Object sizeType = getSizeTypeMethod.invoke(storageData);

            java.lang.reflect.Method getMaxAmountMethod = sizeType.getClass().getDeclaredMethod("getMaxAmount");
            Number maxAmount = (Number) getMaxAmountMethod.invoke(sizeType);

            java.lang.reflect.Method getMaxTypeCountMethod = sizeType.getClass().getDeclaredMethod("getMaxTypeCount");
            Number maxTypeCount = (Number) getMaxTypeCountMethod.invoke(sizeType);

            return maxAmount.longValue() * maxTypeCount.longValue();
        } catch (Throwable e) {
            disableNetworkDrawer(e);
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Location getLocation(ItemMeta meta) {
        String locstr = meta.getPersistentDataContainer().get(KEY_LOC, PersistentDataType.STRING);
        if (locstr == null) return null;
        return DataCache.locationFromString(locstr);
    }

    @Override
    public void updateLocation(Location loc) {
        try {
            Class<?> drawerClass = Class.forName("com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer");
            java.lang.reflect.Method updateMethod = drawerClass.getDeclaredMethod("update", Location.class, boolean.class);
            updateMethod.invoke(null, loc, true);
        } catch (Throwable e) {
            disableNetworkDrawer(e);
        }
    }

    private void disableNetworkDrawer(Throwable e) {
        Debug.logger("AN ERROR OCCURED IN NETWORK_DRAWER_PROXY, STORAGE TYPE MAY BE DISABLED %d/%d"
                .formatted(ExceptionTimes, 100));
        Debug.logger(e);
        ExceptionTimes++;
        if (ExceptionTimes > 100) {
            disableStorageType(this);
        }
    }
}