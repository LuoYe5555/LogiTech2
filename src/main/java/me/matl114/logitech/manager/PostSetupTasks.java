package me.matl114.logitech.manager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.utils.Debug;

public class PostSetupTasks {
    // 在服务器启动之后执行的初始化
    public static boolean startPostRegister = false;
    public static boolean recipeSupportorInit = false;
    public static HashSet<Runnable> registerTasks = new LinkedHashSet<>();

    public static void addPostRegisterTask(Runnable t) {
        registerTasks.add(t);
    }

    public static void schedulePostRegister() {
        startPostRegister = true;
        Debug.debug("START ADDON POSTREGISTER TASKS");
        // 先重扫一遍外部插件可能后注册的机器（如 RSC）
        // 必须在 initCustomMenus() 之前，否则 MACHINE_RECIPELIST 的快照会漏掉它们
        RecipeSupporter.rescanMachineRecipes();
        for (Runnable t : registerTasks) {
            t.run();
        }
    }
}
