package cn.blockforge.generated.deeprebuild;

//? if >=26 {
/*import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
 *///?} else {
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
//?}
//? if <1.20 {
import net.minecraft.client.util.math.MatrixStack;
//?} elif >=1.20 <26 {
/*import net.minecraft.client.gui.DrawContext;
 *///?}
//? if >=1.21 <26 {
/*import net.minecraft.client.gui.Click;
 *///?}

import java.util.ArrayList;
import java.util.List;

/**
 * 「深重建 · 额外导入」页：按目录树勾选存档里的额外文件，默认全选（进度数据已被过滤掉）。
 *
 * <p>绘制按版本分三条分支：1.19.4（MatrixStack + Screen 自绘工具）、
 * 1.20~1.21（DrawContext）、26.x（extractRenderState 抽取式渲染，鼠标事件也换成了事件对象）。</p>
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

    //? if >=26 {
    /*public ExtraFilesScreen(Screen parent, DeepRebuildPlan plan) {
        super(Component.translatable("deeprebuild.screen.title"));
        this.parent = parent;
        this.plan = plan;
    }
     *///?} else {
    public ExtraFilesScreen(Screen parent, DeepRebuildPlan plan) {
        super(Text.translatable("deeprebuild.screen.title"));
        this.parent = parent;
        this.plan = plan;
    }
    //?}

    /** 建按钮：两个版本 families 的 Button/ButtonWidget 差异收在这里。 */
    //? if >=26 {
    /*private Button deeprebuild$button(String key, int x, int y, int w, int h, Button.OnPress onPress) {
        return Button.builder(Component.translatable(key), onPress).bounds(x, y, w, h).build();
    }

    private <T extends AbstractWidget> T deeprebuild$add(T widget) {
        return this.addRenderableWidget(widget);
    }
     *///?} else {
    private ButtonWidget deeprebuild$button(String key, int x, int y, int w, int h, ButtonWidget.PressAction onPress) {
        return ButtonWidget.builder(Text.translatable(key), onPress).dimensions(x, y, w, h).build();
    }

    private <T extends ClickableWidget> T deeprebuild$add(T widget) {
        return this.addDrawableChild(widget);
    }
    //?}

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
        this.deeprebuild$add(this.deeprebuild$button(DeepRebuildPreset.CONFIG_ONLY.translationKey, x, y, w, 20, b -> {
            this.plan.applyPreset(DeepRebuildPreset.CONFIG_ONLY);
            this.rowsDirty = true;
        }));
        this.deeprebuild$add(this.deeprebuild$button(DeepRebuildPreset.CONFIG_AND_RESOURCES.translationKey, x + w + gap, y, w, 20, b -> {
            this.plan.applyPreset(DeepRebuildPreset.CONFIG_AND_RESOURCES);
            this.rowsDirty = true;
        }));
        this.deeprebuild$add(this.deeprebuild$button(DeepRebuildPreset.ALL.translationKey, x + (w + gap) * 2, y, w, 20, b -> {
            this.plan.applyPreset(DeepRebuildPreset.ALL);
            this.rowsDirty = true;
        }));

        int y2 = this.height - 32;
        this.deeprebuild$add(this.deeprebuild$button("deeprebuild.select_all", x, y2, w, 20, b -> {
            this.plan.selectAll();
        }));
        this.deeprebuild$add(this.deeprebuild$button("deeprebuild.select_none", x + w + gap, y2, w, 20, b -> {
            this.plan.selectNone();
        }));
        this.deeprebuild$add(this.deeprebuild$button("deeprebuild.done", x + (w + gap) * 2, y2, w, 20, b -> this.deeprebuild$closeScreen()));
    }

    /** 关闭本页回到创建世界页（close 是 Yarn 名，26.x 叫 onClose）。 */
    private void deeprebuild$closeScreen() {
        //? if >=26 {
        /*this.onClose();
         *///?} else {
        this.close();
        //?}
    }

    //? if <1.20 {
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
    //?} elif >=1.20 <26 {
    /*@Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.rowsDirty) {
            this.rebuildRows();
        }
        int panelLeft = this.listLeft - 3;
        int panelRight = this.listRight + 9;
        context.fill(panelLeft, this.listTop - 4, panelRight, this.listBottom + 4, 0xB0101018);

        context.enableScissor(this.listLeft, this.listTop, this.listRight, this.listBottom);
        int visibleRows = Math.max(1, (this.listBottom - this.listTop) / ROW_HEIGHT);
        int first = Math.max(0, this.scroll / ROW_HEIGHT);
        int last = Math.min(this.rows.size(), first + visibleRows + 2);
        for (int i = first; i < last; i++) {
            Row row = this.rows.get(i);
            int y = this.listTop + i * ROW_HEIGHT - this.scroll;
            boolean hovered = mouseX >= this.listLeft && mouseX < this.listRight
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if (hovered) {
                context.fill(this.listLeft, y, this.listRight, y + ROW_HEIGHT, 0x35FFFFFF);
            }
            this.drawRow(context, row, y);
        }
        context.disableScissor();

        this.drawScrollbar(context);
        this.drawHeader(context);
    }

    private void drawHeader(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        int total = this.plan.totalCount();
        int selected = this.plan.selectedCount();
        Text subtitle = Text.translatable("deeprebuild.screen.summary",
                selected, total, DeepRebuildUtil.bytes(this.plan.selectedBytes()));
        context.drawCenteredTextWithShadow(this.textRenderer, subtitle, this.width / 2, 28, 0xFF9FD0FF);
        if (this.plan.ignoredCount() > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("deeprebuild.screen.filtered", this.plan.ignoredCount())
                            .formatted(Formatting.DARK_GRAY),
                    this.width / 2, 40, 0xFF707070);
        }
    }

    private void drawScrollbar(DrawContext context) {
        int visible = this.listBottom - this.listTop;
        int content = this.rows.size() * ROW_HEIGHT;
        if (content <= visible) {
            return;
        }
        int barHeight = Math.max(16, visible * visible / content);
        int max = content - visible;
        int barY = this.listTop + (visible - barHeight) * this.scroll / Math.max(1, max);
        context.fill(this.listRight + 2, barY, this.listRight + 7, barY + barHeight, 0xFF6C7FA0);
    }

    private void drawRow(DrawContext context, Row row, int y) {
        FileNode node = row.node;
        int x = this.listLeft + 3 + row.depth * 8;
        if (node.directory) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(node.expanded ? "-" : "+"),
                    x, y + 2, 0xFFB0B8C8);
        }
        int boxX = x + 9;
        boolean on = this.plan.isSelected(node);
        boolean partial = this.plan.isPartiallySelected(node);
        this.drawCheckbox(context, boxX, y + 1, on, partial);
        int textX = boxX + 12;
        int color = node.directory ? 0xFFE8C978 : 0xFFDDDDDD;
        String right = node.directory
                ? this.plan.selectedIn(node) + "/" + node.fileCount
                : DeepRebuildUtil.bytes(node.size);
        int rightWidth = this.textRenderer.getWidth(right);
        context.drawTextWithShadow(this.textRenderer, Text.literal(node.name), textX, y + 2, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal(right),
                this.listRight - 4 - rightWidth, y + 2, 0xFF8C93A0);
    }

    private void drawCheckbox(DrawContext context, int x, int y, boolean on, boolean partial) {
        context.fill(x, y, x + 9, y + 9, 0xFF05050A);
        context.fill(x + 1, y + 1, x + 8, y + 8, on ? 0xFF2F6FD0 : (partial ? 0xFF3D5A85 : 0xFF1B1B22));
        if (on) {
            context.fill(x + 3, y + 4, x + 6, y + 7, 0xFFE8F2FF);
        } else if (partial) {
            context.fill(x + 2, y + 4, x + 7, y + 5, 0xFFD8DEE8);
        }
    }
     *///?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.rowsDirty) {
            this.rebuildRows();
        }
        int panelLeft = this.listLeft - 3;
        int panelRight = this.listRight + 9;
        graphics.fill(panelLeft, this.listTop - 4, panelRight, this.listBottom + 4, 0xB0101018);

        graphics.enableScissor(this.listLeft, this.listTop, this.listRight, this.listBottom);
        int visibleRows = Math.max(1, (this.listBottom - this.listTop) / ROW_HEIGHT);
        int first = Math.max(0, this.scroll / ROW_HEIGHT);
        int last = Math.min(this.rows.size(), first + visibleRows + 2);
        for (int i = first; i < last; i++) {
            Row row = this.rows.get(i);
            int y = this.listTop + i * ROW_HEIGHT - this.scroll;
            boolean hovered = mouseX >= this.listLeft && mouseX < this.listRight
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(this.listLeft, y, this.listRight, y + ROW_HEIGHT, 0x35FFFFFF);
            }
            this.drawRow(graphics, row, y);
        }
        graphics.disableScissor();

        this.drawScrollbar(graphics);
        this.drawHeader(graphics);
    }

    private void drawHeader(GuiGraphicsExtractor graphics) {
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        int total = this.plan.totalCount();
        int selected = this.plan.selectedCount();
        MutableComponent subtitle = Component.translatable("deeprebuild.screen.summary",
                selected, total, DeepRebuildUtil.bytes(this.plan.selectedBytes()));
        graphics.centeredText(this.font, subtitle, this.width / 2, 28, 0xFF9FD0FF);
        if (this.plan.ignoredCount() > 0) {
            graphics.centeredText(this.font,
                    Component.translatable("deeprebuild.screen.filtered", this.plan.ignoredCount())
                            .withStyle(ChatFormatting.DARK_GRAY),
                    this.width / 2, 40, 0xFF707070);
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int visible = this.listBottom - this.listTop;
        int content = this.rows.size() * ROW_HEIGHT;
        if (content <= visible) {
            return;
        }
        int barHeight = Math.max(16, visible * visible / content);
        int max = content - visible;
        int barY = this.listTop + (visible - barHeight) * this.scroll / Math.max(1, max);
        graphics.fill(this.listRight + 2, barY, this.listRight + 7, barY + barHeight, 0xFF6C7FA0);
    }

    private void drawRow(GuiGraphicsExtractor graphics, Row row, int y) {
        FileNode node = row.node;
        int x = this.listLeft + 3 + row.depth * 8;
        if (node.directory) {
            graphics.text(this.font, Component.literal(node.expanded ? "-" : "+"), x, y + 2, 0xFFB0B8C8, true);
        }
        int boxX = x + 9;
        boolean on = this.plan.isSelected(node);
        boolean partial = this.plan.isPartiallySelected(node);
        this.drawCheckbox(graphics, boxX, y + 1, on, partial);
        int textX = boxX + 12;
        int color = node.directory ? 0xFFE8C978 : 0xFFDDDDDD;
        String right = node.directory
                ? this.plan.selectedIn(node) + "/" + node.fileCount
                : DeepRebuildUtil.bytes(node.size);
        int rightWidth = this.font.width(right);
        graphics.text(this.font, Component.literal(node.name), textX, y + 2, color, true);
        graphics.text(this.font, Component.literal(right),
                this.listRight - 4 - rightWidth, y + 2, 0xFF8C93A0, true);
    }

    private void drawCheckbox(GuiGraphicsExtractor graphics, int x, int y, boolean on, boolean partial) {
        graphics.fill(x, y, x + 9, y + 9, 0xFF05050A);
        graphics.fill(x + 1, y + 1, x + 8, y + 8, on ? 0xFF2F6FD0 : (partial ? 0xFF3D5A85 : 0xFF1B1B22));
        if (on) {
            graphics.fill(x + 3, y + 4, x + 6, y + 7, 0xFFE8F2FF);
        } else if (partial) {
            graphics.fill(x + 2, y + 4, x + 7, y + 5, 0xFFD8DEE8);
        }
    }
     *///?}

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

    //? if >=26 {
    /*@Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() != 0) {
            return false;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        return this.deeprebuild$clickList(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.rows.isEmpty()) {
            return false;
        }
        this.scroll = Math.max(0, this.scroll - (int) (verticalAmount * ROW_HEIGHT * 2));
        this.clampScroll();
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
     *///?} elif >=1.21 {
    /*@Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        if (click.button() != 0) {
            return false;
        }
        return this.deeprebuild$clickList(click.x(), click.y());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.rows.isEmpty()) {
            return false;
        }
        this.scroll = Math.max(0, this.scroll - (int) (verticalAmount * ROW_HEIGHT * 2));
        this.clampScroll();
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
     *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        return this.deeprebuild$clickList(mouseX, mouseY);
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
    //?}

    /** 点在文件树里：展开/折叠目录或切换勾选。 */
    private boolean deeprebuild$clickList(double mouseX, double mouseY) {
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

    private static final class Row {
        final FileNode node;
        final int depth;

        Row(FileNode node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }
}
