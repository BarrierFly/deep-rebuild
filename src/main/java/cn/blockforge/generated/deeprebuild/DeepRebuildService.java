package cn.blockforge.generated.deeprebuild;

//? if >=26 {
/*import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelResource;
 *///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
//?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 深重建的流程中枢：
 * 选存档 → 扫描清单 → 创建世界页面里勾选 → 确认 → 新存档建好的一瞬间复制 → 聊天栏报账。
 */
public final class DeepRebuildService {
    private static final Object LOCK = new Object();
    private static final int CHAT_DELAY_TICKS = 30;

    private static DeepRebuildPlan session;
    private static PendingJob pending;
    private static Report pendingReport;
    private static int chatDelay;

    private DeepRebuildService() {
    }

    // ------------------------------------------------------------------ 会话

    //? if >=26 {
    /*public static void beginSession(Minecraft minecraft, LevelSummary summary) {
        Path saves = minecraft.getLevelSource().getBaseDir();
        Path root = saves.resolve(summary.getLevelId());
     *///?} else {
    public static void beginSession(MinecraftClient client, LevelSummary summary) {
        Path saves = client.getLevelStorage().getSavesDirectory();
        Path root = saves.resolve(summary.getName());
    //?}
        if (!Files.isDirectory(root)) {
            DeepRebuildClient.LOGGER.warn("找不到存档目录 {}", root);
            session = null;
            return;
        }
        FilterRules rules = FilterRules.fromConfig();
        //? if >=26 {
        /*session = DeepRebuildPlan.build(root, summary.getLevelName(), rules);
         *///?} else {
        session = DeepRebuildPlan.build(root, summary.getDisplayName(), rules);
        //?}
        DeepRebuildClient.LOGGER.info("深重建清单：{} 个文件可复制，共 {}，来源 {}",
                session.totalCount(), DeepRebuildUtil.bytes(session.totalBytes()), root);
    }

    public static void endSession() {
        session = null;
    }

    /** 回到世界列表时取消还没执行完的复制任务，防止误写进之后新建的其它世界。 */
    public static void cancelPending() {
        synchronized (LOCK) {
            pending = null;
        }
    }

    public static boolean hasSession() {
        return session != null;
    }

    public static DeepRebuildPlan plan() {
        return session;
    }

    // -------------------------------------------------------------- 创建世界

    /** 玩家点了「创建世界」，记下目标目录名，等服务器启动时动手。 */
    public static void armCreate(String directoryName) {
        if (session == null) {
            return;
        }
        Collection<Path> files = new ArrayList<>(session.selectedPaths());
        pending = new PendingJob(session.root(), directoryName, files, session.selectedBytes(),
                session.worldName(), System.currentTimeMillis());
        DeepRebuildClient.LOGGER.info("深重建待执行：目标 {}，{} 个文件，共 {}",
                directoryName, files.size(), DeepRebuildUtil.bytes(session.selectedBytes()));
    }

    // ------------------------------------------------------------ 启动时复制

