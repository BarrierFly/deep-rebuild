package cn.blockforge.generated.deeprebuild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 过滤规则配置。
 *
 * <p>内置一份「进度相关」黑名单（世界数据、玩家数据、游戏时间、统计、图标……），
 * 玩家可以在 {@code config/deeprebuild.json} 里追加自己的通配符规则，
 * 也可以用白名单把某些文件强行捞回来。</p>
 */
public final class DeepRebuildConfig {
    public static final String FILE_NAME = "deeprebuild.json";

    /** 原版「重建」已经会带走的东西 + 关联游戏进度的存档内容，默认不复制。 */
    public static final List<String> BUILTIN_BLACKLIST = List.of(
            "level.dat",
            "level.dat_old",
            "session.lock",
            "icon.png",
            "playerdata/**",
            "stats/**",
            "advancements/**",
            "data/**",
            "region/**",
            "entities/**",
            "poi/**",
            "DIM-1/**",
            "DIM1/**",
            "dimensions/**",
            "**/region/**",
            "**/entities/**",
            "**/poi/**"
    );

    private static List<String> extraBlacklist = new ArrayList<>();
    private static List<String> extraWhitelist = new ArrayList<>();
    private static long maxTotalBytes = 2L * 1024L * 1024L * 1024L;
    private static boolean loaded;

    private DeepRebuildConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            if (Files.notExists(file)) {
                extraBlacklist = new ArrayList<>();
                extraWhitelist = new ArrayList<>();
                writeDefault(file);
            } else {
                String raw = Files.readString(file, StandardCharsets.UTF_8);
                JsonElement root = JsonParser.parseString(raw);
                if (root != null && root.isJsonObject()) {
                    JsonObject obj = root.getAsJsonObject();
                    extraBlacklist = readList(obj, "extra_blacklist");
                    extraWhitelist = readList(obj, "extra_whitelist");
                    if (obj.has("max_total_bytes") && obj.get("max_total_bytes").isJsonPrimitive()) {
                        maxTotalBytes = Math.max(0L, obj.get("max_total_bytes").getAsLong());
                    }
                }
            }
            loaded = true;
        } catch (Exception e) {
            DeepRebuildClient.LOGGER.warn("读取 {} 失败，改用内置规则", file.getFileName(), e);
            extraBlacklist = new ArrayList<>();
            extraWhitelist = new ArrayList<>();
            loaded = true;
        }
    }

    private static void writeDefault(Path file) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("_comment", "深重建：在 extra_blacklist 里按通配符追加要跳过的文件（例如 my_mod/cache/**），extra_whitelist 的优先级更高。");
        obj.add("extra_blacklist", new JsonArray());
        obj.add("extra_whitelist", new JsonArray());
        obj.addProperty("max_total_bytes", maxTotalBytes);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(file, gson.toJson(obj), StandardCharsets.UTF_8);
    }

    private static List<String> readList(JsonObject obj, String key) {
        List<String> out = new ArrayList<>();
        JsonElement e = obj.get(key);
        if (e != null && e.isJsonArray()) {
            for (JsonElement item : e.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    String s = item.getAsString().trim();
                    if (!s.isEmpty()) {
                        out.add(s);
                    }
                }
            }
        }
        return out;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static List<String> extraBlacklist() {
        return extraBlacklist;
    }

    public static List<String> extraWhitelist() {
        return extraWhitelist;
    }

    public static long maxTotalBytes() {
        return maxTotalBytes;
    }
}
