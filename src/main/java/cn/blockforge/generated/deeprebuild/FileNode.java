package cn.blockforge.generated.deeprebuild;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** 存档目录里的一棵树：目录和文件都用同一个节点表示。 */
public final class FileNode {
    public final Path path;
    public final String name;
    public final String relativePath;
    public final boolean directory;
    public final List<FileNode> children = new ArrayList<>();
    public long size;
    public int fileCount;
    public boolean expanded;

    public FileNode(Path path, String name, String relativePath, boolean directory) {
        this.path = path;
        this.name = name;
        this.relativePath = relativePath;
        this.directory = directory;
    }

    public boolean isEmpty() {
        return this.fileCount == 0;
    }

    public void collectFiles(List<FileNode> out) {
        if (this.directory) {
            for (FileNode child : this.children) {
                child.collectFiles(out);
            }
        } else {
            out.add(this);
        }
    }
}
