package cn.blockforge.generated.deeprebuild;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 深重建（Deep Rebuild）客户端入口。
 *
 * <p>这是个纯客户端模组：只动单人世界的「重建」按钮和创建世界页面，
 * 复制文件发生在集成服务器启动前的一瞬间。</p>
 */
public final class DeepRebuildClient implements ClientModInitializer {
    public static final String MOD_ID = "deeprebuild";
    public static final Logger LOGGER = LoggerFactory.getLogger("DeepRebuild");

    @Override
    public void onInitializeClient() {
        DeepRebuildConfig.load(FabricLoader.getInstance().getConfigDir());
        // 新存档的目录刚建好、世界数据还没写进去的时机，把勾选的额外文件搬过去。
        ServerLifecycleEvents.SERVER_STARTING.register(DeepRebuildService::onServerStarting);
        ClientTickEvents.END_CLIENT_TICK.register(DeepRebuildService::onClientTick);
    }
}
