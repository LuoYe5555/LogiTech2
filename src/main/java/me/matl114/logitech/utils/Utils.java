package me.matl114.logitech.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Utils {
    public static void log(String... message) {
        Debug.logger(String.join(" ", message));
    }

    public static <T extends Object> List<T> list(T... objs) {
        return Arrays.stream(objs).collect(Collectors.toCollection(ArrayList::new));
    }

    public static <T extends Object> T[] array(T... objs) {
        return objs;
    }

    public static ItemStack[] toArray(List<ItemStack> list) {
        return list.toArray(new ItemStack[list.size()]);
    }

    public static ItemStack[] recipe(Object... v) {
        return Arrays.stream(v).map(AddUtils::resolveItem).toArray(ItemStack[]::new);
    }

    // === 1.21.x 跨版本兼容 helper ===
    // Material.CHAIN 在 1.21.9 改名为 IRON_CHAIN；Material.valueOf() 是 Enum 标准方法，编译安全
    public static Material chainMaterial() {
        try {
            return Material.valueOf("IRON_CHAIN");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return Material.valueOf("CHAIN");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("找不到 CHAIN/IRON_CHAIN material");
    }

    // EntityType.BOAT/CHEST_BOAT 在 1.21.4 拆分为 OAK_BOAT/OAK_CHEST_BOAT 等具体变种
    public static EntityType boatEntity() {
        try {
            return EntityType.valueOf("OAK_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return EntityType.valueOf("BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("找不到 OAK_BOAT/BOAT entity type");
    }

    public static EntityType chestBoatEntity() {
        try {
            return EntityType.valueOf("OAK_CHEST_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return EntityType.valueOf("CHEST_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("找不到 OAK_CHEST_BOAT/CHEST_BOAT entity type");
    }
}
