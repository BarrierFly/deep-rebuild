package cn.blockforge.generated.deeprebuild;

import java.util.Locale;

/** 小工具：体积格式化之类。 */
public final class DeepRebuildUtil {
    private DeepRebuildUtil() {
    }

    public static String bytes(long value) {
        if (value < 1024L) {
            return value + " B";
        }
        double kb = value / 1024.0D;
        if (kb < 1024.0D) {
            return String.format(Locale.ROOT, "%.1f KB", kb);
        }
        double mb = kb / 1024.0D;
        if (mb < 1024.0D) {
            return String.format(Locale.ROOT, "%.2f MB", mb);
        }
        return String.format(Locale.ROOT, "%.2f GB", mb / 1024.0D);
    }
}
