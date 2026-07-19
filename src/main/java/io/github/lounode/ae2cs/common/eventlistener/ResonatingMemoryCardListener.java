package io.github.lounode.ae2cs.common.eventlistener;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.block.entity.ResonatingPatternProviderBlockEntity;
import io.github.lounode.ae2cs.common.item.ResonatingMemoryCardHelper;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = AECSConstants.MODID)
public final class ResonatingMemoryCardListener {

    private ResonatingMemoryCardListener() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getLevel().getBlockEntity(event.getPos()) instanceof ResonatingPatternProviderBlockEntity provider) {
            ResonatingMemoryCardHelper.tryApplyToBlockEntity(player, provider);
        }
    }
}
