# 多版本规划（Stonecutter）

目标：一套源码编译出 **1.19.4 / 1.20.1 / 1.21.11 / 26.3** 四个 Fabric 版本。

> 现状说明：本仓库当前**只有一个可构建目标 1.19.4**（根目录的 `build.gradle`）。
> 本目录里的 `.kts` 是接好的 Stonecutter 脚手架模板 + 版本矩阵，按下面的步骤拷到根目录即可启用；
> 另外三个版本需要逐条核对下面的「版本差异点」，本仓库还没有在它们上面编译过。

## 版本矩阵（2026-02 从 Fabric 官方索引核对）

| 版本 | Java | mappings | Fabric API | 状态 |
| --- | --- | --- | --- | --- |
| 1.19.4 | 17 | Yarn `1.19.4+build.2` | `0.87.2+1.19.4` | ✅ 已编译验证 |
| 1.20.1 | 17 | Yarn `1.20.1+build.10` | `0.92.12+1.20.1` | ⬜ 待适配 |
| 1.21.11 | 21 | Yarn `1.21.11+build.6` | `0.141.6+1.21.11` | ⬜ 待适配 |
| 26.3 | 21+ | 官方映射 `officialMojangMappings()` | `0.165.0+26.3` | ⬜ 待适配 |

要点：**26.3 没有 Yarn 了**（Fabric 索引里该版本的 yarn 为空、intermediary 为 `0.0.0`），
因为游戏从 26.x 起不再混淆。26.3 必须改用 `loom.officialMojangMappings()`，
类名会从 Yarn 的 `net.minecraft.client.gui.screen.world.SelectWorldScreen` 变成官方名
`net.minecraft.client.gui.screens.worldselection.SelectWorldScreen`，改动点集中在两个 mixin 文件。

## 启用步骤

1. 把 `multiversion/settings.gradle.kts` 和 `multiversion/stonecutter.gradle.kts` 拷到仓库根目录。
2. 把根目录的 `build.gradle` 改成 `build.gradle.kts`，版本号从 `gradle.properties` 改成读 Stonecutter 的
   `stonecutter.current.version` 与 `versions/<版本>/gradle.properties`（模板里已按这种结构写好）。
3. `gradle stonecutterGenerate` 生成 `versions/1.19.4`、`versions/1.20.1`、`versions/1.21.11`、`versions/26.3`
   四个子项目；`src/` 保持共享。
4. 用 `//? if 1.20.1 { ... }` 这类 Stonecutter 注释处理版本差异，然后逐个跑
   `gradle "1.21.11:build"` 之类验证。
5. `stonecutter active 1.19.4` 决定默认构建哪个版本。

## 版本差异点（需要逐条核对，不是全部）

1. **渲染 API（1.20.2 起）**：`Screen#render(MatrixStack, int, int, float)` 换成
   `render(DrawContext, int, int, float)`。影响 `ExtraFilesScreen`、两个 mixin 里的 `render` 注入与自绘。
   自绘工具 `fill(MatrixStack, ...)` / `drawTextWithShadow(MatrixStack, ...)` 也一并换成 `DrawContext` 版本。
2. **世界创建界面**：1.20.2 起 `CreateWorldScreen` 用 `WorldCreationContext` 取代 `WorldCreator`，
   `createLevel()` 改名为 `createLevel()`（保留）但字段 `worldCreator` 变成 `getUiState()`/`WorldCreator` 的双层结构。
   本模组用到的是 `worldCreator.getWorldDirectoryName()`，需要按版本换成对应取值方式。
3. **世界列表**：`WorldListWidget` 在 1.20.2 换成 `WorldListWidget` + `LevelListWidget`，
   条目类与 `recreate()` 的位置都变了；1.21.x 里「重建」按钮移到了条目内，需要重新核对注入点。
4. **存档目录**：`LevelStorage#getSavesDirectory()`、`MinecraftServer#getSavePath(WorldSavePath.ROOT)`、
   `ServerLifecycleEvents.SERVER_STARTING` 四个版本都还在，基本可以直接用。
5. **26.3**：官方映射 + 上面前三条的官方名写法（`screens/worldselection/SelectWorldScreen`、
   `QuickPlay`/`WorldOpenFlows` 等）。

## 为什么现在不直接开

根目录必须是单一可构建目标，才能保证 `gradle build` 产出可安装的 1.19.4 jar；
同时开四个子项目会在缺少对应工具链时让整个构建失败。
启用多版本后，产物在各自 `versions/<版本>/build/libs/` 下。
