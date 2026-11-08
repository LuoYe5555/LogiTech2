package me.matl114.logitech.manager;

import me.matl114.logitech.MyAddon;
import me.matl114.logitech.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动更新: 比对 GitHub latest release tag 尾部短哈希与本地构建时注入的 build 短哈希,
 * 不一致则下载新 jar 到 plugins/update/, 服务器下次启动时 Bukkit 自动替换加载旧 jar。
 * GitHub 直连失败时按序尝试国内镜像前缀。
 */
public class AutoUpdater {
    private static final String API_URL = "https://api.github.com/repos/%s/%s/releases/latest";
    // 直连失败时按序尝试的镜像前缀(国内网络环境)
    private static final String[] MIRRORS = {"https://gh-proxy.com/", "https://ghfast.top/", "https://ghproxy.net/"};
    // release tag 尾部的 commit 短哈希, 如 更新-13ab34c
    private static final Pattern TAG_SHA = Pattern.compile("([0-9a-fA-F]{7,40})$");
    // 本地构建注入的 build 标识(来自 plugin.yml 的 build 字段)
    private static String localBuild = "dev";

    public static void start(JavaPlugin plugin) {
        localBuild = readLocalBuild(plugin);
        if ("dev".equals(localBuild)) {
            Debug.debug("[自动更新] 本地为开发构建(未注入build标识), 跳过自动更新");
            return;
        }
        // 启动30秒后首查, 之后每6小时一次
        Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, AutoUpdater::checkAndUpdate, 600L, 6 * 60 * 60 * 20L);
    }

    private static void checkAndUpdate() {
        try {
            JavaPlugin plugin = MyAddon.getInstance();
            String jarName = getPluginFile(plugin).getName();
            String apiUrl = String.format(API_URL, MyAddon.username, MyAddon.repo);
            String body = httpGet(apiUrl, 25_000);
            if (body == null) {
                Debug.debug("[自动更新] 检查更新失败: GitHub直连与镜像均不可达");
                return;
            }
            String tag = extractJsonString(body, "tag_name");
            if (tag == null) {
                Debug.debug("[自动更新] 检查更新失败: 响应解析失败");
                return;
            }
            String remoteSha = matchTagSha(tag);
            if (remoteSha == null) {
                Debug.debug("[自动更新] release tag %s 中未包含commit短哈希, 跳过".formatted(tag));
                return;
            }
            if (remoteSha.equalsIgnoreCase(localBuild)) {
                Debug.debug("[自动更新] 已是最新版本 " + tag);
                return;
            }
            Debug.logger("[自动更新] 发现新版本 " + tag + " (当前构建 " + localBuild + "), 开始下载...");
            String assetUrl = findAssetUrl(body, jarName);
            if (assetUrl == null) {
                Debug.logger("[自动更新] release " + tag + " 中未找到 " + jarName + " 资产, 跳过下载");
                return;
            }
            byte[] bin = httpGetBinary(assetUrl, 10 * 60_000);
            if (bin == null || bin.length < 1024) {
                Debug.logger("[自动更新] 下载内容过小, 疑似下载失败, 本次更新已跳过");
                return;
            }
            if (bin.length < 2 || bin[0] != 'P' || bin[1] != 'K') {
                Debug.logger("[自动更新] 下载内容不是有效的 jar 文件, 本次更新已跳过");
                return;
            }
            File updateDir = new File(plugin.getDataFolder().getParentFile(), Bukkit.getUpdateFolder());
            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            File target = new File(updateDir, jarName);
            Files.write(target.toPath(), bin);
            Debug.logger("[自动更新] 新版本已下载到 " + target.getPath()
                    + ", 重启服务器后自动替换并加载 (当前 " + localBuild + " -> " + remoteSha + ")");
        } catch (Throwable t) {
            Debug.debug("[自动更新] 更新过程出现异常: " + t.getMessage());
            Debug.debug(t);
        }
    }

    /**
     * 获取插件jar文件, getFile()在不同API版本中可见性不同, 通过反射调用,
     * 失败时按 plugins/<插件名>.jar 约定回退
     */
    private static File getPluginFile(JavaPlugin plugin) {
        try {
            Method m = JavaPlugin.class.getDeclaredMethod("getFile");
            m.setAccessible(true);
            return (File) m.invoke(plugin);
        } catch (Throwable t) {
            return new File(plugin.getDataFolder().getParentFile(), plugin.getName() + ".jar");
        }
    }

    /** 从jar内的plugin.yml读取构建时注入的build短哈希 */
    private static String readLocalBuild(JavaPlugin plugin) {
        try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            if (in == null) {
                return "dev";
            }
            YamlConfiguration yml = new YamlConfiguration();
            yml.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            String build = yml.getString("build");
            return build == null || build.isEmpty() ? "dev" : build.trim();
        } catch (Throwable e) {
            return "dev";
        }
    }

    private static String matchTagSha(String tag) {
        Matcher m = TAG_SHA.matcher(tag);
        return m.find() ? m.group(1) : null;
    }

    /** 从release JSON中找到名为jarName的资产并返回其browser_download_url */
    private static String findAssetUrl(String json, String jarName) {
        Pattern block = Pattern.compile(
                "\\{[^{}]*\"name\"\\s*:\\s*\"" + Pattern.quote(jarName) + "\"[^{}]*\\}");
        Matcher m = block.matcher(json);
        if (m.find()) {
            return extractJsonString(m.group(), "browser_download_url");
        }
        return null;
    }

    private static String extractJsonString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String httpGet(String url, int timeoutMs) {
        byte[] bin = httpGetBinary(url, timeoutMs);
        return bin == null ? null : new String(bin, StandardCharsets.UTF_8);
    }

    /** GitHub直连优先, 失败后按序尝试镜像前缀 */
    private static byte[] httpGetBinary(String url, int timeoutMs) {
        String[] candidates = new String[MIRRORS.length + 1];
        candidates[0] = url;
        for (int i = 0; i < MIRRORS.length; i++) {
            candidates[i + 1] = MIRRORS[i] + url;
        }
        for (String u : candidates) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(u).openConnection();
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(timeoutMs);
                conn.setRequestProperty("User-Agent", "LogiTech2-AutoUpdater");
                conn.setInstanceFollowRedirects(true);
                int code = conn.getResponseCode();
                if (code != 200) {
                    continue;
                }
                try (InputStream in = conn.getInputStream()) {
                    return in.readAllBytes();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }
}
