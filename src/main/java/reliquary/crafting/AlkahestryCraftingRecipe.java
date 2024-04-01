package reliquary.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import reliquary.init.ModItems;
import reliquary.items.AlkahestryTomeItem;

public class AlkahestryCraftingRecipe implements CraftingRecipe {
	private final Ingredient craftingIngredient;
	private final int chargeNeeded;
	private final int resultCount;
	private ItemStack result = ItemStack.EMPTY;
	private final Ingredient tomeIngredient;

	public AlkahestryCraftingRecipe(Ingredient craftingIngredient, int chargeNeeded, int resultCount) {
		this.craftingIngredient = craftingIngredient;
		this.chargeNeeded = chargeNeeded;
		tomeIngredient = Ingredient.of(AlkahestryTomeItem.setCharge(new ItemStack(ModItems.ALKAHESTRY_TOME.get()), AlkahestryTomeItem.getChargeLimit()));
		this.resultCount = resultCount;

		AlkahestryRecipeRegistry.registerCraftingRecipe(this);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		boolean hasIngredient = false;
		boolean hasTome = false;
		for (int x = 0; x < inv.getContainerSize(); x++) {
			ItemStack slotStack = inv.getItem(x);

			if (!slotStack.isEmpty()) {
				boolean inRecipe = false;
				if (craftingIngredient.test(slotStack)) {
					inRecipe = true;
					hasIngredient = true;
				} else if (!hasTome && slotStack.getItem() == ModItems.ALKAHESTRY_TOME.get() && AlkahestryTomeItem.getCharge(slotStack) >= chargeNeeded) {
					inRecipe = true;
					hasTome = true;
				}

				if (!inRecipe) {
					return false;
				}

			}
		}

		return hasIngredient && hasTome;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, craftingIngredient, tomeIngredient);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack stack = inv.getItem(slot);

			if (!stack.isEmpty() && stack.getItem() != ModItems.ALKAHESTRY_TOME.get()) {
				ItemStack craftingResult = stack.copy();
				craftingResult.setCount(resultCount);
				return craftingResult;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	public ItemStack getResult() {
		if (result.isEmpty()) {
			ItemStack[] ingredientItems = craftingIngredient.getItems();
			if (ingredientItems.length > 0) {
				result = ingredientItems[0].copy();
				result.setCount(resultCount);
			}
		}

		return result;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return getResult();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.ALKAHESTRY_CRAFTING_SERIALIZER.get();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> remainingItems = CraftingRecipe.super.getRemainingItems(inv);

		addTomeWithUsedCharge(remainingItems, inv);

		return remainingItems;
	}

	private void addTomeWithUsedCharge(NonNullList<ItemStack> remainingItems, CraftingContainer inv) {
		for (int slot = 0; slot < remainingItems.size(); slot++) {
			ItemStack stack = inv.getItem(slot);

			if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				ItemStack tome = stack.copy();
				AlkahestryTomeItem.useCharge(tome, chargeNeeded);
				remainingItems.set(slot, tome);

				break;
			}
		}
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public int getChargeNeeded() {
		return chargeNeeded;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<AlkahestryCraftingRecipe> {
		private final Codec<AlkahestryCraftingRecipe> codec = RecordCodecBuilder.create(
				instance -> instance.group(
								Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.craftingIngredient),
								Codec.INT.fieldOf("charge").forGetter(recipe -> recipe.chargeNeeded),
								Codec.INT.fieldOf("result_count").forGetter(recipe -> recipe.resultCount)
						)
						.apply(instance, AlkahestryCraftingRecipe::new));

		@Override
		public Codec<AlkahestryCraftingRecipe> codec() {
			return codec;
		}

		@Override
		public AlkahestryCraftingRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new AlkahestryCraftingRecipe(Ingredient.fromNetwork(buffer), buffer.readInt(), buffer.readInt());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlkahestryCraftingRecipe recipe) {
			recipe.craftingIngredient.toNetwork(buffer);
			buffer.writeInt(recipe.chargeNeeded);
			buffer.writeInt(recipe.resultCount);
		}
	}
}
