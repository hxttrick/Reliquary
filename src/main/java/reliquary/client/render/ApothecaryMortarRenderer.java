package reliquary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import reliquary.blocks.ApothecaryMortarBlock;
import reliquary.blocks.tile.ApothecaryMortarBlockEntity;

public class ApothecaryMortarRenderer implements BlockEntityRenderer<ApothecaryMortarBlockEntity> {
	@Override
	public void render(ApothecaryMortarBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Direction direction = tile.getBlockState().getValue(ApothecaryMortarBlock.FACING);
		float horizontalRotation = direction == Direction.UP ? 0F : direction.get2DDataValue() * 90F;

		NonNullList<ItemStack> mortarItems = tile.getItemStacks();

		matrixStack.pushPose();
		matrixStack.translate(0.5D, 0.3D, 0.5D);
		matrixStack.mulPose(Axis.YN.rotationDegrees(horizontalRotation));

		renderMortarItem(matrixStack, buffer, packedLight, () -> {
			matrixStack.mulPose(Axis.ZP.rotationDegrees(40F));
			matrixStack.mulPose(Axis.YP.rotationDegrees(90F));
		}, mortarItems.get(0), -0.09F, 0F, packedOverlay);

		renderMortarItem(matrixStack, buffer, packedLight, () -> {
			matrixStack.mulPose(Axis.XP.rotationDegrees(40F));
			matrixStack.mulPose(Axis.YP.rotationDegrees(180F));
		}, mortarItems.get(1), 0F, 0.09F, packedOverlay);

		renderMortarItem(matrixStack, buffer, packedLight, () -> {
			matrixStack.mulPose(Axis.ZN.rotationDegrees(40F));
			matrixStack.mulPose(Axis.YP.rotationDegrees(270F));
		}, mortarItems.get(2), 0.09F, 0F, packedOverlay);

		matrixStack.popPose();
	}

	private void renderMortarItem(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Runnable processRotationTransforms, ItemStack itemToRender, float translateX, float translateZ, int packedOverlay) {
		if (!itemToRender.isEmpty()) {
			matrixStack.pushPose();
			matrixStack.translate(translateX, 0F, translateZ);
			processRotationTransforms.run();
			matrixStack.scale(0.60F, 0.60F, 0.60F);
			Minecraft.getInstance().getItemRenderer().renderStatic(itemToRender, ItemDisplayContext.GROUND, packedLight, packedOverlay, matrixStack, buffer, null, 0);
			matrixStack.popPose();
		}
	}
}
