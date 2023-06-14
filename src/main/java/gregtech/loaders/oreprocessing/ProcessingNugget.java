package gregtech.loaders.oreprocessing;

import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes;
import static gregtech.api.util.GT_Recipe.GT_Recipe_Map.sFluidSolidficationRecipes;
import static gregtech.api.util.GT_RecipeBuilder.SECONDS;
import static gregtech.api.util.GT_RecipeBuilder.TICKS;
import static gregtech.api.util.GT_Utility.calculateRecipeEU;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.SubTag;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_RecipeRegistrator;
import gregtech.api.util.GT_Utility;

public class ProcessingNugget implements gregtech.api.interfaces.IOreRecipeRegistrator {

    public ProcessingNugget() {
        OrePrefixes.nugget.add(this);
    }

    @Override
    public void registerOre(OrePrefixes aPrefix, Materials aMaterial, String aOreDictName, String aModName,
        ItemStack aStack) {

        if (aMaterial.contains(SubTag.SMELTING_TO_GEM)
            && GT_OreDictUnificator.get(OrePrefixes.gem, aMaterial.mSmeltInto, 1L) != null) {
            GT_Values.RA.stdBuilder()
                .itemInputs(GT_Utility.copyAmount(9L, aStack), ItemList.Shape_Mold_Ball.get(0L))
                .itemOutputs(GT_OreDictUnificator.get(OrePrefixes.gem, aMaterial.mSmeltInto, 1L))
                .noFluidInputs()
                .noFluidOutputs()
                .duration(10 * SECONDS)
                .eut(calculateRecipeEU(aMaterial, 2))
                .addTo(sAlloySmelterRecipes);
        }

        if ((!aMaterial.contains(SubTag.SMELTING_TO_GEM))
            && GT_OreDictUnificator.get(OrePrefixes.ingot, aMaterial.mSmeltInto, 1L) != null) {
            GT_Values.RA.stdBuilder()
                .itemInputs(GT_Utility.copyAmount(9L, aStack), ItemList.Shape_Mold_Ingot.get(0L))
                .itemOutputs(GT_OreDictUnificator.get(OrePrefixes.ingot, aMaterial.mSmeltInto, 1L))
                .noFluidInputs()
                .noFluidOutputs()
                .duration(10 * SECONDS)
                .eut(calculateRecipeEU(aMaterial, 2))
                .addTo(sAlloySmelterRecipes);
        }

        if (aMaterial.mStandardMoltenFluid != null) {
            if (!(aMaterial == Materials.AnnealedCopper || aMaterial == Materials.WroughtIron)) {
                GT_Values.RA.stdBuilder()
                    .itemInputs(ItemList.Shape_Mold_Nugget.get(0L))
                    .itemOutputs(GT_OreDictUnificator.get(OrePrefixes.nugget, aMaterial, 1L))
                    .fluidInputs(aMaterial.getMolten(16L))
                    .noFluidOutputs()
                    .duration(16 * TICKS)
                    .eut(calculateRecipeEU(aMaterial, 4))
                    .addTo(sFluidSolidficationRecipes);
            }
        }

        GT_RecipeRegistrator.registerReverseFluidSmelting(aStack, aMaterial, aPrefix.mMaterialAmount, null);
        GT_RecipeRegistrator
            .registerReverseMacerating(aStack, aMaterial, aPrefix.mMaterialAmount, null, null, null, false);
        if (!aMaterial.contains(SubTag.NO_SMELTING)
            && GT_OreDictUnificator.get(OrePrefixes.ingot, aMaterial, 1L) != null) {
            GT_Values.RA.stdBuilder()
                .itemInputs(
                    GT_OreDictUnificator.get(OrePrefixes.ingot, aMaterial, 1L),
                    ItemList.Shape_Mold_Nugget.get(0L))
                .itemOutputs(GT_Utility.copyAmount(9L, aStack))
                .noFluidInputs()
                .noFluidOutputs()
                .duration(5 * SECONDS)
                .eut(calculateRecipeEU(aMaterial, 1))
                .addTo(sAlloySmelterRecipes);
            if (aMaterial.getProcessingMaterialTierEU() < TierEU.IV) {
                GT_ModHandler.addCraftingRecipe(
                    GT_OreDictUnificator.get(OrePrefixes.nugget, aMaterial, 8L),
                    GT_ModHandler.RecipeBits.BUFFERED,
                    new Object[] { "sI ", 'I', OrePrefixes.ingot.get(aMaterial) });
            }
        }
    }
}
