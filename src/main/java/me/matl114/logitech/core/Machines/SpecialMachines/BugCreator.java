package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.Random;
import javax.annotation.Nonnull;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BugCreator extends AbstractMachine {
    
    private static final int[] INPUT_SLOT = {10};
    private static final int[] OUTPUT_SLOT = {14, 15, 16};
    private static final int[] BACKGROUND_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
    };
    
    private final Random random = new Random();

    public BugCreator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe, 5000, 1145);
    }

    @Override
    public void constructMenu(BlockMenuPreset preset) {
        for (int slot : BACKGROUND_SLOTS) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    @Override
    public void process(@Nonnull Block b, @Nonnull BlockMenu menu, @Nonnull SlimefunBlockData data) {
        if (!conditionHandle(b, menu)) {
            return;
        }

        ItemStack input = menu.getItemInSlot(INPUT_SLOT[0]);
        if (input == null || input.getType().isAir()) {
            return;
        }

        // Check if input is a BUG item
        SlimefunItem sfItem = SlimefunItem.getByItem(input);
        if (sfItem == null || !sfItem.getId().equals(AddItem.BUG.getItemId())) {
            return;
        }

        // Consume input
        if (input.getAmount() > 1) {
            input.setAmount(input.getAmount() - 1);
        } else {
            menu.replaceExistingItem(INPUT_SLOT[0], null);
        }

        // Consume energy
        progressorCost(b, menu);

        // Process output
        double rand = random.nextDouble();
        
        // 50% 概率产出2个BUG
        if (rand < 0.5) {
            ItemStack bugOutput = AddItem.BUG.clone();
            bugOutput.setAmount(2);
            menu.pushItem(bugOutput, OUTPUT_SLOT);
        } 
        // 50% 概率产出骨粉+书+命名牌
        else {
            menu.pushItem(new ItemStack(Material.BONE_MEAL), OUTPUT_SLOT);
            menu.pushItem(new ItemStack(Material.BOOK), OUTPUT_SLOT);
            menu.pushItem(new ItemStack(Material.NAME_TAG), OUTPUT_SLOT);
        }
    }
}