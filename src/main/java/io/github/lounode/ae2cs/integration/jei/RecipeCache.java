package io.github.lounode.ae2cs.integration.jei;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = AECSConstants.MODID, value = Dist.CLIENT)
public class RecipeCache {
    private static RecipeMap recipeMap = RecipeMap.EMPTY;

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        recipeMap = RecipeMap.EMPTY;
    }

    public static RecipeMap getRecipeMap() {
        return recipeMap;
    }
}
