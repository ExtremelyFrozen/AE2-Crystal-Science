package io.github.lounode.ae2cs.datagen;

import appeng.client.api.model.parts.ClientPart;
import appeng.client.api.model.parts.CompositePartModel;
import appeng.client.api.model.parts.PartModel;
import appeng.client.api.model.parts.StaticPartModel;
import appeng.client.model.LevelEmitterPartModel;
import appeng.client.model.StatusIndicatorPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartItem;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSParts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AECSPartModelProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public AECSPartModelProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "ae2/parts");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<Identifier, ClientPart> parts = new LinkedHashMap<>();

        register(parts, AECSParts.ENDER_INTERFACE_PART.get(), interfaceLike("part/ender_interface/base"));
        register(parts, AECSParts.EX_ENDER_INTERFACE_PART.get(), interfaceLike("part/ender_interface/extended"));
        register(parts, AECSParts.INTEGRATE_INTERFACE_PART.get(), interfaceLike("part/integrate_interface/base"));
        register(parts, AECSParts.EX_INTEGRATE_INTERFACE_PART.get(), interfaceLike("part/integrate_interface/extended"));
        register(parts, AECSParts.SIMPLE_PATTERN_PROVIDER_PART.get(), patternProviderLike("part/simple_pattern_provider/base"));
        register(parts, AECSParts.METEORITE_PATTERN_PROVIDER_PART.get(), patternProviderLike("part/meteorite_pattern_provider/base"));
        register(parts, AECSParts.RESONATING_PATTERN_PROVIDER_PART.get(), patternProviderLike("part/resonating_pattern_provider/base"));
        register(parts, AECSParts.EX_RESONATING_PATTERN_PROVIDER_PART.get(), patternProviderLike("part/resonating_pattern_provider/extended"));
        register(parts, AECSParts.QUARTZ_OSCILLATOR_CLOCK_PART.get(), quartzOscillatorClock());

        return CompletableFuture.allOf(parts.entrySet()
                .stream()
                .map(entry -> save(output, entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new));
    }

    private void register(Map<Identifier, ClientPart> parts, PartItem<?> part, ClientPart clientPart) {
        parts.put(BuiltInRegistries.ITEM.getKey(part.asItem()), clientPart);
    }

    private CompletableFuture<?> save(CachedOutput output, Identifier id, ClientPart clientPart) {
        JsonElement json = ClientPart.CODEC.encodeStart(JsonOps.INSTANCE, clientPart).getOrThrow();
        return DataProvider.saveStable(output, json, this.pathProvider.json(id));
    }

    private static ClientPart interfaceLike(String baseModel) {
        return composite(
                new StaticPartModel.Unbaked(AE2CrystalScience.makeId(baseModel)),
                interfaceStatusIndicator()
        );
    }

    private static ClientPart patternProviderLike(String baseModel) {
        return composite(
                new StaticPartModel.Unbaked(AE2CrystalScience.makeId(baseModel)),
                interfaceStatusIndicator()
        );
    }

    private static ClientPart quartzOscillatorClock() {
        return composite(
                new LevelEmitterPartModel.Unbaked(
                        AE2CrystalScience.makeId("part/quartz_oscillator_clock/base_on"),
                        AE2CrystalScience.makeId("part/quartz_oscillator_clock/base_off")
                ),
                interfaceStatusIndicator()
        );
    }

    private static StatusIndicatorPartModel.Unbaked interfaceStatusIndicator() {
        return new StatusIndicatorPartModel.Unbaked(
                AppEng.makeId("part/interface_has_channel"),
                AppEng.makeId("part/interface_on"),
                AppEng.makeId("part/interface_off")
        );
    }

    private static ClientPart composite(PartModel.Unbaked... models) {
        return new ClientPart(
                new CompositePartModel.Unbaked(List.of(models)),
                ClientPart.Properties.DEFAULT
        );
    }

    @Override
    public String getName() {
        return "AE2 Crystal Science Part Models";
    }
}