    public static void onServerStarting(MinecraftServer server) {
        PendingJob job;
        synchronized (LOCK) {
            job = pending;
            pending = null;
        }
        if (job == null) {
            return;
        }
        if (System.currentTimeMillis() - job.createdAt > 300_000L) {
            DeepRebuildClient.LOGGER.warn("深重建任务超时，已忽略");
            queueReport(Report.skip(job, SkipReason.TIMEOUT));
            return;
        }
        Path target;
        try {
            //? if >=26 {
            /*// ROOT 常量是「.」，resolve 出来的文件名也会是「.」，规范化后才是真实目录名。
            target = server.getWorldPath(LevelResource.ROOT).normalize();
             *///?} else {
            target = server.getSavePath(WorldSavePath.ROOT);
            //?}
        } catch (Throwable t) {
            DeepRebuildClient.LOGGER.warn("拿不到新存档目录，深重建取消", t);
            queueReport(Report.skip(job, SkipReason.NO_TARGET));
            return;
        }
        SkipReason skip = verifyTarget(target, job);
        if (skip != null) {
            DeepRebuildClient.LOGGER.warn("深重建跳过（{}）：目标 {}", skip.name(), target);
            queueReport(Report.skip(job, skip, target));
            return;
        }
        long maxBytes = DeepRebuildConfig.maxTotalBytes();
        if (maxBytes > 0L && job.totalBytes > maxBytes) {
            DeepRebuildClient.LOGGER.warn("勾选内容 {} 超过上限 {}，深重建跳过",
                    DeepRebuildUtil.bytes(job.totalBytes), DeepRebuildUtil.bytes(maxBytes));
            queueReport(Report.skip(job, SkipReason.TOO_LARGE));
            return;
        }

        long start = System.nanoTime();
        int copied = 0;
        int failed = 0;
        long bytes = 0L;
        for (Path file : job.files) {
            Path destination;
            try {
                destination = target.resolve(job.sourceRoot.relativize(file).toString());
            } catch (IllegalArgumentException e) {
                failed++;
                continue;
            }
            try {
                Path parent = destination.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                long size = Files.size(file);
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                copied++;
                bytes += size;
            } catch (IOException e) {
                failed++;
                DeepRebuildClient.LOGGER.warn("复制 {} 失败", file, e);
            }
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        DeepRebuildClient.LOGGER.info("深重建完成：{} 个文件，{}，用时 {} ms，失败 {} 个",
                copied, DeepRebuildUtil.bytes(bytes), elapsedMs, failed);
        queueReport(new Report(job, copied, bytes, elapsedMs, failed));
    }

    /** 目标存档目录建成的宽限：目录 mtime 必须晚于 armCreate 至少这么多，避免创建瞬间的抖动误判。 */
    private static final long FRESH_SLACK_MS = 2_000L;

    /**
     * 确认目标确实是本次深重建刚建出来的那个存档，返回 {@code null} 表示可以复制。
     *
     * <p>不能只看目录名：原版遇到重名会把目录改成「名字 (2)」，而 {@code getWorldDirectoryName()}
     * / {@code getTargetFolder()} 给的是去重前的名字，比较必然对不上。所以主判据是目录 mtime。</p>
     *
     * <p>这里也<b>不能</b>只判断 {@code playerdata/} 这个目录存不存在。{@code PlayerSaveHandler}
     * 的构造函数里会无条件 {@code playerDataDir.mkdirs()}，而它由 {@code MinecraftServer}
     * 的构造函数通过 {@code createSaveHandler()} 触发，早于 SERVER_STARTING 事件——也就是说
     * 任何新建存档跑到这里时，都已经有一个<b>空的</b> {@code playerdata/}。
     * 所以改为判断「目录里是否真的有玩家存档文件」：只有真的存在 {@code *.dat}
     * 才说明这是玩过的旧存档。</p>
     */
    private static SkipReason verifyTarget(Path target, PendingJob job) {
        Path real;
        try {
            real = target.toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return SkipReason.NO_TARGET;
        }
        Path source;
        try {
            source = job.sourceRoot.toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            source = job.sourceRoot;
        }
        if (real.equals(source)) {
            return SkipReason.SELF;
        }
        if (!Files.isDirectory(real)) {
            return SkipReason.NO_TARGET;
        }
        long modified;
        try {
            modified = Files.getLastModifiedTime(real).toMillis();
        } catch (IOException e) {
            DeepRebuildClient.LOGGER.warn("读不到目标存档目录的修改时间 {}", real, e);
            return SkipReason.NOT_NEW;
        }
        if (modified + FRESH_SLACK_MS < job.createdAt) {
            return SkipReason.NOT_NEW;
        }
        // 目录是刚建的，但里面已经有真正的进度数据，说明落到了别处（或被别的流程写过）。
        // playerdata/ 会被原版提前建成空目录，必须看里面有没有存档文件才算数。
        if (hasPlayerDataFiles(real.resolve("playerdata"))) {
            return SkipReason.HAS_PLAYERDATA;
        }
        if (hasAnyFile(real.resolve("stats"))) {
            return SkipReason.HAS_STATS;
        }
        if (hasAnyFile(real.resolve("advancements"))) {
            return SkipReason.HAS_ADVANCEMENTS;
        }
        return null;
    }

    /** 目录里有没有玩家存档文件（{@code <uuid>.dat}）。空目录或只有 {@code .dat_old} 之类不算。 */
    private static boolean hasPlayerDataFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (java.util.stream.Stream<Path> entries = Files.list(dir)) {
            return entries.anyMatch(path -> {
                String name = path.getFileName().toString();
                return name.endsWith(".dat") && !name.endsWith(".dat_old");
            });
        } catch (IOException e) {
            // 读不出来时按「有数据」处理，宁可跳过也不误覆盖。
            DeepRebuildClient.LOGGER.warn("读不到 {}，按已有玩家数据处理", dir, e);
            return true;
        }
    }

