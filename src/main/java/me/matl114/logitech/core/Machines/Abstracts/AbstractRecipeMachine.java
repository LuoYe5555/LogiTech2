// AbstractRecipeMachine.java
package me.matl114.logitech.core.Machines.Abstracts;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.MachineRecipeUtils;
import me.matl114.logitech.utils.UtilClass.ItemClass.DisplayItemStack;
import me.matl114.logitech.utils.UtilClass.MenuClass.DataMenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.List;

/**
 * 配方机器抽象基类 - 严格按 6×9 GUI 布局修正版
 * ✅ slot 4: 输入槽
 * ✅ slot 13: 唯一状态/进度显示槽
 * ✅ slot 18-53: 全部输出槽（36格）
 * ✅ 仅1个进度条，无冗余槽位
 */
public abstract class AbstractRecipeMachine extends AbstractMachine {

    // 核心配置
    public final int time;
    protected int pubTick;

    // ✅ 关键槽位定义（严格按您的要求）
    public static final int INPUT_SLOT = 4;           // 唯一输入槽
    public static final int STATUS_SLOT = 13;         // 唯一状态/进度显示槽
    public static final int OUTPUT_START = 18;        // 输出槽起始位置
    public static final int OUTPUT_COUNT = 36;        // 输出槽数量（6行×9列 - 前2行 = 54-18=36）
    public static final int[] OUTPUT_SLOTS = new int[OUTPUT_COUNT];

