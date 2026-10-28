package cn.blockforge.generated.deeprebuild;

//? if >=26 {
/*import net.minecraft.client.gui.components.AbstractWidget;
 *///?} else {
import net.minecraft.client.gui.widget.ClickableWidget;
//?}

import java.util.List;

/** 按钮重排：把一排按钮重新居中，空间不够就等比压窄，避免和其他模组的按钮叠在一起。 */
public final class GuiLayout {
    private GuiLayout() {
    }

    //? if >=26 {
    /*public static void reflow(int screenWidth, List<AbstractWidget> row, int gap, int minWidth) {
        if (row.isEmpty()) {
            return;
        }
        int usable = Math.max(40, screenWidth - 8);
        long sum = 0L;
        for (AbstractWidget widget : row) {
            sum += widget.getWidth();
        }
        long total = sum + (long) gap * (row.size() - 1);
        if (total > usable) {
            int gaps = gap * (row.size() - 1);
            int available = Math.max(20 * row.size(), usable - gaps);
            double scale = (double) available / (double) sum;
            for (AbstractWidget widget : row) {
                int width = Math.max(minWidth, (int) Math.floor(widget.getWidth() * scale));
                widget.setWidth(width);
            }
            total = 0L;
            for (AbstractWidget widget : row) {
                total += widget.getWidth();
            }
            total += gaps;
        }
        int x = (screenWidth - (int) total) / 2;
        for (AbstractWidget widget : row) {
            widget.setX(x);
            x += widget.getWidth() + gap;
        }
    }
     *///?} else {
    public static void reflow(int screenWidth, List<ClickableWidget> row, int gap, int minWidth) {
        if (row.isEmpty()) {
            return;
        }
        int usable = Math.max(40, screenWidth - 8);
        long sum = 0L;
        for (ClickableWidget widget : row) {
            sum += widget.getWidth();
        }
        long total = sum + (long) gap * (row.size() - 1);
        if (total > usable) {
            int gaps = gap * (row.size() - 1);
            int available = Math.max(20 * row.size(), usable - gaps);
            double scale = (double) available / (double) sum;
            for (ClickableWidget widget : row) {
                int width = Math.max(minWidth, (int) Math.floor(widget.getWidth() * scale));
                widget.setWidth(width);
            }
            total = 0L;
            for (ClickableWidget widget : row) {
                total += widget.getWidth();
            }
            total += gaps;
        }
        int x = (screenWidth - (int) total) / 2;
        for (ClickableWidget widget : row) {
            widget.setX(x);
            x += widget.getWidth() + gap;
        }
    }
    //?}
}
