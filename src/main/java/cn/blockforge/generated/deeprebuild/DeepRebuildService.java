package cn.blockforge.generated.deeprebuild;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
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

    public static void beginSession(MinecraftClient client, LevelSummary summary) {
        Path saves = client.getLevelStorage().getSavesDirectory();
        Path root = saves.resolve(summary.getName());
        if (!Files.isDirectory(root)) {
            DeepRebuildClient.LOGGER.warn("找不到存档目录 {}", root);
            session = null;
            return;
        }
        FilterRules rules = FilterRules.fromConfig();
        session = DeepRebuildPlan.build(root, summary.getDisplayName(), rules);
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
            return;
        }
        Path target;
        try {
            target = server.getSavePath(WorldSavePath.ROOT);
        } catch (Throwable t) {
            DeepRebuildClient.LOGGER.warn("拿不到新存档目录，深重建取消", t);
            return;
        }
        String dirName = target.getFileName() == null ? "" : target.getFileName().toString();
        boolean sameName = dirName.equals(job.directoryName);
        if (!sameName && !looksFresh(target)) {
            DeepRebuildClient.LOGGER.warn("目标存档 {} 不是新建的，深重建跳过", target);
            queueReport(new Report(job, 0, 0L, 0L, 0, true));
            return;
        }
        long maxBytes = DeepRebuildConfig.maxTotalBytes();
        if (maxBytes > 0L && job.totalBytes > maxBytes) {
            DeepRebuildClient.LOGGER.warn("勾选内容 {} 超过上限 {}，深重建跳过",
                    DeepRebuildUtil.bytes(job.totalBytes), DeepRebuildUtil.bytes(maxBytes));
            queueReport(new Report(job, 0, 0L, 0L, 0, true));
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
        queueReport(new Report(job, copied, bytes, elapsedMs, failed, false));
    }

    /** 新建的存档里一定还没有玩家数据、统计和进度。 */
    private static boolean looksFresh(Path target) {
        return !Files.exists(target.resolve("playerdata"))
                && !Files.exists(target.resolve("stats"))
                && !Files.exists(target.resolve("advancements"));
    }

    private static void queueReport(Report report) {
        pendingReport = report;
        chatDelay = CHAT_DELAY_TICKS;
    }

    // ------------------------------------------------------------ 聊天栏报账

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
        if (report.aborted) {
            send(client, Text.translatable("deeprebuild.chat.skipped").formatted(Formatting.RED));
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

    // ---------------------------------------------------------------- 数据类

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
        final boolean aborted;
        final List<String> topLevels = new ArrayList<>();

        Report(PendingJob job, int copied, long bytes, long elapsedMs, int failed, boolean aborted) {
            this.copied = copied;
            this.bytes = bytes;
            this.elapsedMs = elapsedMs;
            this.failed = failed;
            this.aborted = aborted;
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
    }
}
