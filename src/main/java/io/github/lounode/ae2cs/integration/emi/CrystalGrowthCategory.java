package io.github.lounode.ae2cs.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.lounode.ae2cs.AE2CrystalScience;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.item.CrystalSeedItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class CrystalGrowthCategory extends BasicEmiRecipe
{
    public static final EmiRecipeCategory RECIPE_TYPE = new EmiRecipeCategory(AE2CrystalScience.makeId("crystal_growth"),
            EmiStack.of(AECSBlocks.CRYSTAL_GROWTH_CHAMBER_BLOCK))
    {
        @Override
        public Component getName()
        {
            return Component.translatable("ae2cs.integration.jei.recipe_category.crystal_growth");
        }
    };

    public static final ResourceLocation BG = AE2CrystalScience.makeId("textures/gui/recipe/crystal_growth.png");
    private static final int W = 135;
    private static final int H = 58;

    public CrystalGrowthCategory(CrystalSeedItem seedItem)
    {
        super(RECIPE_TYPE, getGrowthId(seedItem), 135, 58);

        this.inputs.add(EmiStack.of(seedItem));
        this.outputs.add(EmiStack.of(seedItem.getGrowTo()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        widgets.addTexture(BG, 0, 0, W, H, 0, 0, W, H, 256, 256);

        int xIn = 22;
        int yIn = 21;
        widgets.addSlot(this.inputs.get(0), xIn, yIn).drawBack(false);

        int xOut = 85;
        int yOut = yIn;
        widgets.addSlot(this.outputs.get(0), xOut, yOut).recipeContext(this).drawBack(false);

        widgets.addFillingArrow(53, 22, 5000);
    }

    public static ResourceLocation getGrowthId(CrystalSeedItem item)
    {
        return AE2CrystalScience.makeId("crystal_growth/" + getItemName(item) + "_from_" + getItemName(item.getGrowTo()));
    }

    protected static String getItemName(ItemLike itemLike)
    {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
    }
}