    /** 目录里有没有任何文件（统计 / 进度目录正常新建时是空的，甚至根本不建）。 */
    private static boolean hasAnyFile(Path dir) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (java.util.stream.Stream<Path> entries = Files.list(dir)) {
            return entries.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            DeepRebuildClient.LOGGER.warn("读不到 {}，按已有数据处理", dir, e);
            return true;
        }
    }

    private static void queueReport(Report report) {
        pendingReport = report;
        chatDelay = CHAT_DELAY_TICKS;
    }

    // ------------------------------------------------------------ 聊天栏报账

    //? if >=26 {
    /*public static void onClientTick(Minecraft minecraft) {
        if (pendingReport == null) {
            return;
        }
        if (minecraft.level == null || minecraft.player == null) {
            chatDelay = CHAT_DELAY_TICKS;
            return;
        }
        if (chatDelay > 0) {
            chatDelay--;
            return;
        }
        Report report = pendingReport;
        pendingReport = null;
        if (report == null) {
            return;
        }
        if (report.skipped != null) {
            send(minecraft, Component.translatable("deeprebuild.chat.header").withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable(report.skipped.translationKey()).withStyle(ChatFormatting.RED)));
            if (!report.targetPath.isEmpty()) {
                send(minecraft, Component.literal("  · " + report.targetPath).withStyle(ChatFormatting.DARK_GRAY));
            }
            return;
        }
        if (report.copied == 0 && report.failed == 0) {
            send(minecraft, Component.translatable("deeprebuild.chat.nofiles").withStyle(ChatFormatting.GRAY));
            return;
        }
        MutableComponent line = Component.translatable("deeprebuild.chat.header").withStyle(ChatFormatting.AQUA)
                .append(Component.translatable("deeprebuild.chat.done",
                                report.copied,
                                DeepRebuildUtil.bytes(report.bytes),
                                report.elapsedMs)
                        .withStyle(ChatFormatting.GRAY));
        send(minecraft, line);
        if (report.failed > 0) {
            send(minecraft, Component.translatable("deeprebuild.chat.failed", report.failed).withStyle(ChatFormatting.RED));
        }
        for (String dir : report.topLevels) {
            send(minecraft, Component.literal("  · " + dir).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void send(Minecraft minecraft, Component text) {
        minecraft.gui.hud.getChat().addClientSystemMessage(text);
    }
     *///?} else {
    public static void onClientTick(MinecraftClient client) {
        if (pendingReport == null) {
            return;
        }
        if (client.world == null || client.player == null || client.inGameHud == null) {
            chatDelay = CHAT_DELAY_TICKS;
            return;
        }
        if (chatDelay > 0) {
            chatDelay--;
            return;
        }
        Report report = pendingReport;
        pendingReport = null;
        if (report == null) {
            return;
        }
        if (report.skipped != null) {
            send(client, Text.translatable("deeprebuild.chat.header").formatted(Formatting.AQUA)
                    .append(Text.translatable(report.skipped.translationKey()).formatted(Formatting.RED)));
            if (!report.targetPath.isEmpty()) {
                send(client, Text.literal("  · " + report.targetPath).formatted(Formatting.DARK_GRAY));
            }
            return;
        }
        if (report.copied == 0 && report.failed == 0) {
            send(client, Text.translatable("deeprebuild.chat.nofiles").formatted(Formatting.GRAY));
            return;
        }
        MutableText line = Text.translatable("deeprebuild.chat.header").formatted(Formatting.AQUA)
                .append(Text.translatable("deeprebuild.chat.done",
                                report.copied,
                                DeepRebuildUtil.bytes(report.bytes),
                                report.elapsedMs)
                        .formatted(Formatting.GRAY));
        send(client, line);
        if (report.failed > 0) {
            send(client, Text.translatable("deeprebuild.chat.failed", report.failed).formatted(Formatting.RED));
        }
        for (String dir : report.topLevels) {
            send(client, Text.literal("  · " + dir).formatted(Formatting.DARK_GRAY));
        }
    }

    private static void send(MinecraftClient client, Text text) {
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(text);
        }
    }
    //?}

    // ---------------------------------------------------------------- 数据类

    /** 复制被跳过的原因；每一种都对应一条独立的聊天栏文案，省得再翻日志。 */
    private enum SkipReason {
        /** 目标目录就是源存档本身。 */
        SELF("chat.skipped.self"),
        /** 目标目录不是本次重建新建的（mtime 早于 armCreate）。 */
        NOT_NEW("chat.skipped.not_new"),
        /** 拿不到或找不到目标存档目录。 */
        NO_TARGET("chat.skipped.no_target"),
        /** 目标里已经有玩家数据。 */
        HAS_PLAYERDATA("chat.skipped.playerdata"),
        /** 目标里已经有统计数据。 */
        HAS_STATS("chat.skipped.stats"),
        /** 目标里已经有进度数据。 */
        HAS_ADVANCEMENTS("chat.skipped.advancements"),
        /** 勾选内容超过配置的体积上限。 */
        TOO_LARGE("chat.skipped.too_large"),
        /** 从确认到服务器启动之间拖了太久，任务已作废。 */
        TIMEOUT("chat.skipped.timeout");

        private final String key;

        SkipReason(String key) {
            this.key = "deeprebuild." + key;
        }

        String translationKey() {
            return this.key;
        }
    }

    private static final class PendingJob {
        final Path sourceRoot;
        final String directoryName;
        final List<Path> files;
        final long totalBytes;
        final String worldName;
        final long createdAt;

        PendingJob(Path sourceRoot, String directoryName, Collection<Path> files, long totalBytes,
                   String worldName, long createdAt) {
            this.sourceRoot = sourceRoot;
            this.directoryName = directoryName;
            this.files = new ArrayList<>(files);
            this.totalBytes = totalBytes;
            this.worldName = worldName;
            this.createdAt = createdAt;
        }
    }

    private static final class Report {
        final int copied;
        final long bytes;
        final long elapsedMs;
        final int failed;
        final SkipReason skipped;
        final String targetPath;
        final List<String> topLevels = new ArrayList<>();

        Report(PendingJob job, int copied, long bytes, long elapsedMs, int failed) {
            this.copied = copied;
            this.bytes = bytes;
            this.elapsedMs = elapsedMs;
            this.failed = failed;
            this.skipped = null;
            this.targetPath = "";
            java.util.Set<String> tops = new java.util.LinkedHashSet<>();
            for (Path file : job.files) {
                try {
                    Path rel = job.sourceRoot.relativize(file);
                    if (rel.getNameCount() > 1) {
                        tops.add(rel.getName(0).toString() + "/");
                    }
                } catch (IllegalArgumentException ignored) {
                    // 忽略
                }
                if (tops.size() >= 4) {
                    break;
                }
            }
            this.topLevels.addAll(tops);
        }

        private Report(PendingJob job, SkipReason skipped, String targetPath) {
            this.copied = 0;
            this.bytes = 0L;
            this.elapsedMs = 0L;
            this.failed = 0;
            this.skipped = skipped;
            this.targetPath = targetPath == null ? "" : targetPath;
        }

        static Report skip(PendingJob job, SkipReason reason) {
            return new Report(job, reason, "");
        }

        static Report skip(PendingJob job, SkipReason reason, Path target) {
            return new Report(job, reason, target == null ? "" : target.toString());
        }
    }
}
