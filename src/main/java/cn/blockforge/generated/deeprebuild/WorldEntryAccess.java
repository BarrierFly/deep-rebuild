package cn.blockforge.generated.deeprebuild;

import net.minecraft.world.level.storage.LevelSummary;

/**
 * 借道 mixin 读取世界列表条目里私有的 LevelSummary（存档目录名就在里面）。
 *
 * <p>这是个 duck 接口，不是 mixin——必须放在 mixin 包外面，
 * mixin 包里的非 mixin 类运行时会被 Mixin 拒绝直接加载。</p>
 */
public interface WorldEntryAccess {
    LevelSummary deeprebuild$levelSummary();
}
