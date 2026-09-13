package me.matl114.logitech.utils.UtilClass.StorageClass;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface LocationProxy {
    public ItemStack getItemStack(Location loc);

    public long getAmount(Location loc);

    public void setAmount(Location loc, long amount);

    public long getMaxAmount(Location loc);

    public Location getLocation(ItemMeta meta);

    public void updateLocation(Location loc);

    public default ItemStack getItemStack(Location loc, ItemStack hint) {
        return getItemStack(loc);
    }

    public default long getItemAmount(Location loc, ItemStack item) {
        return getAmount(loc);
    }

    public default void setItemAmount(Location loc, ItemStack item, long amount) {
        setAmount(loc, amount);
    }

    public default long getItemMaxAmount(Location loc, ItemStack item) {
        return getMaxAmount(loc);
    }

    public default boolean canAcceptItem(Location loc, ItemStack item) {
        ItemStack stored = getItemStack(loc);
        return stored == null || stored.isSimilar(item);
    }
}
