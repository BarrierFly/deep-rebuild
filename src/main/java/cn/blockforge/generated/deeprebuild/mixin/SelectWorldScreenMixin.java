package cn.blockforge.generated.deeprebuild.mixin;

import cn.blockforge.generated.deeprebuild.DeepRebuildService;
import cn.blockforge.generated.deeprebuild.GuiLayout;
import cn.blockforge.generated.deeprebuild.ParticleButton;
import cn.blockforge.generated.deeprebuild.WorldEntryAccess;
//? if >=26 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.LevelSummary;
 *///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
//?}
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
 *
 * <p>粒子画在 {@link ParticleButton} 自己身上——1.21.11 起 SelectWorldScreen 不再声明
 * render、26.x 连 Screen 级 render 都没了，所以这里不能注入任何绘制方法。</p>
 */
@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    //? if >=26 {
    /*@Shadow
    private Button recreateButton;

    @Shadow
    private WorldSelectionList list;
     *///?} else {
    @Shadow
    private ButtonWidget recreateButton;

    @Shadow
    private WorldListWidget levelList;
    //?}

    @Unique
    private ParticleButton deeprebuild$button;

    //? if >=26 {
    /*protected SelectWorldScreenMixin(Component title) {
        super(title);
    }
     *///?} else {
    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }
    //?}

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
        //? if >=26 {
        /*ParticleButton button = new ParticleButton(x, y, 72, 20,
                Component.translatable("deeprebuild.button"), b -> this.deeprebuild$onPress());
        button.setTooltip(Tooltip.create(Component.translatable("deeprebuild.button.tooltip")));
        button.active = this.recreateButton.active;
        this.deeprebuild$button = this.addRenderableWidget(button);
         *///?} else {
        ParticleButton button = new ParticleButton(x, y, 72, 20,
                Text.translatable("deeprebuild.button"), b -> this.deeprebuild$onPress());
        button.setTooltip(Tooltip.of(Text.translatable("deeprebuild.button.tooltip")));
        button.active = this.recreateButton.active;
        this.deeprebuild$button = this.addDrawableChild(button);
        //?}
        this.deeprebuild$reflow(button);
    }

    //? if >=26 {
    /*@Unique
    private void deeprebuild$reflow(AbstractWidget extra) {
        List<AbstractWidget> row = new ArrayList<>();
        for (GuiEventListener element : this.children()) {
            if (element instanceof AbstractWidget widget && widget != extra
                    && widget.getY() == extra.getY()) {
                row.add(widget);
            }
        }
        row.add(extra);
        row.sort(Comparator.comparingInt(AbstractWidget::getX));
        GuiLayout.reflow(this.width, row, 4, 40);
    }
     *///?} else {
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
    //?}

    @Unique
    private void deeprebuild$onPress() {
        //? if >=26 {
        /*Minecraft minecraft = this.minecraft;
        if (minecraft == null || this.list == null) {
            return;
        }
        Optional<WorldSelectionList.WorldListEntry> optional = this.list.getSelectedOpt();
        if (optional.isEmpty()) {
            return;
        }
        WorldSelectionList.WorldListEntry entry = optional.get();
        LevelSummary summary = ((WorldEntryAccess) (Object) entry).deeprebuild$levelSummary();
        if (summary == null) {
            return;
        }
        DeepRebuildService.beginSession(minecraft, summary);
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 0.45F));
        // 沿用原版「重建」：世界类型、生成器、数据包等设置全部带过去，然后再叠加深重建
        entry.recreateWorld();
         *///?} elif >=1.21 {
        /*MinecraftClient client = this.client;
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
        client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.45F));
        // 沿用原版「重建」：世界类型、生成器、数据包等设置全部带过去，然后再叠加深重建
        entry.recreate();
         *///?} else {
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
        //?}
    }

    //? if >=26 {
    /*@Inject(method = "updateButtonStatus", at = @At("TAIL"))
    private void deeprebuild$syncActive(LevelSummary summary, CallbackInfo ci) {
        if (this.deeprebuild$button != null && this.recreateButton != null) {
            this.deeprebuild$button.active = this.recreateButton.active;
        }
    }
     *///?} elif >=1.21 {
    /*@Inject(method = "worldSelected", at = @At("TAIL"))
    private void deeprebuild$syncActive(LevelSummary summary, CallbackInfo ci) {
        if (this.deeprebuild$button != null && this.recreateButton != null) {
            this.deeprebuild$button.active = this.recreateButton.active;
        }
    }
     *///?} else {
    @Inject(method = "worldSelected", at = @At("TAIL"))
    private void deeprebuild$syncActive(boolean buttonsActive, boolean deleteButtonActive, CallbackInfo ci) {
        if (this.deeprebuild$button != null && this.recreateButton != null) {
            this.deeprebuild$button.active = this.recreateButton.active;
        }
    }
    //?}
}
