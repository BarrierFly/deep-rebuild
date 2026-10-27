package cn.blockforge.generated.deeprebuild.mixin;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** 暴露世界列表条目里的 LevelSummary，用来定位存档目录。 */
@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin implements WorldEntryAccess {
    @Shadow
    private LevelSummary level;

    @Override
    public LevelSummary deeprebuild$levelSummary() {
        return this.level;
    }
}
