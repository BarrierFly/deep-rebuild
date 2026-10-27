package cn.blockforge.generated.deeprebuild;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 扫描一个存档目录，按过滤规则剪枝，得到一棵可直接给界面用的文件树。 */
public final class SaveTree {
    private static final int MAX_DEPTH = 24;

    private SaveTree() {
    }

    public static FileNode build(Path root, FilterRules rules) {
        FileNode rootNode = new FileNode(root, "", "", true);
        rootNode.expanded = true;
        walk(rootNode, rules, 0);
        return rootNode;
    }

    private static void walk(FileNode node, FilterRules rules, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        List<Path> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(node.path)) {
            for (Path p : stream) {
                entries.add(p);
            }
        } catch (IOException e) {
            DeepRebuildClient.LOGGER.warn("无法列出目录 {}", node.path, e);
            return;
        }
        entries.sort(Comparator.comparing((Path p) -> !Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                .thenComparing(p -> p.getFileName().toString().toLowerCase(java.util.Locale.ROOT)));

        for (Path entry : entries) {
            String fileName = entry.getFileName().toString();
            String rel = node.relativePath.isEmpty() ? fileName : node.relativePath + "/" + fileName;
            if (Files.isSymbolicLink(entry) || rules.isDenied(rel)) {
                continue;
            }
            boolean dir = Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);
            if (!dir && !Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            FileNode child = new FileNode(entry, fileName, rel, dir);
            if (dir) {
                child.expanded = depth < 1;
                walk(child, rules, depth + 1);
                if (child.fileCount == 0) {
                    continue;
                }
            } else {
                try {
                    child.size = Files.size(entry);
                } catch (IOException ignored) {
                    child.size = 0L;
                }
                child.fileCount = 1;
            }
            node.children.add(child);
            node.size += child.size;
            node.fileCount += child.fileCount;
        }
    }
}
