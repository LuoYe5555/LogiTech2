// RecipeMachine.java
package me.matl114.logitech.core.Machines.AutoMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import me.matl114.logitech.core.Machines.Abstracts.AbstractRecipeMachine;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 具体配方机器实现 - 严格遵循 6×9 GUI 布局
 * slot 0-2: 灰色 | 3,5: 蓝色 | 4: 输入 (由 Slimefun 自动处理) | 6-8: 灰色
 * slot 9-12,14-17: 橙色 | 13: 状态槽
 * slot 18-53: 全部输出槽
 */
public class RecipeMachine extends AbstractRecipeMachine {

    public RecipeMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int time,
            int energyBuffer,
            int energyConsumption,
            List<Pair<Object, Integer>> customRecipes) {
        super(itemGroup, item, recipeType, recipe, time, energyBuffer, energyConsumption, customRecipes);
    }

    // 创建带有显示名称的物品，自动处理颜色代码
    private ItemStack createItemStack(Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // ✅ 修复颜色代码：将 &x 转为 §x
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if (lore.length > 0) {
                List<String> translatedLore = new ArrayList<>();
                for (String line : lore) {
                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(translatedLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void constructMenu(BlockMenuPreset preset) {
        preset.setSize(54); // 6×9

        // 第1行：0-8
        for (int i = 0; i <= 2; i++) {
            preset.addItem(i, createItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), (p, s, is, ca) -> false);
        }
        preset.addItem(3, createItemStack(Material.BLUE_STAINED_GLASS_PANE, "&3输入区"), (p, s, is, ca) -> false);

        // ✅ 修复输入槽：完全不在此处定义！只在 getInputSlots() 中声明，让 Slimefun 自动处理
        // preset.addItem(4, null, (p, s, is, ca) -> false); // <-- 移除此行

        preset.addItem(5, createItemStack(Material.BLUE_STAINED_GLASS_PANE, "&3输入区"), (p, s, is, ca) -> false);
        for (int i = 6; i <= 8; i++) {
            preset.addItem(i, createItemStack(Material.GRAY_STAINED_GLASS_PANE, " "), (p, s, is, ca) -> false);
        }

        // 第2行：9-17
        for (int i = 9; i <= 12; i++) {
            preset.addItem(i, createItemStack(Material.ORANGE_STAINED_GLASS_PANE, " "), (p, s, is, ca) -> false);
        }
        preset.addItem(13, getInfoIdle(), (p, s, is, ca) -> false); // 状态槽（颜色已修复）
        for (int i = 14; i <= 17; i++) {
            preset.addItem(i, createItemStack(Material.ORANGE_STAINED_GLASS_PANE, " "), (p, s, is, ca) -> false);
        }

        // 第3-6行：18-53 → 输出槽
        for (int i = 18; i <= 53; i++) {
            preset.addItem(i, createItemStack(Material.AIR, "&6输出槽"), (p, s, is, ca) -> false);
        }
    }

    // getInputSlots() 和 getOutputSlots() 已由父类 AbstractRecipeMachine 实现
}