package cn.blockforge.generated.deeprebuild;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

//? if >=26 {
/*import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
 *///?} else {
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
//?}
//? if >=1.21 <26 {
/*import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
 *///?}

/**
 * 深重建会话里把「重建带过来的维度」反查回真正的世界类型预设。
 *
 * <p>原版从维度反推世界类型（{@code WorldPresets.getWorldPreset / fromSettings}）只认
 * 超平坦和调试模式，其余一律按「普通」处理：1.19.4~1.20.1 表现为界面标签不对；
 * 1.21.11/26.3 更严重——初始化时会用「普通」预设的维度把带过来的维度覆盖掉，
 * 大型生物群系、放大化、单生物群系和多数模组世界类型就这样在重建时真的变成普通世界。
 * 这里把携带的维度与 world_preset 注册表里每个预设的维度编码成 JSON 逐一比对：
 * 整套维度完全一致直接命中；只有主世界一致时（1.21.11/26.3，因为它们本来就会覆盖维度）
 * 且唯一命中也采纳，其余情况保持原版行为。</p>
 *
 * <p>只在深重建会话激活时介入，原版「重建」按钮保持原版行为。</p>
 */
public final class DeepRebuildWorldType {
    private DeepRebuildWorldType() {
    }

    //? if >=26 {
    /*public static Optional<ResourceKey<WorldPreset>> match(WorldCreationContext context, WorldDimensions carried,
            Optional<ResourceKey<WorldPreset>> vanilla) {
        if (!DeepRebuildService.hasSession()) {
            return vanilla;
        }
        try {
            RegistryAccess.Frozen registries = context.worldgenLoadContext();
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            TreeMap<String, String> carriedSig = stemSignatures(ops, carried.dimensions());
            if (carriedSig.isEmpty()) {
                return vanilla;
            }
            String overworldId = LevelStem.OVERWORLD.identifier().toString();
            Optional<ResourceKey<WorldPreset>> overworldMatch = Optional.empty();
            boolean ambiguous = false;
            for (var preset : registries.lookupOrThrow(Registries.WORLD_PRESET).listElements().toList()) {
                Optional<ResourceKey<WorldPreset>> key = preset.unwrapKey();
                if (key.isEmpty()) {
                    continue;
                }
                TreeMap<String, String> sig = stemSignatures(ops, preset.value().createWorldDimensions().dimensions());
                if (sig.equals(carriedSig)) {
                    return key;
                }
                String overworld = sig.get(overworldId);
                if (overworld != null && overworld.equals(carriedSig.get(overworldId))) {
                    if (overworldMatch.isEmpty()) {
                        overworldMatch = key;
                    } else {
                        ambiguous = true;
                    }
                }
            }
            return ambiguous ? vanilla : overworldMatch;
        } catch (Throwable t) {
            DeepRebuildClient.LOGGER.warn("深重建匹配世界类型失败，保持原版行为", t);
            return vanilla;
        }
    }

    private static TreeMap<String, String> stemSignatures(RegistryOps<JsonElement> ops,
            Map<ResourceKey<LevelStem>, LevelStem> dimensions) {
        TreeMap<String, String> sig = new TreeMap<>();
        dimensions.forEach((key, stem) -> addSignature(sig, key.identifier().toString(),
                LevelStem.CODEC.encodeStart(ops, stem).result().map(JsonElement::toString)));
        return sig;
    }
     *///?} elif >=1.21 {
    /*public static Optional<RegistryKey<WorldPreset>> match(GeneratorOptionsHolder holder,
            DimensionOptionsRegistryHolder carried, Optional<RegistryKey<WorldPreset>> vanilla) {
        if (!DeepRebuildService.hasSession()) {
            return vanilla;
        }
        try {
            DynamicRegistryManager manager = holder.getCombinedRegistryManager();
            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, manager);
            TreeMap<String, String> carriedSig = dimensionSignatures(ops, carried.dimensions());
            if (carriedSig.isEmpty()) {
                return vanilla;
            }
            String overworldId = DimensionOptions.OVERWORLD.getValue().toString();
            Optional<RegistryKey<WorldPreset>> overworldMatch = Optional.empty();
            boolean ambiguous = false;
            for (RegistryEntry.Reference<WorldPreset> preset
                    : manager.getOrThrow(RegistryKeys.WORLD_PRESET).streamEntries().toList()) {
                Optional<RegistryKey<WorldPreset>> key = preset.getKey();
                if (key.isEmpty()) {
                    continue;
                }
                TreeMap<String, String> sig = dimensionSignatures(ops, preset.value().createDimensionsRegistryHolder().dimensions());
                if (sig.equals(carriedSig)) {
                    return key;
                }
                String overworld = sig.get(overworldId);
                if (overworld != null && overworld.equals(carriedSig.get(overworldId))) {
                    if (overworldMatch.isEmpty()) {
                        overworldMatch = key;
                    } else {
                        ambiguous = true;
                    }
                }
            }
            return ambiguous ? vanilla : overworldMatch;
        } catch (Throwable t) {
            DeepRebuildClient.LOGGER.warn("深重建匹配世界类型失败，保持原版行为", t);
            return vanilla;
        }
    }

    private static TreeMap<String, String> dimensionSignatures(RegistryOps<JsonElement> ops,
            Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions) {
        TreeMap<String, String> sig = new TreeMap<>();
        dimensions.forEach((key, options) -> addSignature(sig, key.getValue().toString(),
                DimensionOptions.CODEC.encodeStart(ops, options).result().map(JsonElement::toString)));
        return sig;
    }
     *///?} else {
    public static Optional<RegistryKey<WorldPreset>> match(GeneratorOptionsHolder holder,
            Registry<DimensionOptions> carried, Optional<RegistryKey<WorldPreset>> vanilla) {
        if (!DeepRebuildService.hasSession()) {
            return vanilla;
        }
        try {
            DynamicRegistryManager manager = holder.getCombinedRegistryManager();
            RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, manager);
            TreeMap<String, String> carriedSig = dimensionSignatures(ops, carried);
            if (carriedSig.isEmpty()) {
                return vanilla;
            }
            // 1.19.4/1.20.1 反推为空时原版会保留携带的维度不动，所以只在整套维度
            // 与某个预设完全一致时才纠正标签；其余情况保持原版行为，避免覆盖玩家自定义的维度。
            for (RegistryEntry.Reference<WorldPreset> preset
                    : manager.get(RegistryKeys.WORLD_PRESET).streamEntries().toList()) {
                Optional<RegistryKey<WorldPreset>> key = preset.getKey();
                if (key.isEmpty()) {
                    continue;
                }
                if (dimensionSignatures(ops, preset.value().createDimensionsRegistryHolder().dimensions())
                        .equals(carriedSig)) {
                    return key;
                }
            }
            return vanilla;
        } catch (Throwable t) {
            DeepRebuildClient.LOGGER.warn("深重建匹配世界类型失败，保持原版行为", t);
            return vanilla;
        }
    }

    private static TreeMap<String, String> dimensionSignatures(RegistryOps<JsonElement> ops,
            Registry<DimensionOptions> dimensions) {
        TreeMap<String, String> sig = new TreeMap<>();
        dimensions.streamEntries().forEach(entry -> addSignature(sig,
                entry.getKey().map(key -> key.getValue().toString()).orElse(""),
                DimensionOptions.CODEC.encodeStart(ops, entry.value()).result().map(JsonElement::toString)));
        return sig;
    }
    //?}

    private static void addSignature(TreeMap<String, String> sig, String id, Optional<String> encoded) {
        if (!id.isEmpty() && encoded.isPresent()) {
            sig.put(id, encoded.get());
        }
    }
}
