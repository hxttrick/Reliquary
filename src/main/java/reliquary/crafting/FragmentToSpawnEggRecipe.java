package reliquary.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FragmentToSpawnEggRecipe extends ShapelessRecipe {
	public static final Serializer SERIALIZER = new Serializer();
	private final ShapelessRecipe recipeDelegate;

	public FragmentToSpawnEggRecipe(ShapelessRecipe recipeDelegate) {
		super(recipeDelegate.getId(), recipeDelegate.getGroup(), CraftingBookCategory.MISC, recipeDelegate.result, recipeDelegate.getIngredients());
		this.recipeDelegate = recipeDelegate;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		return super.matches(inv, worldIn) && FragmentRecipeHelper.hasOnlyOneFragmentType(inv);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		return FragmentRecipeHelper.getRegistryName(inv).map(FragmentRecipeHelper::getSpawnEggStack)
				.orElse(new ItemStack(FragmentRecipeHelper.FALL_BACK_SPAWN_EGG));
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public static class Serializer implements RecipeSerializer<FragmentToSpawnEggRecipe> {

		@Override
		public FragmentToSpawnEggRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return new FragmentToSpawnEggRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(recipeId, json));
		}

		@Nullable
		@Override
		public FragmentToSpawnEggRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			//noinspection ConstantConditions - shapeless crafting recipe serializer always returns an instance here so no need to check for null
			return new FragmentToSpawnEggRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FragmentToSpawnEggRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe.recipeDelegate);
		}
	}
}
