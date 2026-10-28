# 深重建 Deep Rebuild

给 Minecraft **1.19.4 / Fabric** 做的一个客户端小模组：在单人世界列表里，「重建」按钮右边多一个「深重建」按钮。

原版「重建」只会带走世界类型、生成器设置、数据包这些写在 `level.dat` 里的东西，
存档目录里其它模组自己加的配置（比如各种 `config/`、`scripts/`、`datapacks/`、`resources.zip`）
一律丢掉。深重建就是在原版重建的基础上，把这些**与游戏进度无关**的额外文件也一并复制到新存档里。

## 用法

1. 单人游戏 → 选中一个世界 → 点「重建」旁边的 **深重建**（按钮周围有深蓝粒子，点击有低音提示）。
2. 会像原版重建一样打开「创建新世界」页面（世界类型、生成器、数据包都已带过来）。
   页面底部的 **深重建配置** 按钮会打开「额外导入」页。
3. 「额外导入」页是一棵可勾选的文件树：
   - 默认全选（进度数据已经被过滤掉了）；
   - 三档预设：**仅配置** / **配置+资源** / **全部**；
   - 目录可以点 `+` / `-` 展开折叠，点勾选框是整目录一起勾选；
   - 顶部实时显示「已选 N / M 项 · 体积」。
4. 点「创建新世界」时会先弹一个确认清单，列出要复制的文件和总大小，确认后才真正开始。
5. 世界建好、进入游戏后，聊天栏会输出一行统计：
   ```
   [深重建] 已额外复制 23 个文件，共 1.24 MB，用时 41 ms
   ```

## 会被忽略的东西（内置黑名单）

这些是跟游戏进度强相关的，深重建永远不复制：

| 内容 | 原因 |
| --- | --- |
| `level.dat`、`level.dat_old` | 世界时间、种子、游戏规则等，原版重建自己会处理 |
| `playerdata/**` | 玩家数据（背包、坐标、经验） |
| `stats/**`、`advancements/**` | 统计信息与进度 |
| `icon.png` | 世界缩略图 |
| `region/**`、`entities/**`、`poi/**`、`DIM-1/**`、`DIM1/**`、`dimensions/**` | 方块与实体数据 |
| `data/**`、`session.lock` | 世界运行时数据与锁文件 |

**可扩展**：首次启动会在 `config/deeprebuild.json` 生成配置文件，可以按通配符追加规则：

```json
{
  "_comment": "在 extra_blacklist 里按通配符追加要跳过的文件（例如 my_mod/cache/**），extra_whitelist 的优先级更高。",
  "extra_blacklist": ["my_mod/cache/**"],
  "extra_whitelist": ["resources.zip"],
  "max_total_bytes": 2147483648
}
```

通配符：`*` 匹配一层里的任意字符，`**` 匹配任意层目录，`?` 匹配单个字符。
白名单（`extra_whitelist`）命中时优先于黑名单。`max_total_bytes` 是单次复制的体积上限，超过就放弃复制并提示。

## 兼容性说明

- 纯客户端模组，安装到客户端即可，服务器端不需要装。
- 入口按钮会跟原版那一排按钮一起重新居中排布；别的模组也在同一行加按钮时，会自动收窄而不是叠在一起。
- 若目标存档目录名和预期不一致（例如名字重复被改名），只有在目标看起来是全新存档（没有 `playerdata`/`stats`/`advancements`）时才会复制，否则跳过并提示。

## 构建

```bash
./gradlew build             # 构建全部四个版本
./gradlew :1.21.11:build    # 只构建某一个版本
```

仓库自带 Gradle wrapper（Gradle 9.8.2）。构建机需要 Java 21+（运行 Gradle 用）；
1.19.4 / 1.20.1 会以 `--release 17` 编译，1.21.11 / 26.3 以 `--release 21` 编译，
不需要额外安装 JDK 17。产物在 `versions/<版本>/build/libs/deeprebuild-1.0.0-r1+<版本>.jar`。

## 多版本（Stonecutter）

一套源码通过 [Stonecutter](https://stonecutter.kikugie.dev/)（0.9.8）编译出
**1.19.4 / 1.20.1 / 1.21.11 / 26.3** 四个 Fabric 版本，四个目标都已在本仓库编译验证。

- 版本矩阵在 `versions/<版本>/gradle.properties`（mappings、Fabric API、Java 版本等），
  数值与 Fabric 官方索引核对过（2026-10）。
- 版本差异用 Stonecutter 注释处理，例如 `//? if >=1.20 <26 { ... }`；
  共享代码（配置、过滤、文件树、清单）不做分支。
- `stonecutter active "1.19.4"` 决定根项目默认构建哪个版本（也是 IDE 编辑源码的基准版本）。

| 版本 | Java | mappings | Fabric API |
| --- | --- | --- | --- |
| 1.19.4 | 17 | Yarn `1.19.4+build.2` | `0.87.2+1.19.4` |
| 1.20.1 | 17 | Yarn `1.20.1+build.10` | `0.92.12+1.20.1` |
| 1.21.11 | 21 | Yarn `1.21.11+build.6` | `0.141.6+1.21.11` |
| 26.3 | 21 | 官方名（游戏已去混淆，无映射文件） | `0.165.0+26.3` |

### 各版本实际差异（以 guardian 反编译源码核对为准）

1. **渲染**：1.19.4 是 `Screen#render(MatrixStack, ...)` + `DrawableHelper.fill`；
   1.20.1 / 1.21.11 换成 `DrawContext`（注意：1.20 就换了，不是 1.20.2）；
   26.x 渲染整体改为抽取式，`Screen#render` 不复存在，改在
   `extractRenderState(GuiGraphicsExtractor, ...)` 里画（widget 层是 final，按钮子类覆写 `extractContents`）。
2. **输入**：1.21.11 起 `mouseClicked`/`mouseScrolled`/`keyPressed` 换成事件对象
   （Yarn 叫 `Click`，26.x 叫 `MouseButtonEvent`），滚动多了横向参数。
3. **世界列表**：三个 Yarn 版本的 `SelectWorldScreen` / `WorldListWidget.WorldEntry`（字段 `level`）
   / `recreate()` / 底部按钮排完全一致；26.x 换成 `WorldSelectionList.WorldListEntry`（字段 `summary`）、
   `getSelectedOpt()`、`recreateWorld()`，「选中世界」回调从 `worldSelected(boolean, boolean)`
   变成 `worldSelected(LevelSummary)`（26.x 叫 `updateButtonStatus`）。
4. **创建世界**：三个 Yarn 版本一直是 `worldCreator` + `createLevel()` + `getWorldDirectoryName()`
   （此前预期的 1.20.2 重构并没有发生）；26.x 换成 `uiState`（`WorldCreationUiState`）+
   `onCreate()` + `getTargetFolder()`，关闭钩子也从 `close()` 变成 `onClose()`。
5. **声音**：`PositionedSoundInstance.master` 在 1.21.11 改名 `ui`，26.x 是
   `SimpleSoundInstance.forUI` + `SoundEvents.BEACON_DEACTIVATE`。
6. **26.x 其它**：`setScreen` 挪到了 `minecraft.gui` 上，聊天栏报账走
   `minecraft.gui.hud.getChat().addClientSystemMessage`，存档根目录用
   `server.getWorldPath(LevelResource.ROOT)`（记得 `normalize()`，ROOT 常量是「.」），
   依赖按 Fabric 迁移指南改用普通 `implementation`（`versions/26.3` 里已开
   `fabric.loom.disableObfuscation`）。

## 授权

WTFPL v2，见 [LICENSE](LICENSE)。
