package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Debug;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AntiLogicMolecularReconstructor extends AbstractMachine {
    protected static final int[] BORDER = new int[] {13, 22, 31, 40, 49};
    protected static final int[] INPUT_BORDER = new int[] {0, 1, 2, 3};
    protected static final int[] OUTPUT_BORDER = new int[] {5};
    protected static final int[] INPUT_SLOT = new int[] {
        9, 10, 11, 12,
        18, 19, 20, 21,
        27, 28, 29, 30,
        36, 37, 38, 39,
        45, 46, 47, 48
    };
    protected static final int[] OUTPUT_SLOT = new int[] {
        14, 15, 16, 17,
        23, 24, 25, 26,
        32, 33, 34, 35,
        41, 42, 43, 44,
        50, 51, 52, 53
    };
    
    protected static final int LOOP_SLOT = 7;
    protected static final int INFO_SLOT = 4;
    protected static final int CLEAR_SLOT = 6;
    protected static final int EMPTY_SLOT = 8;
    
    protected static final ItemStack LOOP_OFF =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&6循环模式", "&7状态: &c关闭", "&7开启后将输出转移回输入");
    protected static final ItemStack LOOP_ON =
            new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6循环模式", "&7状态: &a开启", "&7正在将输出转移回输入");
    
    protected static final ItemStack CLEAR_OFF =
            new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "&6清空模式", "&7状态: &c关闭", "&7点击清空输出槽");
    protected static final ItemStack CLEAR_ON =
            new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, "&6清空模式", "&7状态: &a开启", "&7输出槽已清空");
    
    protected static final ItemStack EMPTY_SLOT_ITEM =
            new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ");
    
    protected static final ItemStack INFO_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE,
            "&6机制说明",
            "&7将任意物品放入输入槽",
            "&7机器将该物品重组为随机粘液科技物品",
            "&7数量随机 (0-64)",
            "&7概率产出原版随机物品",
            "&e当输入物品和虚拟世界一起输入时",
            "&c小概率将输入物品填满输出槽",
            "&4极小概率导致机器熔毁");

    private final Random random = new Random();
    private final List<SlimefunItem> slimefunItems;

    public AntiLogicMolecularReconstructor(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);
        this.slimefunItems = new ArrayList<>();
        
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f机制 - &c分子重组",
                        "&7违反逻辑的机器",
                        "&7输入任意物品，将尝试将该物品重组为任意粘液科技物品",
                        "&7数量随机 (0-64)",
                        "&7概率产出原版随机物品"),
                null,
                AddUtils.getInfoShow(
                        "&f机制 - &c虚拟世界联动",
                        "&7当某物品和 1 个虚拟世界一起输入时",
                        "&c小概率将输入物品填满输出槽",
                        "&4极小概率导致机器熔毁"),
                AddUtils.getInfoShow(
                        "&f警告 - &c逻辑崩溃",
                        "&7这台机器违反了逻辑...",
                        "&7使用需谨慎")));
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
    public void constructMenu(BlockMenuPreset preset) {
        for (int slot : BORDER) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : INPUT_BORDER) {
            preset.addItem(slot, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : OUTPUT_BORDER) {
            preset.addItem(slot, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(CLEAR_SLOT, CLEAR_OFF);
        preset.addItem(LOOP_SLOT, LOOP_OFF);
        preset.addItem(EMPTY_SLOT, EMPTY_SLOT_ITEM, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newMenuInstance(BlockMenu menu, Block block) {
        super.newMenuInstance(menu, block);
        
        menu.addMenuClickHandler(CLEAR_SLOT, (player, i, itemStack, clickAction) -> {
            boolean isClearOn = itemStack != null && itemStack.getType() == Material.CYAN_STAINED_GLASS_PANE;
            if (isClearOn) {
                menu.replaceExistingItem(CLEAR_SLOT, CLEAR_OFF);
            } else {
                menu.replaceExistingItem(CLEAR_SLOT, CLEAR_ON);
                clearOutput(menu);
            }
            return false;
        });
        
        menu.addMenuClickHandler(LOOP_SLOT, (player, i, itemStack, clickAction) -> {
            boolean isLoopOn = itemStack != null && itemStack.getType() == Material.GREEN_STAINED_GLASS_PANE;
            if (isLoopOn) {
                menu.replaceExistingItem(LOOP_SLOT, LOOP_OFF);
            } else {
                menu.replaceExistingItem(LOOP_SLOT, LOOP_ON);
            }
            return false;
        });
    }

    protected MachineRecipe findRecipe(BlockMenu menu) {
        boolean hasVirtualWorld = false;
        ItemStack mainInput = null;

        for (int slot : INPUT_SLOT) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && !item.getType().isAir()) {
                if (mainInput == null) {
                    mainInput = item;
                }
                SlimefunItem sfItem = SlimefunItem.getByItem(item);
                if (sfItem != null && AddItem.VIRTUALWORLD != null && sfItem.getId().equals(AddItem.VIRTUALWORLD.getItemId())) {
                    hasVirtualWorld = true;
                }
            }
        }

        if (mainInput == null || mainInput.getType().isAir()) {
            return null;
        }

        return new MachineRecipe(0, new ItemStack[]{mainInput.clone()}, new ItemStack[] {processOutput(mainInput, hasVirtualWorld)});
    }

    private ItemStack processOutput(ItemStack input, boolean hasVirtualWorld) {
        double rand = random.nextDouble();
        
        if (hasVirtualWorld && rand < 0.05) {
            if (random.nextDouble() < 0.01) {
                Debug.debug("机器熔毁！");
                return new CustomItemStack(Material.FIRE, "&4机器熔毁!", "&c机器因逻辑崩溃而熔毁");
            }
            return new ItemStack(input.getType(), 64);
        }

        if (random.nextDouble() < 0.3) {
            Material[] materials = Material.values();
            Material randomMaterial = materials[random.nextInt(materials.length)];
            int amount = random.nextInt(65);
            return new ItemStack(randomMaterial, amount);
        } else {
            if (slimefunItems.isEmpty()) {
                for (SlimefunItem sfItem : Slimefun.getRegistry().getAllSlimefunItems()) {
                    if (sfItem != null && !sfItem.isDisabled()) {
                        slimefunItems.add(sfItem);
                    }
                }
            }
            
            if (!slimefunItems.isEmpty()) {
                SlimefunItem randomSF = slimefunItems.get(random.nextInt(slimefunItems.size()));
                ItemStack output = randomSF.getItem().clone();
                int amount = random.nextInt(65);
                output.setAmount(Math.min(amount, output.getMaxStackSize()));
                return output;
            }
        }

        return null;
    }
    
    private boolean hasEmptyOutputSlot(BlockMenu menu) {
        for (int slot : OUTPUT_SLOT) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasInputItem(BlockMenu menu) {
        for (int slot : INPUT_SLOT) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && !item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }
    
    private void clearOutput(BlockMenu menu) {
        for (int slot : OUTPUT_SLOT) {
            menu.replaceExistingItem(slot, null);
        }
    }

    @Override
    public void process(Block b, BlockMenu menu, SlimefunBlockData data) {
        // 获取循环模式状态
        ItemStack loopItem = menu.getItemInSlot(LOOP_SLOT);
        boolean isLoopMode = loopItem != null && loopItem.getType() == Material.GREEN_STAINED_GLASS_PANE;

        // 检查是否有输入物品
        boolean hasInput = false;
        for (int inputSlot : INPUT_SLOT) {
            ItemStack inputItem = menu.getItemInSlot(inputSlot);
            if (inputItem != null && !inputItem.getType().isAir()) {
                hasInput = true;
                break;
            }
        }
        
        // 如果没有输入，但开启了循环模式，尝试从输出转移到输入
        if (!hasInput) {
            if (isLoopMode) {
                transferOutputToInput(menu);
            }
            return;
        }

        // 处理输入物品 - 只要有输入物品就继续处理
        for (int inputSlot : INPUT_SLOT) {
            ItemStack inputItem = menu.getItemInSlot(inputSlot);
            if (inputItem != null && !inputItem.getType().isAir()) {
                // 检查输出槽是否有空间
                boolean hasOutputSpace = hasEmptyOutputSlot(menu);
                
                // 如果输出槽满了，尝试循环转移
                if (!hasOutputSpace && isLoopMode) {
                    transferOutputToInput(menu);
                    hasOutputSpace = hasEmptyOutputSlot(menu);
                }
                
                // 如果输出槽还是满的，跳过当前槽位
                if (!hasOutputSpace) {
                    continue;
                }

                // 检查能源 - 参考RecipeMachine的逻辑：开始前检查电量
                if (isChargeable()) {
                    if (getCharge(menu.getLocation()) < energyConsumption) {
                        continue; // 电量不足，尝试下一个槽位
                    }
                    // 消耗能源
                    removeCharge(menu.getLocation(), energyConsumption);
                }

                // 检查是否有虚拟世界
                boolean hasVirtualWorld = false;
                SlimefunItem sfItem = SlimefunItem.getByItem(inputItem);
                if (sfItem != null && AddItem.VIRTUALWORLD != null && 
                    sfItem.getId().equals(AddItem.VIRTUALWORLD.getItemId())) {
                    hasVirtualWorld = true;
                }

                // 生成输出
                ItemStack output = processOutput(inputItem, hasVirtualWorld);
                
                // 再次检查输出空间
                hasOutputSpace = hasEmptyOutputSlot(menu);
                if (!hasOutputSpace && isLoopMode) {
                    transferOutputToInput(menu);
                    hasOutputSpace = hasEmptyOutputSlot(menu);
                }
                
                // 只有当有输出且有输出空间时才消耗输入
                if (output != null && hasOutputSpace) {
                    // 消耗输入物品
                    menu.replaceExistingItem(inputSlot, null);
                    
                    // 放入输出槽
                    for (int slot : OUTPUT_SLOT) {
                        ItemStack existing = menu.getItemInSlot(slot);
                        if (existing == null || existing.getType().isAir()) {
                            menu.replaceExistingItem(slot, output.clone());
                            return; // 成功处理一个物品，返回
                        } else if (existing.isSimilar(output) && existing.getAmount() < existing.getMaxStackSize()) {
                            existing.setAmount(Math.min(existing.getAmount() + output.getAmount(), existing.getMaxStackSize()));
                            return; // 成功处理一个物品，返回
                        }
                    }
                }
                
                // 如果没有输出或输出槽满了，继续处理下一个槽位
                continue;
            }
        }
    }
    
    private void transferOutputToInput(BlockMenu menu) {
        // 遍历输出槽，将物品转移到输入槽
        for (int outputSlot : OUTPUT_SLOT) {
            ItemStack outputItem = menu.getItemInSlot(outputSlot);
            if (outputItem != null && !outputItem.getType().isAir()) {
                // 查找第一个空输入槽或可堆叠的槽位
                boolean transferred = false;
                for (int inputSlot : INPUT_SLOT) {
                    ItemStack inputItem = menu.getItemInSlot(inputSlot);
                    if (inputItem == null || inputItem.getType().isAir()) {
                        menu.replaceExistingItem(inputSlot, outputItem.clone());
                        menu.replaceExistingItem(outputSlot, null);
                        transferred = true;
                        break;
                    } else if (inputItem.isSimilar(outputItem) && inputItem.getAmount() < inputItem.getMaxStackSize()) {
                        int addAmount = Math.min(outputItem.getAmount(), inputItem.getMaxStackSize() - inputItem.getAmount());
                        inputItem.setAmount(inputItem.getAmount() + addAmount);
                        outputItem.setAmount(outputItem.getAmount() - addAmount);
                        if (outputItem.getAmount() <= 0) {
                            menu.replaceExistingItem(outputSlot, null);
                        }
                        transferred = true;
                        break;
                    }
                }
                
                if (!transferred) {
                    // 输入槽满了，停止转移
                    break;
                }
            }
        }
    }
}
