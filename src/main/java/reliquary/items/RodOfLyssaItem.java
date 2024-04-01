package reliquary.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import reliquary.entities.LyssaHook;
import reliquary.util.NBTHelper;

public class RodOfLyssaItem extends ItemBase {
	public RodOfLyssaItem() {
		super(new Properties().stacksTo(1));
	}

	public static int getHookEntityId(ItemStack stack) {
		return NBTHelper.getInt("hookEntityId", stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		int entityId = getHookEntityId(stack);
		if (entityId != 0 && level.getEntity(entityId) instanceof LyssaHook hook) {
			player.swing(hand);
			hook.handleHookRetraction(stack);
			setHookEntityId(stack, 0);
		} else {
			level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

			if (!level.isClientSide) {

				int lureLevel = stack.getEnchantmentLevel(Enchantments.FISHING_SPEED);
				int luckOfTheSeaLevel = stack.getEnchantmentLevel(Enchantments.FISHING_LUCK);

				LyssaHook hook = new LyssaHook(level, player, lureLevel, luckOfTheSeaLevel);
				level.addFreshEntity(hook);

				setHookEntityId(stack, hook.getId());
			}

			player.swing(hand);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	private void setHookEntityId(ItemStack stack, int entityId) {
		NBTHelper.putInt("hookEntityId", stack, entityId);
	}
}