    static {
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            OUTPUT_SLOTS[i] = OUTPUT_START + i;
        }
    }

    // 状态显示项 - 延迟初始化以避免构造时的问题
    private ItemStack infoIdle;
    private ItemStack infoWorking;
    private ItemStack progressEmpty;
    private ItemStack progressFull;

    // 创建带有显示名称和Lore的物品，自动处理颜色代码
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

    public ItemStack getInfoIdle() {
        if (infoIdle == null) {
            infoIdle = createItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6空闲中");
        }
        return infoIdle;
    }

    public ItemStack getInfoWorking() {
        if (infoWorking == null) {
            infoWorking = createItemStack(Material.GREEN_STAINED_GLASS_PANE, "&a工作中");
        }
        return infoWorking;
    }

    public ItemStack getProgressEmpty() {
        if (progressEmpty == null) {
            progressEmpty = createItemStack(Material.GRAY_STAINED_GLASS_PANE, "&8进度: 0%");
        }
        return progressEmpty;
    }

    public ItemStack getProgressFull() {
        if (progressFull == null) {
            progressFull = createItemStack(Material.LIME_STAINED_GLASS_PANE, "&a进度: 100%");
        }
        return progressFull;
    }

    // 构造函数
    public AbstractRecipeMachine(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int time,
            int energybuffer,
            int energyConsumption,
            List<Pair<Object, Integer>> customRecipes) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);

        this.time = (time <= 0) ? 1 : time;
        this.pubTick = 0;

        if (customRecipes != null) {
            this.machineRecipes = new ArrayList<>(customRecipes.size());
            var tmp = AddUtils.buildRecipeMap(customRecipes);
            for (var recipePiece : tmp) {
                // 确保配方构建不会出错
                try {
                    MachineRecipe machineRecipe = new MachineRecipe(
                            recipePiece.getSecondValue(),
                            recipePiece.getFirstValue().getFirstValue(),
                            recipePiece.getFirstValue().getSecondValue()
                    );
                    this.machineRecipes.add(MachineRecipeUtils.stackFromMachine(machineRecipe));
                } catch (Exception e) {
                    System.err.println("Error creating machine recipe: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            this.machineRecipes = new ArrayList<>();
        }
    }

    // ==================== 必须重写的抽象方法 ====================
    @Override
    public int[] getInputSlots() {
        return new int[]{INPUT_SLOT};
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    // 正确实现 BlockMenuPreset 抽象方法
    @Override
    public abstract void constructMenu(BlockMenuPreset preset);

    // ==================== 核心处理逻辑 ====================
    @Override
    public void process(Block b, BlockMenu menu, SlimefunBlockData data) {
        DataMenuClickHandler holder = getDataHolder(b, menu);

        // 如果有正在进行的任务
        if (holder.getInt(0) > 0) {
            int remaining = holder.getInt(0) - 1;
            holder.setInt(0, remaining);

            // 更新进度显示（仅 slot 13）
            if (menu.hasViewer()) {
                int progress = (int) ((double) (this.time - remaining) / this.time * 100);
                ItemStack progressItem = getProgressItem(progress);
                menu.replaceExistingItem(STATUS_SLOT, progressItem);
            }

            if (remaining <= 0) {
                // 任务完成
                completeTask(b, menu, holder);
            }
        } else {
            // 尝试开始新任务
            startNewTask(b, menu, holder);
        }
    }

    private void startNewTask(Block b, BlockMenu menu, DataMenuClickHandler holder) {
        MachineRecipe rep = CraftUtils.matchNextRecipe(
                menu,
                getInputSlots(),
                getMachineRecipes(),
                true,
                Settings.SEQUNTIAL,
                CRAFT_PROVIDER);

        if (rep == null) {
            resetStatus(menu);
            return;
        }

        // 检查能量
        if (getCharge(menu.getLocation()) < energyConsumption) {
            resetStatus(menu);
            return;
        }

        // 检查输出空间（关键：使用安全的 push 检查）
        if (!hasEnoughOutputSpace(menu, rep.getOutput())) {
            resetStatus(menu);
            return;
        }

        // ✅ 开始任务：设置计时器，消耗能量
        holder.setInt(0, this.time); // 剩余时间
        holder.setInt(1, DataCache.getLastRecipe(menu.getLocation())); // 配方ID

        removeCharge(menu.getLocation(), energyConsumption);

        // 更新状态显示（仅 slot 13）
        if (menu.hasViewer()) {
            menu.replaceExistingItem(STATUS_SLOT, getProgressItem(0));
        }
    }

    private void completeTask(Block b, BlockMenu menu, DataMenuClickHandler holder) {
        MachineRecipe rep = CraftUtils.matchNextRecipe(
                menu,
                getInputSlots(),
                getMachineRecipes(),
                true,
                Settings.SEQUNTIAL,
                CRAFT_PROVIDER);

        if (rep != null) {
            // ✅ 安全消耗输入：保留 NBT（修复后的版本）
            consumeInputSafely(menu, rep.getInput());

            // ✅ 安全推送输出：使用 CraftUtils.pushItems（已确保保留 NBT）
            CraftUtils.pushItems(rep.getOutput(), menu, getOutputSlots(), CRAFT_PROVIDER);
        }

        // 重置
        holder.setInt(0, 0);
        holder.setInt(1, -1);

        // 显示空闲
        if (menu.hasViewer()) {
            menu.replaceExistingItem(STATUS_SLOT, getInfoIdle());
        }
    }

    // ✅ 安全消耗输入（保留 NBT）- 修复了当输入槽物品数量大于所需数量时不消耗的问题
    private void consumeInputSafely(BlockMenu menu, ItemStack[] inputs) {
        ItemStack inputSlotItem = menu.getItemInSlot(INPUT_SLOT);
        if (inputSlotItem == null) return;

        for (ItemStack required : inputs) {
            if (required == null) continue;

            if (inputSlotItem.isSimilar(required) && inputSlotItem.getAmount() >= required.getAmount()) {
                // 减少数量，保留 NBT
                int newAmount = inputSlotItem.getAmount() - required.getAmount();

                // 修复：正确处理数量变化
                if (newAmount > 0) {
                    ItemStack newItem = inputSlotItem.clone();
                    newItem.setAmount(newAmount);
                    menu.replaceExistingItem(INPUT_SLOT, newItem);
                } else {
                    menu.replaceExistingItem(INPUT_SLOT, null);
                }
                break; // 仅处理第一个匹配输入（单输入槽）
            }
        }
    }

    // ✅ 检查输出空间（考虑堆叠和 NBT）
    private boolean hasEnoughOutputSpace(BlockMenu menu, ItemStack[] outputs) {
        for (ItemStack output : outputs) {
            if (output == null) continue;

            boolean foundSpace = false;
            for (int slot : OUTPUT_SLOTS) {
                ItemStack existing = menu.getItemInSlot(slot);
                if (existing == null) {
                    foundSpace = true;
                    break;
                } else if (existing.isSimilar(output) && existing.getAmount() < existing.getMaxStackSize()) {
                    foundSpace = true;
                    break;
                }
            }
            if (!foundSpace) return false;
        }
        return true;
    }

    // 重置状态显示
    private void resetStatus(BlockMenu menu) {
        if (menu.hasViewer()) {
            menu.replaceExistingItem(STATUS_SLOT, getInfoIdle());
        }
    }

    // 获取进度显示物品（带 lore），颜色代码已修复
    private ItemStack getProgressItem(int progress) {
        String color = progress >= 100 ? "&a" : "&b";
        Material mat = progress >= 100 ? Material.LIME_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE;
        String name = color + "进度: " + progress + "%";
        String lore = "&7剩余时间: " + Math.max(0, this.time - (progress * this.time / 100)) + " ticks";

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== 辅助方法 ====================
    @Override
    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        SlimefunBlockData data = DataCache.safeLoadBlock(inv.getLocation());
        if (data == null) {
            Schedules.launchSchedules(() -> updateMenu(inv, b, mod), 20, false, 0);
            return;
        }

        DataMenuClickHandler holder = getDataHolder(b, inv);
        if (holder.getInt(0) > 0) {
            int progress = (int) ((double) (this.time - holder.getInt(0)) / this.time * 100);
            if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, getProgressItem(progress));
            }
        } else {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(STATUS_SLOT, getInfoIdle());
            }
        }
    }

    @Override
    public void newMenuInstance(BlockMenu inv, Block b) {
        inv.addMenuOpeningHandler(player -> updateMenu(inv, b, Settings.RUN));
        inv.addMenuCloseHandler(player -> updateMenu(inv, b, Settings.RUN));
        updateMenu(inv, b, Settings.INIT);
    }

    public DataMenuClickHandler createDataHolder() {
        return new DataMenuClickHandler() {
            int[] tick = new int[2]; // [0]=剩余时间, [1]=配方ID
            @Override public int getInt(int i) { return tick[i]; }
            @Override public void setInt(int i, int val) { tick[i] = val; }
            @Override public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) { return false; }
        };
    }

    public final int DATA_SLOT = 0;

    public DataMenuClickHandler getDataHolder(Block b, BlockMenu inv) {
        var handler = inv.getMenuClickHandler(DATA_SLOT);
        if (handler instanceof DataMenuClickHandler dh) return dh;
        DataMenuClickHandler dh = createDataHolder();
        inv.addMenuClickHandler(DATA_SLOT, dh);
        updateMenu(inv, b, Settings.RUN);
        return dh;
    }

    @Override
    public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
        return flow == ItemTransportFlow.INSERT ? getInputSlots() : getOutputSlots();
    }

    @Override
    public boolean isSync() {
        return false;
    }

    @Override
    public List<ItemStack> _getDisplayRecipes(List<ItemStack> list2) {
        List<ItemStack> list = super._getDisplayRecipes(list2);
        if (!list.isEmpty() && list.get(0) == null) {
            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta meta = clock.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7处理速度"));
                meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7每 " + time + " 粘液刻处理一次")));
                clock.setItemMeta(meta);
            }
            list.set(0, new DisplayItemStack(clock));
        }
        return list;
    }
}