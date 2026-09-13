package me.matl114.logitech.core.Depends;

import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.NotImplementedException;
import me.matl114.logitech.core.Cargo.Storages;
import me.matl114.logitech.core.Registries.AddDepends;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.StorageClass.StorageType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NetworksQuantumStorageAdapter extends StorageType {
    int ExceptionTimes = 0;

    /**
     * 性能优化: QuantumCache 的 PDC 反序列化成本较高,
     * 而机器运行时会在同一个 tick 内对同一个 ItemMeta 反复查询(内容/数量/容量/isStorage 各查一次)。
     * 这里按 ItemMeta 实例做短TTL(约1tick)的线程本地缓存:
     * - Bukkit 的 getItemMeta() 每次返回快照副本, 外部改动必然产生新的 meta 实例, 不会读到脏数据;
     * - 本附属对数量的修改统一走 onStorageAmountWrite, 修改的是缓存中同一个 QuantumCache 实例后再写回,
     *   因此缓存对象始终与 meta 内数据一致, 不会产生数量错乱/刷物问题。
     */
    private static final long CACHE_TTL_NANOS = 50_000_000L; // ~1 server tick
    private static final int CACHE_MAX_SIZE = 256;

    private static final class CacheEntry {
        final QuantumCache cache;
        final long born;

        CacheEntry(QuantumCache cache, long born) {
            this.cache = cache;
            this.born = born;
        }
    }

    private static final ThreadLocal<java.util.IdentityHashMap<ItemMeta, CacheEntry>> META_CACHE =
            ThreadLocal.withInitial(java.util.IdentityHashMap::new);

    public NetworksQuantumStorageAdapter() {
        super();
    }

    @Override
    public boolean isStorage(ItemMeta meta) {
        return getQuantumCache(meta) != null;
    }

    @Override
    public boolean canStorage(ItemMeta meta) {
        // 我们不帮网络存储设置类型 。
        // 该方法只在设置类型的时候被调用
        return false;
        //        if(AddDepends.NETWORKSQUANTUMSTORAGE==null){
        //            return false;
        //        }
        //        String id=CraftUtils.parseSfId(meta);
        //        if(id==null){
        //            return false;
        //        }
        //        return AddDepends.NETWORKSQUANTUMSTORAGE.isInstance(SlimefunItem.getById(id));
    }

    @Override
    public boolean canStorage(SlimefunItem item) {
        // 我们不帮网络存储设置类型 。
        return false;
        //        if(AddDepends.NETWORKSQUANTUMSTORAGE==null){
        //            return false;
        //        }
        //        return AddDepends.NETWORKSQUANTUMSTORAGE.isInstance(item);
    }

    public QuantumCache getQuantumCache(ItemMeta meta) {
        if (meta == null || AddDepends.NTWQUANTUMKEY == null) {
            return null;
        }
        long now = System.nanoTime();
        java.util.IdentityHashMap<ItemMeta, CacheEntry> map = META_CACHE.get();
        CacheEntry entry = map.get(meta);
        if (entry != null && now - entry.born < CACHE_TTL_NANOS) {
            return entry.cache;
        }
        QuantumCache cache;
        try {
            cache = DataTypeMethods.getCustom(meta, AddDepends.NTWQUANTUMKEY, PersistentQuantumStorageType.TYPE);
        } catch (Throwable e) {
            disableNetworkQuantum(e);
            return null;
        }
        if (map.size() >= CACHE_MAX_SIZE) {
            map.clear();
        }
        map.put(meta, new CacheEntry(cache, now));
        return cache;
    }

    public void disableNetworkQuantum(Throwable e) {
        Debug.logger("AN ERROR OCCURED IN NETWORK_QUANTUM_STORAGE, STORAGE TYPE MAY BE DISABLED %d/%d"
                .formatted(ExceptionTimes, 100));
        Debug.logger(e);
        ExceptionTimes++;
        if (ExceptionTimes > 100) {
            Storages.disableNetworkStorage();
            disableStorageType(this);
        }
    }

    public long getStorageMaxSize(QuantumCache cache) {
        if (cache == null) {
            return 0;
        }
        // Method amount=NetWorkQuantumMethod. getLimitMethod(cache);
        try {
            Number num = NetWorkQuantumMethod.getLimitAccess.invoke(cache); // (Integer)amount.invoke(cache);
            return num.longValue();
        } catch (Throwable e) {
            disableNetworkQuantum(e);
            return 0;
        }
    }

    @Override
    public long getStorageMaxSize(ItemMeta meta) {
        QuantumCache cache = getQuantumCache(meta);
        return getStorageMaxSize(cache);
    }

    @Override
    public void setStorage(ItemMeta meta, ItemStack content) {
        // 我们不帮网络存储设置类型 。
        //        DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE,
        // cache);
        //        cache.addMetaLore(itemMeta);
        //        itemToDrop.setItemMeta(itemMeta);
        throw new NotImplementedException("NetworkQuantumStorage's content shouldn't be set");
    }

    public void setAmount(QuantumCache cache, long amount) {
        if (cache == null) {
            return;
        }
        // Method set=NetWorkQuantumMethod.getSetAmountMethod(cache);
        try {
            NetWorkQuantumMethod.getSetAmountAccess.invoke(cache, amount);
            // set.invoke(cache, amount);
        } catch (Throwable e) {
            disableNetworkQuantum(e);
            return;
        }
    }

    @Override
    public void onStorageAmountWrite(ItemMeta meta, long amount) {
        QuantumCache cache = getQuantumCache(meta);
        setAmount(cache, amount);
        DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, cache);
    }

    @Override
    public long getStorageAmount(ItemMeta meta) {
        QuantumCache cache = getQuantumCache(meta);
        return getStorageAmount(cache);
    }

    public long getStorageAmount(QuantumCache cache) {
        if (cache == null) {
            return 0;
        }
        // Method amount=  NetWorkQuantumMethod. getAmountMethod(cache);

        try {
            Number res = NetWorkQuantumMethod.getAmountAccess.invoke(cache);
            return res.longValue();
        } catch (Throwable e) {
            disableNetworkQuantum(e);
            return 0;
        }
    }

    @Override
    public ItemStack getStorageContent(ItemMeta meta) {
        QuantumCache cache = getQuantumCache(meta);
        return getStorageContent(cache);
    }

    public ItemStack getStorageContent(QuantumCache cache) {
        if (cache == null) {
            return null;
        }
        // Method getItem=NetWorkQuantumMethod.getItemStackMethod(cache);
        try {
            return NetWorkQuantumMethod.getItemStackAccess.invoke(cache); // (ItemStack) getItem.invoke(cache);
        } catch (Throwable e) {
            disableNetworkQuantum(e);
            return null;
        }
    }

    @Override
    public void clearStorage(ItemMeta meta) {
        throw new NotImplementedException("NetworkQuantumStorage's content shouldn't be cleared");
    }

    @Override
    public void onStorageDisplayWrite(ItemMeta meta, long amount) {
        var cache = getQuantumCache(meta);
        if (cache == null) {
            return;
        }
        // Method set;
        try {
            NetWorkQuantumMethod.updateMetaLoreAccess.invoke(cache, meta);
            //            set=NetWorkQuantumMethod.getUpdateMetaLore(cache);
            //            set.invoke(cache, meta);
        } catch (Throwable e) {
            disableNetworkQuantum(e);
        }
    }
}
