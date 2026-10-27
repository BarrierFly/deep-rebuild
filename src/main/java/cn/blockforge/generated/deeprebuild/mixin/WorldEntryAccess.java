package cn.blockforge.generated.deeprebuild.mixin;

import net.minecraft.world.level.storage.LevelSummary;

/** 借道 mixin 读取世界列表条目里私有的 LevelSummary（存档目录名就在里面）。 */
public interface WorldEntryAccess {
    LevelSummary deeprebuild$levelSummary();
}
