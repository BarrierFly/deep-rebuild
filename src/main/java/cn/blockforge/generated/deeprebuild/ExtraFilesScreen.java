package cn.blockforge.generated.deeprebuild;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * 「深重建 · 额外导入」页：按目录树勾选存档里的额外文件，默认全选（进度数据已被过滤掉）。
 */
public class ExtraFilesScreen extends Screen {
    private static final int ROW_HEIGHT = 12;

    private final Screen parent;
    private final DeepRebuildPlan plan;
    private final List<Row> rows = new ArrayList<>();

    private int scroll;
    private boolean rowsDirty = true;
    private int listLeft;
    private int listRight;
    private int listTop;
    private int listBottom;

    public ExtraFilesScreen(Screen parent, DeepRebuildPlan plan) {
        super(Text.translatable("deeprebuild.screen.title"));
        this.parent = parent;
        this.plan = plan;
    }

    @Override
    protected void init() {
        this.listLeft = Math.max(6, this.width / 2 - 205);
        this.listRight = Math.max(this.listLeft + 120, Math.min(this.width - 6, this.width / 2 + 205));
        this.listTop = 52;
        this.listBottom = Math.max(this.listTop + 40, this.height - 64);

        int w = 100;
        int gap = 4;
        int x = this.width / 2 - (w * 3 + gap * 2) / 2;
        int y = this.height - 56;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(DeepRebuildPreset.CONFIG_ONLY.translationKey), b -> {
            this.plan.applyPreset(DeepRebuildPreset.CONFIG_ONLY);
            this.rowsDirty = true;
        }).dimensions(x, y, w, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(DeepRebuildPreset.CONFIG_AND_RESOURCES.translationKey), b -> {
            this.plan.applyPreset(DeepRebuildPreset.CONFIG_AND_RESOURCES);
            this.rowsDirty = true;
        }).dimensions(x + w + gap, y, w, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(DeepRebuildPreset.ALL.translationKey), b -> {
            this.plan.applyPreset(DeepRebuildPreset.ALL);
            this.rowsDirty = true;
        }).dimensions(x + (w + gap) * 2, y, w, 20).build());

        int y2 = this.height - 32;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("deeprebuild.select_all"), b -> {
            this.plan.selectAll();
        }).dimensions(x, y2, w, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("deeprebuild.select_none"), b -> {
            this.plan.selectNone();
        }).dimensions(x + w + gap, y2, w, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("deeprebuild.done"), b -> this.close())
                .dimensions(x + (w + gap) * 2, y2, w, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (this.rowsDirty) {
            this.rebuildRows();
        }
        int panelLeft = this.listLeft - 3;
        int panelRight = this.listRight + 9;
        fill(matrices, panelLeft, this.listTop - 4, panelRight, this.listBottom + 4, 0xB0101018);

        enableScissor(this.listLeft, this.listTop, this.listRight, this.listBottom);
        int visibleRows = Math.max(1, (this.listBottom - this.listTop) / ROW_HEIGHT);
        int first = Math.max(0, this.scroll / ROW_HEIGHT);
        int last = Math.min(this.rows.size(), first + visibleRows + 2);
        for (int i = first; i < last; i++) {
            Row row = this.rows.get(i);
            int y = this.listTop + i * ROW_HEIGHT - this.scroll;
            boolean hovered = mouseX >= this.listLeft && mouseX < this.listRight
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if (hovered) {
                fill(matrices, this.listLeft, y, this.listRight, y + ROW_HEIGHT, 0x35FFFFFF);
            }
            this.drawRow(matrices, row, y);
        }
        disableScissor();

        this.drawScrollbar(matrices);
        this.drawHeader(matrices);
    }

    private void drawHeader(MatrixStack matrices) {
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        int total = this.plan.totalCount();
        int selected = this.plan.selectedCount();
        Text subtitle = Text.translatable("deeprebuild.screen.summary",
                selected, total, DeepRebuildUtil.bytes(this.plan.selectedBytes()));
        drawCenteredTextWithShadow(matrices, this.textRenderer, subtitle, this.width / 2, 28, 0xFF9FD0FF);
        if (this.plan.ignoredCount() > 0) {
            drawCenteredTextWithShadow(matrices, this.textRenderer,
                    Text.translatable("deeprebuild.screen.filtered", this.plan.ignoredCount())
                            .formatted(Formatting.DARK_GRAY),
                    this.width / 2, 40, 0xFF707070);
        }
    }

    private void drawScrollbar(MatrixStack matrices) {
        int visible = this.listBottom - this.listTop;
        int content = this.rows.size() * ROW_HEIGHT;
        if (content <= visible) {
            return;
        }
        int barHeight = Math.max(16, visible * visible / content);
        int max = content - visible;
        int barY = this.listTop + (visible - barHeight) * this.scroll / Math.max(1, max);
        fill(matrices, this.listRight + 2, barY, this.listRight + 7, barY + barHeight, 0xFF6C7FA0);
    }

    private void drawRow(MatrixStack matrices, Row row, int y) {
        FileNode node = row.node;
        int x = this.listLeft + 3 + row.depth * 8;
        if (node.directory) {
            drawTextWithShadow(matrices, this.textRenderer, Text.literal(node.expanded ? "-" : "+"),
                    x, y + 2, 0xFFB0B8C8);
        }
        int boxX = x + 9;
        boolean on = this.plan.isSelected(node);
        boolean partial = this.plan.isPartiallySelected(node);
        this.drawCheckbox(matrices, boxX, y + 1, on, partial);
        int textX = boxX + 12;
        int color = node.directory ? 0xFFE8C978 : 0xFFDDDDDD;
        String right = node.directory
                ? this.plan.selectedIn(node) + "/" + node.fileCount
                : DeepRebuildUtil.bytes(node.size);
        int rightWidth = this.textRenderer.getWidth(right);
        drawTextWithShadow(matrices, this.textRenderer, Text.literal(node.name), textX, y + 2, color);
        drawTextWithShadow(matrices, this.textRenderer, Text.literal(right),
                this.listRight - 4 - rightWidth, y + 2, 0xFF8C93A0);
    }

    private void drawCheckbox(MatrixStack matrices, int x, int y, boolean on, boolean partial) {
        fill(matrices, x, y, x + 9, y + 9, 0xFF05050A);
        fill(matrices, x + 1, y + 1, x + 8, y + 8, on ? 0xFF2F6FD0 : (partial ? 0xFF3D5A85 : 0xFF1B1B22));
        if (on) {
            fill(matrices, x + 3, y + 4, x + 6, y + 7, 0xFFE8F2FF);
        } else if (partial) {
            fill(matrices, x + 2, y + 4, x + 7, y + 5, 0xFFD8DEE8);
        }
    }

    private void rebuildRows() {
        this.rows.clear();
        for (FileNode child : this.plan.tree().children) {
            this.addRows(child, 0);
        }
        this.rowsDirty = false;
        this.clampScroll();
    }

    private void addRows(FileNode node, int depth) {
        this.rows.add(new Row(node, depth));
        if (node.directory && node.expanded) {
            for (FileNode child : node.children) {
                this.addRows(child, depth + 1);
            }
        }
    }

    private void clampScroll() {
        int visible = this.listBottom - this.listTop;
        int max = Math.max(0, this.rows.size() * ROW_HEIGHT - visible);
        this.scroll = Math.max(0, Math.min(this.scroll, max));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (mouseX < this.listLeft || mouseX >= this.listRight
                || mouseY < this.listTop || mouseY >= this.listBottom) {
            return false;
        }
        int index = (int) ((mouseY - this.listTop + this.scroll) / ROW_HEIGHT);
        if (index < 0 || index >= this.rows.size()) {
            return false;
        }
        Row row = this.rows.get(index);
        int arrowX = this.listLeft + 3 + row.depth * 8;
        if (row.node.directory && mouseX >= arrowX - 1 && mouseX < arrowX + 8) {
            row.node.expanded = !row.node.expanded;
            this.rowsDirty = true;
            return true;
        }
        this.plan.toggle(row.node);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.rows.isEmpty()) {
            return false;
        }
        this.scroll = Math.max(0, this.scroll - (int) (amount * ROW_HEIGHT * 2));
        this.clampScroll();
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private static final class Row {
        final FileNode node;
        final int depth;

        Row(FileNode node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }
}
