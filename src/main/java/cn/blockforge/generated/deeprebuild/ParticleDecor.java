package cn.blockforge.generated.deeprebuild;

//? if <1.20 {
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
//?} elif >=1.20 <26 {
/*import net.minecraft.client.gui.DrawContext;
 *///?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
 *///?}

/**
 * 「深重建」按钮周围漂浮的深蓝粒子。绘制入口的参数类型跟着版本走：
 * 1.19.4 是 MatrixStack + Screen 自带的 fill，1.20 起换成 DrawContext，26 起渲染改为抽取式（GuiGraphicsExtractor）。
 */
public final class ParticleDecor {
    private ParticleDecor() {
    }

    //? if <1.20 {
    public static void draw(MatrixStack matrices, int mouseX, int mouseY,
                            int baseX, int baseY, int width, int height, boolean hovered) {
        int count = hovered ? 26 : 16;
        long time = System.currentTimeMillis();
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
            DrawableHelper.fill(matrices, px, py, px + size, py + size, color);
        }
    }
    //?} elif >=1.20 <26 {
    /*public static void draw(DrawContext context, int mouseX, int mouseY,
                            int baseX, int baseY, int width, int height, boolean hovered) {
        int count = hovered ? 26 : 16;
        long time = System.currentTimeMillis();
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
            context.fill(px, py, px + size, py + size, color);
        }
    }
    *///?} else {
    /*public static void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                            int baseX, int baseY, int width, int height, boolean hovered) {
        int count = hovered ? 26 : 16;
        long time = System.currentTimeMillis();
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
            graphics.fill(px, py, px + size, py + size, color);
        }
    }
     *///?}
}
