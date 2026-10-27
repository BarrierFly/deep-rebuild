package cn.blockforge.generated.deeprebuild;

import java.util.Locale;
import java.util.Set;

/** 三档预设：只要配置 / 配置加资源 / 全部（都不含进度数据）。 */
public enum DeepRebuildPreset {
    CONFIG_ONLY("deeprebuild.preset.config"),
    CONFIG_AND_RESOURCES("deeprebuild.preset.config_resources"),
    ALL("deeprebuild.preset.all");

    private static final Set<String> CONFIG_DIRS = Set.of(
            "config", "serverconfig", "defaultconfigs", "scripts", "kubejs", "local",
            "configs", "modconfig", "mod-configs", "openloader");
    private static final Set<String> RESOURCE_DIRS = Set.of(
            "resources", "resourcepacks", "resource_packs", "datapacks", "custom_images", "structure");
    private static final Set<String> CONFIG_EXTENSIONS = Set.of(
            ".json", ".toml", ".cfg", ".conf", ".ini", ".properties", ".yaml", ".yml", ".xml", ".hocon");

    public final String translationKey;

    DeepRebuildPreset(String translationKey) {
        this.translationKey = translationKey;
    }

    public boolean includes(String relPath) {
        if (this == ALL) {
            return true;
        }
        boolean config = isConfig(relPath);
        if (this == CONFIG_ONLY) {
            return config;
        }
        return config || isResource(relPath);
    }

    private static boolean isConfig(String rel) {
        String lower = rel.toLowerCase(Locale.ROOT);
        int slash = lower.indexOf('/');
        if (slash < 0) {
            return hasConfigExtension(lower);
        }
        return CONFIG_DIRS.contains(lower.substring(0, slash));
    }

    private static boolean isResource(String rel) {
        String lower = rel.toLowerCase(Locale.ROOT);
        if (lower.equals("resources.zip") || lower.equals("resources.zip.tmp")) {
            return true;
        }
        int slash = lower.indexOf('/');
        return slash >= 0 && RESOURCE_DIRS.contains(lower.substring(0, slash));
    }

    private static boolean hasConfigExtension(String name) {
        for (String ext : CONFIG_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
