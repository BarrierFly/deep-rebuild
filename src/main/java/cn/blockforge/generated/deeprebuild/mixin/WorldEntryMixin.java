package cn.blockforge.generated.deeprebuild.mixin;

//? if >=26 {
/*import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
 *///?} else {
import net.minecraft.client.gui.screen.world.WorldListWidget;
//?}
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** 暴露世界列表条目里的 LevelSummary，用来定位存档目录。 */
//? if >=26 {
/*@Mixin(WorldSelectionList.WorldListEntry.class)
 *///?} else {
@Mixin(WorldListWidget.WorldEntry.class)
//?}
public abstract class WorldEntryMixin implements WorldEntryAccess {
    //? if >=26 {
    /*@Shadow
    private LevelSummary summary;

    @Override
    public LevelSummary deeprebuild$levelSummary() {
        return this.summary;
    }
     *///?} else {
    @Shadow
    private LevelSummary level;

    @Override
    public LevelSummary deeprebuild$levelSummary() {
        return this.level;
    }
    //?}
}
