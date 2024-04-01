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

public class AlkahestryDrainRecipe implements CraftingRecipe {
	private final int chargeToDrain;
	private final ItemStack result;
	private final Ingredient tomeIngredient;

	public AlkahestryDrainRecipe(int chargeToDrain, ItemStack result) {
		this.chargeToDrain = chargeToDrain;
		this.result = result;
		tomeIngredient = Ingredient.of(AlkahestryTomeItem.setCharge(new ItemStack(ModItems.ALKAHESTRY_TOME.get()), AlkahestryTomeItem.getChargeLimit()));
		AlkahestryRecipeRegistry.setDrainRecipe(this);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		boolean hasTome = false;
		ItemStack tome = ItemStack.EMPTY;
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!hasTome && stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				hasTome = true;
				tome = stack;
			} else {
				return false;
			}
		}

		return hasTome && AlkahestryTomeItem.getCharge(tome) > 0;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, tomeIngredient);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack tome = getTome(inv).copy();

		int charge = AlkahestryTomeItem.getCharge(tome);
		ItemStack ret = result.copy();
		ret.setCount(Math.min(ret.getMaxStackSize(), charge / chargeToDrain));

		return ret;
	}

	private ItemStack getTome(CraftingContainer inv) {
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		NonNullList<ItemStack> ret = CraftingRecipe.super.getRemainingItems(inv);
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				ItemStack tome = stack.copy();
				int charge = AlkahestryTomeItem.getCharge(tome);
				int itemCount = Math.min(result.getMaxStackSize(), charge / chargeToDrain);
				AlkahestryTomeItem.useCharge(tome, itemCount * chargeToDrain);
				ret.set(slot, tome);
			}
		}

		return ret;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.ALKAHESTRY_DRAIN_SERIALIZER.get();
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<AlkahestryDrainRecipe> {
		private final Codec<AlkahestryDrainRecipe> codec = RecordCodecBuilder.create(
				instance -> instance.group(
								Codec.INT.fieldOf("charge").forGetter(recipe -> recipe.chargeToDrain),
								ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
						)
						.apply(instance, AlkahestryDrainRecipe::new));

		@Override
		public Codec<AlkahestryDrainRecipe> codec() {
			return codec;
		}

		@Override
		public AlkahestryDrainRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new AlkahestryDrainRecipe(buffer.readInt(), buffer.readItem());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlkahestryDrainRecipe recipe) {
			buffer.writeInt(recipe.chargeToDrain);
			buffer.writeItem(recipe.result);
		}
	}
}
