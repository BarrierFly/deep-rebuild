package cn.blockforge.generated.deeprebuild.mixin;

import cn.blockforge.generated.deeprebuild.DeepRebuildPlan;
import cn.blockforge.generated.deeprebuild.DeepRebuildService;
import cn.blockforge.generated.deeprebuild.DeepRebuildUtil;
import cn.blockforge.generated.deeprebuild.ExtraFilesScreen;
import cn.blockforge.generated.deeprebuild.GuiLayout;
//? if >=26 {
/*import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
 *///?} else {
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
//?}
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
 *
 * <p>1.19.4~1.21.11 是 worldCreator + createLevel()；26.x 换成 uiState + onCreate()，
 * 官方名也全部不同，所以方法体按两条分支维护。</p>
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    //? if >=26 {
    /*@Shadow
    @Final
    WorldCreationUiState uiState;

    @Shadow
    private void onCreate() {
        throw new AssertionError();
    }

    @Unique
    private Button deeprebuild$configButton;

    @Unique
    private boolean deeprebuild$confirmed;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }
     *///?} else {
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
    //?}

    //? if >=26 {
    /*@Inject(method = "init", at = @At("TAIL"))
    private void deeprebuild$init(CallbackInfo ci) {
        this.deeprebuild$configButton = null;
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        DeepRebuildPlan plan = DeepRebuildService.plan();
        if (plan == null) {
            return;
        }
        int bottomY = -1;
        for (GuiEventListener element : this.children()) {
            if (element instanceof AbstractWidget widget) {
                bottomY = Math.max(bottomY, widget.getY());
            }
        }
        if (bottomY < 0) {
            return;
        }
        Button button = Button.builder(Component.translatable("deeprebuild.config_button"),
                        b -> this.deeprebuild$openConfig())
                .bounds(this.width / 2 - 155, bottomY, 150, 20)
                .build();
        button.setTooltip(Tooltip.create(Component.translatable("deeprebuild.config_button.tooltip",
                plan.selectedCount(), plan.totalCount(), DeepRebuildUtil.bytes(plan.selectedBytes()))));
        this.deeprebuild$configButton = this.addRenderableWidget(button);
        List<AbstractWidget> bottomRow = new ArrayList<>();
        for (GuiEventListener element : this.children()) {
            if (element instanceof AbstractWidget widget && widget.getY() >= bottomY - 2) {
                bottomRow.add(widget);
            }
        }
        bottomRow.sort(Comparator.comparingInt(AbstractWidget::getX));
        GuiLayout.reflow(this.width, bottomRow, 4, 60);
    }
     *///?} else {
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
        int bottomY = -1;
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                bottomY = Math.max(bottomY, widget.getY());
            }
        }
        if (bottomY < 0) {
            return;
        }
        ButtonWidget button = ButtonWidget.builder(Text.translatable("deeprebuild.config_button"),
                        b -> this.deeprebuild$openConfig())
                .dimensions(this.width / 2 - 155, bottomY, 150, 20)
                .build();
        button.setTooltip(Tooltip.of(Text.translatable("deeprebuild.config_button.tooltip",
                plan.selectedCount(), plan.totalCount(), DeepRebuildUtil.bytes(plan.selectedBytes()))));
        this.deeprebuild$configButton = this.addDrawableChild(button);
        List<ClickableWidget> bottomRow = new ArrayList<>();
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget && widget.getY() >= bottomY - 2) {
                bottomRow.add(widget);
            }
        }
        bottomRow.sort(Comparator.comparingInt(ClickableWidget::getX));
        GuiLayout.reflow(this.width, bottomRow, 4, 60);
    }
    //?}

    @Unique
    private void deeprebuild$openConfig() {
        DeepRebuildPlan plan = DeepRebuildService.plan();
        //? if >=26 {
        /*if (this.minecraft == null || plan == null) {
            return;
        }
        this.minecraft.gui.setScreen(new ExtraFilesScreen(this, plan));
         *///?} else {
        MinecraftClient client = this.client;
        if (client == null || plan == null) {
            return;
        }
        client.setScreen(new ExtraFilesScreen(this, plan));
        //?}
    }

    //? if >=26 {
    /*@Inject(method = "onClose", at = @At("HEAD"))
    private void deeprebuild$onClose(CallbackInfo ci) {
        DeepRebuildService.endSession();
    }
     *///?} else {
    @Inject(method = "close", at = @At("HEAD"))
    private void deeprebuild$onClose(CallbackInfo ci) {
        DeepRebuildService.endSession();
    }
    //?}

    //? if >=26 {
    /*@Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
    private void deeprebuild$beforeCreate(CallbackInfo ci) {
        if (!DeepRebuildService.hasSession()) {
            return;
        }
        if (this.deeprebuild$confirmed) {
            DeepRebuildService.armCreate(this.uiState.getTargetFolder());
            DeepRebuildService.endSession();
            return;
        }
        ci.cancel();
        if (this.minecraft == null) {
            return;
        }
        this.minecraft.gui.setScreen(new ConfirmScreen(this::deeprebuild$onConfirm,
                Component.translatable("deeprebuild.confirm.title"),
                this.deeprebuild$buildConfirmMessage(),
                Component.translatable("deeprebuild.confirm.yes"),
                CommonComponents.GUI_CANCEL));
    }

    @Unique
    private void deeprebuild$onConfirm(boolean accepted) {
        if (this.minecraft == null) {
            return;
        }
        this.minecraft.gui.setScreen(this);
        if (!accepted) {
            return;
        }
        this.deeprebuild$confirmed = true;
        try {
            this.onCreate();
        } finally {
            this.deeprebuild$confirmed = false;
        }
    }

    @Unique
    private MutableComponent deeprebuild$buildConfirmMessage() {
        DeepRebuildPlan plan = DeepRebuildService.plan();
        if (plan == null) {
            return Component.translatable("deeprebuild.chat.nofiles").copy();
        }
        MutableComponent head = Component.translatable("deeprebuild.confirm.body",
                plan.selectedCount(),
                DeepRebuildUtil.bytes(plan.selectedBytes()),
                plan.worldName()).copy().withStyle(ChatFormatting.WHITE);
        List<String> preview = plan.previewLines(8);
        MutableComponent list = Component.literal("");
        for (String line : preview) {
            list.append(Component.literal("\n  · " + line).withStyle(ChatFormatting.GRAY));
        }
        int rest = plan.selectedCount() - preview.size();
        if (rest > 0) {
            list.append(Component.literal("\n  ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("deeprebuild.confirm.more", rest).withStyle(ChatFormatting.DARK_GRAY));
        }
        MutableComponent footer = Component.translatable("deeprebuild.confirm.footer").withStyle(ChatFormatting.DARK_AQUA);
        return head.append(list).append(Component.literal("\n\n")).append(footer);
    }
     *///?} else {
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
    private MutableText deeprebuild$buildConfirmMessage() {
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
    //?}
}
