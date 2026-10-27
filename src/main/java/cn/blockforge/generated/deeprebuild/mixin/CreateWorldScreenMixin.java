package cn.blockforge.generated.deeprebuild.mixin;

import cn.blockforge.generated.deeprebuild.DeepRebuildPlan;
import cn.blockforge.generated.deeprebuild.DeepRebuildService;
import cn.blockforge.generated.deeprebuild.DeepRebuildUtil;
import cn.blockforge.generated.deeprebuild.ExtraFilesScreen;
import cn.blockforge.generated.deeprebuild.GuiLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 深重建模式下的创建世界页面：
 * 多出一个「深重建配置」按钮（打开额外文件勾选页），点「创建新世界」时先弹清单确认。
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    @Final
    WorldCreator worldCreator;

    @Shadow
    private void createLevel() {
        throw new AssertionError();
    }

    @Unique
    private ButtonWidget deeprebuild$configButton;

    @Unique
    private boolean deeprebuild$confirmed;

    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void deeprebuild$init(CallbackInfo ci) {
        this.deeprebuild$configButton = null;
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        DeepRebuildPlan plan = DeepRebuildService.plan();
        if (plan == null) {
            return;
        }
        List<ClickableWidget> bottomRow = new ArrayList<>();
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget && widget.getY() >= this.height - 34) {
                bottomRow.add(widget);
            }
        }
        ButtonWidget button = ButtonWidget.builder(Text.translatable("deeprebuild.config_button"),
                        b -> this.deeprebuild$openConfig())
                .dimensions(this.width / 2 - 155, this.height - 28, 150, 20)
                .build();
        button.setTooltip(Tooltip.of(Text.translatable("deeprebuild.config_button.tooltip",
                plan.selectedCount(), plan.totalCount(), DeepRebuildUtil.bytes(plan.selectedBytes()))));
        this.deeprebuild$configButton = this.addDrawableChild(button);
        bottomRow.add(button);
        bottomRow.sort(Comparator.comparingInt(ClickableWidget::getX));
        GuiLayout.reflow(this.width, bottomRow, 4, 60);
    }

    @Unique
    private void deeprebuild$openConfig() {
        MinecraftClient client = this.client;
        DeepRebuildPlan plan = DeepRebuildService.plan();
        if (client == null || plan == null) {
            return;
        }
        client.setScreen(new ExtraFilesScreen(this, plan));
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void deeprebuild$onClose(CallbackInfo ci) {
        DeepRebuildService.endSession();
    }

    @Inject(method = "createLevel", at = @At("HEAD"), cancellable = true)
    private void deeprebuild$beforeCreate(CallbackInfo ci) {
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        if (this.deeprebuild$confirmed) {
            DeepRebuildService.armCreate(this.worldCreator.getWorldDirectoryName());
            DeepRebuildService.endSession();
            return;
        }
        ci.cancel();
        MinecraftClient client = this.client;
        if (client == null) {
            return;
        }
        client.setScreen(new ConfirmScreen(this::deeprebuild$onConfirm,
                Text.translatable("deeprebuild.confirm.title"),
                this.deeprebuild$buildConfirmMessage(),
                Text.translatable("deeprebuild.confirm.yes"),
                ScreenTexts.CANCEL));
    }

    @Unique
    private void deeprebuild$onConfirm(boolean accepted) {
        MinecraftClient client = this.client;
        if (client == null) {
            return;
        }
        client.setScreen(this);
        if (!accepted) {
            return;
        }
        this.deeprebuild$confirmed = true;
        try {
            this.createLevel();
        } finally {
            this.deeprebuild$confirmed = false;
        }
    }

    @Unique
    private Text deeprebuild$buildConfirmMessage() {
        DeepRebuildPlan plan = DeepRebuildService.plan();
        if (plan == null) {
            return Text.translatable("deeprebuild.chat.nofiles");
        }
        MutableText head = Text.translatable("deeprebuild.confirm.body",
                plan.selectedCount(),
                DeepRebuildUtil.bytes(plan.selectedBytes()),
                plan.worldName()).copy().formatted(Formatting.WHITE);
        List<String> preview = plan.previewLines(8);
        MutableText list = Text.literal("");
        for (String line : preview) {
            list.append(Text.literal("\n  · " + line).formatted(Formatting.GRAY));
        }
        int rest = plan.selectedCount() - preview.size();
        if (rest > 0) {
            list.append(Text.literal("\n  ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("deeprebuild.confirm.more", rest).formatted(Formatting.DARK_GRAY));
        }
        MutableText footer = Text.translatable("deeprebuild.confirm.footer").formatted(Formatting.DARK_AQUA);
        return head.append(list).append(Text.literal("\n\n")).append(footer);
    }
}
