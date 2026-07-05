package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.client.ShieldFlashState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {

    @Inject(
            method = "packUv(II)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void modifyOverlay(int u, int v, CallbackInfoReturnable<Integer> cir) {
        if (!ShieldFlashState.renderingFlashingShield) {
            return;
        }

    }
}