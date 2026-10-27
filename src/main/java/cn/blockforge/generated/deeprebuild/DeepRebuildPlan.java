package cn.blockforge.generated.deeprebuild;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 一次深重建的「清单」：树 + 勾选状态 + 体积统计。 */
public final class DeepRebuildPlan {
    private final Path root;
    private final FileNode tree;
    private final String worldName;
    private final List<FileNode> allFiles = new ArrayList<>();
    private final Set<Path> selected = new LinkedHashSet<>();
    private final int ignoredCount;

    private DeepRebuildPlan(Path root, FileNode tree, String worldName, int ignoredCount) {
        this.root = root;
        this.tree = tree;
        this.worldName = worldName;
        this.ignoredCount = ignoredCount;
        tree.collectFiles(this.allFiles);
    }

    public static DeepRebuildPlan build(Path root, String worldName, FilterRules rules) {
        FileNode tree = SaveTree.build(root, rules);
        int ignored = countIgnored(root, rules);
        DeepRebuildPlan plan = new DeepRebuildPlan(root, tree, worldName, ignored);
        plan.applyPreset(DeepRebuildPreset.ALL);
        return plan;
    }

    /** 被过滤掉的条目数，界面上用来告诉玩家「这些没算进来」。 */
    private static int countIgnored(Path root, FilterRules rules) {
        int[] counter = new int[1];
        try {
            java.util.stream.Stream<Path> stream = java.nio.file.Files.walk(root, 1);
            stream.filter(p -> !p.equals(root)).forEach(p -> {
                String rel = root.relativize(p).toString().replace('\\', '/');
                if (rules.isDenied(rel)) {
                    counter[0]++;
                }
            });
            stream.close();
        } catch (Exception ignored) {
            // 数不出来不影响复制
        }
        return counter[0];
    }

    public Path root() {
        return this.root;
    }

    public FileNode tree() {
        return this.tree;
    }

    public String worldName() {
        return this.worldName;
    }

    public List<FileNode> allFiles() {
        return Collections.unmodifiableList(this.allFiles);
    }

    public int ignoredCount() {
        return this.ignoredCount;
    }

    public int totalCount() {
        return this.allFiles.size();
    }

    public long totalBytes() {
        return this.tree.size;
    }

    public void applyPreset(DeepRebuildPreset preset) {
        this.selected.clear();
        for (FileNode node : this.allFiles) {
            if (preset.includes(node.relativePath)) {
                this.selected.add(node.path);
            }
        }
    }

    public void selectAll() {
        for (FileNode node : this.allFiles) {
            this.selected.add(node.path);
        }
    }

    public void selectNone() {
        this.selected.clear();
    }

    public boolean isSelected(FileNode node) {
        if (node.directory) {
            int selectedChildren = 0;
            List<FileNode> files = new ArrayList<>();
            node.collectFiles(files);
            for (FileNode f : files) {
                if (this.selected.contains(f.path)) {
                    selectedChildren++;
                }
            }
            return selectedChildren == files.size() && !files.isEmpty();
        }
        return this.selected.contains(node.path);
    }

    public boolean isPartiallySelected(FileNode node) {
        if (!node.directory || this.isSelected(node)) {
            return false;
        }
        List<FileNode> files = new ArrayList<>();
        node.collectFiles(files);
        for (FileNode f : files) {
            if (this.selected.contains(f.path)) {
                return true;
            }
        }
        return false;
    }

    public int selectedIn(FileNode node) {
        if (!node.directory) {
            return this.selected.contains(node.path) ? 1 : 0;
        }
        List<FileNode> files = new ArrayList<>();
        node.collectFiles(files);
        int count = 0;
        for (FileNode f : files) {
            if (this.selected.contains(f.path)) {
                count++;
            }
        }
        return count;
    }

    public void toggle(FileNode node) {
        if (node.directory) {
            boolean target = !this.isSelected(node);
            List<FileNode> files = new ArrayList<>();
            node.collectFiles(files);
            for (FileNode f : files) {
                if (target) {
                    this.selected.add(f.path);
                } else {
                    this.selected.remove(f.path);
                }
            }
        } else if (!this.selected.remove(node.path)) {
            this.selected.add(node.path);
        }
    }

    public int selectedCount() {
        return this.selected.size();
    }

    public long selectedBytes() {
        long bytes = 0L;
        for (FileNode node : this.allFiles) {
            if (this.selected.contains(node.path)) {
                bytes += node.size;
            }
        }
        return bytes;
    }

    public Collection<Path> selectedPaths() {
        return Collections.unmodifiableSet(this.selected);
    }

    /** 弹窗里显示的清单：先列前若干条，剩下的用一句话概括。 */
    public List<String> previewLines(int max) {
        List<String> lines = new ArrayList<>();
        for (FileNode node : this.allFiles) {
            if (this.selected.contains(node.path)) {
                if (lines.size() >= max) {
                    break;
                }
                lines.add(node.relativePath);
            }
        }
        return lines;
    }
}
