package cn.blockforge.generated.deeprebuild;

//? if >=26 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
 *///?} else {
import net.minecraft.text.Text;
//?}
//? if <1.20 {
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
//?} elif >=1.20 <26 {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
 *///?}

/**
 * 「深重建」按钮本体：自带按钮周围漂浮的深蓝粒子。
 *
 * <p>粒子跟着按钮自身绘制，不再注入 Screen 的 render：1.21.11 起 SelectWorldScreen
 * 不再声明 render，1.21.11 的 ClickableWidget#render / renderWidget 也都是 final，
 * 26.x 更是抽取式渲染——所以各版本各挑一个非 final 的绘制挂点：
 * 1.19.4/1.20.x 覆写 render，1.21.11 覆写 drawIcon，26.x 覆写 extractContents。
 * 注意：这个类必须放在 mixin 包外面，mixin 包里的非 mixin 类在运行时会被拒绝加载。</p>
 */
//? if >=26 {
/*public class ParticleButton extends Button.Plain {
    public ParticleButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        ParticleDecor.draw(graphics, mouseX, mouseY, this.getX(), this.getY(),
                this.getWidth(), this.getHeight(), this.isHovered);
    }
}
     *///?} elif >=1.21 {
/*public class ParticleButton extends ButtonWidget.Text {
    // 参数用全限定名：类体内简单名 Text 会被父类的 ButtonWidget.Text 嵌套类遮蔽。
    public ParticleButton(int x, int y, int width, int height, net.minecraft.text.Text message, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.drawIcon(context, mouseX, mouseY, deltaTicks);
        ParticleDecor.draw(context, mouseX, mouseY, this.getX(), this.getY(),
                this.getWidth(), this.getHeight(), this.isHovered());
    }
}
 *///?} elif >=1.20 {
/*public class ParticleButton extends ButtonWidget {
    public ParticleButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ParticleDecor.draw(context, mouseX, mouseY, this.getX(), this.getY(),
                this.getWidth(), this.getHeight(), this.isMouseOver(mouseX, mouseY));
    }
}
 *///?} else {
public class ParticleButton extends ButtonWidget {
    public ParticleButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        ParticleDecor.draw(matrices, mouseX, mouseY, this.getX(), this.getY(),
                this.getWidth(), this.getHeight(), this.isMouseOver(mouseX, mouseY));
    }
}
//?}
