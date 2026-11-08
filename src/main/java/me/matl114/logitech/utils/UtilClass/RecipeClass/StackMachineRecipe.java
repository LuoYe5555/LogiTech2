package me.matl114.logitech.utils.UtilClass.RecipeClass;

import java.util.Collections;
import java.util.Set;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * unshaped recipe with stacked input ItemStack can be used in most Abstract machines
 */
public class StackMachineRecipe extends MachineRecipe {
    /**
     * 标记哪些输入项索引不消耗（仅作为催化剂存在，不从输入槽扣除）
     * 索引对应 getInput() 数组的位置
     */
    private Set<Integer> noConsumeInputs = Collections.emptySet();

    /**
     * 标记输出为多候选随机（如 recipetype: GEO）
     * 运行时从 output[] 里随机选一个产出，而不是全部产出
     */
    private boolean randomOutput = false;

    public StackMachineRecipe(int ticks, ItemStack[] inputs, ItemStack[] outputs) {
        super(0, inputs, outputs);
        this.setTicks(ticks);
    }

    public StackMachineRecipe(int ticks, ItemStack[] inputs, ItemStack[] outputs, Set<Integer> noConsumeInputs) {
        super(0, inputs, outputs);
        this.setTicks(ticks);
        if (noConsumeInputs != null && !noConsumeInputs.isEmpty()) {
            this.noConsumeInputs = noConsumeInputs;
        }
    }

    public boolean isRandomOutput() {
        return randomOutput;
    }

    public StackMachineRecipe setRandomOutput(boolean randomOutput) {
        this.randomOutput = randomOutput;
        return this;
    }

    public Set<Integer> getNoConsumeInputs() {
        return noConsumeInputs;
    }

    public boolean isNoConsumeInput(int index) {
        return noConsumeInputs.contains(index);
    }
}
