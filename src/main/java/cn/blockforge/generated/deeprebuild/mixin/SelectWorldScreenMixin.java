package cn.blockforge.generated.deeprebuild.mixin;

import cn.blockforge.generated.deeprebuild.DeepRebuildService;
import cn.blockforge.generated.deeprebuild.GuiLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 在单人世界的「重建」按钮右侧并列一个「深重建」按钮：
 * 整排按钮会重新居中排布，空间不足时等比收窄，避免和别的模组加的按钮压在一起。
 */
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    private ButtonWidget recreateButton;

    @Shadow
    private WorldListWidget levelList;

    @Unique
    private ButtonWidget deeprebuild$button;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void deeprebuild$addButton(CallbackInfo ci) {
        // 回到世界选择界面说明上一次深重建已经结束（取消或已完成），清掉残留状态
        DeepRebuildService.endSession();
        DeepRebuildService.cancelPending();
        this.deeprebuild$button = null;
        if (this.recreateButton == null) {
            return;
        }
        int x = this.recreateButton.getX() + this.recreateButton.getWidth() + 4;
        int y = this.recreateButton.getY();
        ButtonWidget button = ButtonWidget.builder(Text.translatable("deeprebuild.button"),
                        b -> this.deeprebuild$onPress())
                .dimensions(x, y, 72, 20)
                .build();
        button.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("deeprebuild.button.tooltip")));
        button.active = this.recreateButton.active;
        this.deeprebuild$button = this.addDrawableChild(button);
        this.deeprebuild$reflow(button);
    }

    @Unique
    private void deeprebuild$reflow(ClickableWidget extra) {
        List<ClickableWidget> row = new ArrayList<>();
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget && widget != extra
                    && widget.getY() == extra.getY()) {
                row.add(widget);
            }
        }
        row.add(extra);
        row.sort(Comparator.comparingInt(ClickableWidget::getX));
        GuiLayout.reflow(this.width, row, 4, 40);
    }

    @Unique
    private void deeprebuild$onPress() {
        MinecraftClient client = this.client;
        if (client == null || this.levelList == null) {
            return;
        }
        Optional<WorldListWidget.WorldEntry> optional = this.levelList.getSelectedAsOptional();
        if (optional.isEmpty()) {
            return;
        }
        WorldListWidget.WorldEntry entry = optional.get();
        LevelSummary summary = ((WorldEntryAccess) (Object) entry).deeprebuild$levelSummary();
        if (summary == null) {
            return;
        }
        DeepRebuildService.beginSession(client, summary);
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.45F));
        // 沿用原版「重建」：世界类型、生成器、数据包等设置全部带过去，然后再叠加深重建
        entry.recreate();
    }

    /** 深蓝粒子：按钮附近漂浮的细小亮点，悬停时更亮更密。 */
    @Inject(method = "render", at = @At("TAIL"))
    private void deeprebuild$renderParticles(MatrixStack matrices, int mouseX, int mouseY, float delta,
                                             CallbackInfo ci) {
        ButtonWidget button = this.deeprebuild$button;
        if (button == null || !button.visible) {
            return;
        }
        boolean hovered = button.isMouseOver(mouseX, mouseY);
        int count = hovered ? 26 : 16;
        long time = System.currentTimeMillis();
        int baseX = button.getX();
        int baseY = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();
        for (int i = 0; i < count; i++) {
            double phase = time / 900.0D + i * 0.73D;
            double rise = ((time / 2200.0D + i * 0.37D) % 1.0D);
            int px = baseX + (int) ((0.5D + 0.5D * Math.sin(phase * 1.7D + i)) * width);
            int py = baseY + height + 2 - (int) (rise * (height + 12));
            int size = (i % 3 == 0) ? 2 : 1;
            int alpha = (int) (150.0D * (1.0D - rise)) + (hovered ? 70 : 30);
            alpha = Math.max(0, Math.min(255, alpha));
            int tint = (i % 4 == 0) ? 0x6FA8FF : 0x2A5FD0;
            int color = (alpha << 24) | tint;
            fill(matrices, px, py, px + size, py + size, color);
        }
    }

    @Inject(method = "worldSelected", at = @At("TAIL"))
    private void deeprebuild$syncActive(boolean buttonsActive, boolean deleteButtonActive, CallbackInfo ci) {
        if (this.deeprebuild$button != null && this.recreateButton != null) {
            this.deeprebuild$button.active = this.recreateButton.active;
        }
    }
}
