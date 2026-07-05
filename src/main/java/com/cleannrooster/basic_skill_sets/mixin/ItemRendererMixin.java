package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.client.ShieldFlashState;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

/*

    @Inject(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD")
    )
    private void markShieldContext(
            ItemStack stack,
            ModelTransformationMode mode,
            boolean leftHanded,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light,
            int overlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        boolean isShield = stack.getItem() instanceof ShieldItem; // expand this
        ShieldFlashState.renderingFlashingShield = true;

        ShieldFlashState.push(isShield);
    }

    @Inject(method = "renderItem", at = @At("TAIL"))
    public void clearItemContext(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo callbackInfo) {
        ShieldFlashState.pop();
        ShieldFlashState.renderingFlashingShield = false;

    }
    @Inject(method = "renderBakedItemModel", at = @At("HEAD"))
    private void markShieldContext(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        boolean isShield = stack.getItem() instanceof ShieldItem; // expand this
        ShieldFlashState.renderingFlashingShield = true;

        ShieldFlashState.push(isShield);


    }
    @Inject(method = "renderBakedItemModel", at = @At("TAIL"))
    private void clearItemContext(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        ShieldFlashState.renderingFlashingShield = false;

        ShieldFlashState.pop();

    }*/
}