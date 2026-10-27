package cn.blockforge.generated.deeprebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 路径过滤：内置黑名单 + 配置追加的黑名单 + 白名单。
 * 通配符语法：{@code *} 匹配一段路径内任意字符，{@code **} 匹配任意层目录，{@code ?} 匹配单个字符。
 */
public final class FilterRules {
    private final List<Pattern> deny;
    private final List<Pattern> allow;

    private FilterRules(List<Pattern> deny, List<Pattern> allow) {
        this.deny = deny;
        this.allow = allow;
    }

    public static FilterRules fromConfig() {
        List<Pattern> deny = new ArrayList<>();
        for (String rule : DeepRebuildConfig.BUILTIN_BLACKLIST) {
            deny.add(glob(rule));
        }
        for (String rule : DeepRebuildConfig.extraBlacklist()) {
            deny.add(glob(rule));
        }
        List<Pattern> allow = new ArrayList<>();
        for (String rule : DeepRebuildConfig.extraWhitelist()) {
            allow.add(glob(rule));
        }
        return new FilterRules(deny, allow);
    }

    /** relPath 使用 / 分隔、相对存档根目录。 */
    public boolean isDenied(String relPath) {
        if (relPath.isEmpty()) {
            return false;
        }
        String withSlash = relPath.endsWith("/") ? relPath : relPath + "/";
        for (Pattern p : allow) {
            if (p.matcher(relPath).matches() || p.matcher(withSlash).matches()) {
                return false;
            }
        }
        for (Pattern p : deny) {
            if (p.matcher(relPath).matches() || p.matcher(withSlash).matches()) {
                return true;
            }
        }
        return false;
    }

    static Pattern glob(String glob) {
        StringBuilder sb = new StringBuilder("^");
        String g = glob.replace('\\', '/');
        for (int i = 0; i < g.length(); i++) {
            char c = g.charAt(i);
            if (c == '*') {
                if (i + 1 < g.length() && g.charAt(i + 1) == '*') {
                    sb.append(".*");
                    i++;
                } else {
                    sb.append("[^/]*");
                }
            } else if (c == '?') {
                sb.append("[^/]");
            } else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }
}
