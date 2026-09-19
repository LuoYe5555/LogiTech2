package me.matl114.matlib.utils.version;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 覆盖 matlib 的 Version 枚举，添加对 Paper 26.x 新版本号格式的支持。
 * 原版只识别 "1.26" 前缀，但 Paper 26.x 返回 "26.2.build.124"（无 "1." 前缀）。
 */
public enum Version {
    unknown("unknown", Integer.MAX_VALUE),
    legacy("legacy", 0),
    v1_18_R1("v1_18_R1", 8),
    v1_18_R2("v1_18_R2", 9),
    v1_19_R1("v1_19_R1", 11),
    v1_19_R2("v1_19_R2", 12),
    v1_19_R3("v1_19_R3", 13),
    v1_19_R4("v1_19_R4", 14),
    v1_20_R1("v1_20_R1", 15),
    v1_20_R2("v1_20_R2", 18),
    v1_20_R3("v1_20_R3", 26),
    v1_20_R4("v1_20_R4", 41),
    v1_21_R1("v1_21_R1", 48),
    v1_21_R2("v1_21_R2", 57),
    v1_21_R3("v1_21_R3", 61),
    v1_21_R4("v1_21_R4", 71),
    v1_21_R5("v1_21_R5", 80),
    v1_21_R6("v1_21_R6", 81),
    v1_21_R7("v1_21_R7", 88),
    v1_21_R8("v1_21_R8", 94),
    MODERN("modern", Integer.MAX_VALUE - 1);

    private final String name;
    private final int datapackNumber;

    Version(String name, int datapackNumber) {
        this.name = name;
        this.datapackNumber = datapackNumber;
    }

    public String getName() {
        return name;
    }

    public int getDatapackNumber() {
        return datapackNumber;
    }

    static Version INSTANCE;

    public static Version getVersionInstance() {
        if (INSTANCE == null) {
            INSTANCE = getVersionInstance0();
        }
        return INSTANCE;
    }

    private static Version getVersionInstance0() {
        String version = null;
        try {
            String[] path = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            if (path.length >= 4) {
                version = path[3].trim();
            } else {
                version = Bukkit.getServer().getBukkitVersion().split("-")[0].trim();
            }
            for (Version v : Version.values()) {
                if (v.name.equals(version)) {
                    return v;
                }
            }
            switch (version) {
                case "1.20.5":
                case "1.20.6":
                    return v1_20_R4;
                case "1.21":
                case "1.21.1":
                    return v1_21_R1;
                case "1.21.2":
                case "1.21.3":
                    return v1_21_R2;
                case "1.21.4":
                    return v1_21_R3;
                case "1.21.5":
                    return v1_21_R4;
                case "1.21.6":
                case "1.21.7":
                    return v1_21_R5;
                case "1.21.8":
                    return v1_21_R6;
                case "1.21.9":
                    return v1_21_R7;
                case "1.21.10":
                case "1.21.11":
                    return v1_21_R8;
                default:
                    // Paper 26.x+ 新版本号格式: "1.26.x" 或 "26.x.build.xxx"
                    if (version.startsWith("1.26") || version.startsWith("26.")) {
                        return MODERN;
                    }
                    var majorVersionGroup = SEMANTIC_VERSIONS.matcher(version);
                    if (majorVersionGroup.matches()) {
                        MatchResult result = majorVersionGroup.toMatchResult();
                        try {
                            int majorVersion = Integer.parseInt(result.group(1), 10);
                            if (majorVersion < 18) {
                                return legacy;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
            }
            throw new RuntimeException("Version not supported for " + version);
        } catch (Throwable e) {
            return unknown;
        }
    }

    private static final Pattern SEMANTIC_VERSIONS = Pattern.compile("v1_(\\d+)_R(\\d+)");

    public boolean isAtLeast(Version v2) {
        return versionAtLeast(this, v2);
    }

    public static boolean versionAtLeast(Version v1, Version v2) {
        return v1.datapackNumber >= v2.datapackNumber;
    }

    public static boolean isDataComponentVersion() {
        return getVersionInstance().isAtLeast(v1_20_R4);
    }
}
