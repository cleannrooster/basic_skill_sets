package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.api.CombatTrackerAccess;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
@Shadow
private int foodTickTimer;
    @Shadow private float saturationLevel;
    @Inject(method = "update", at = @At("HEAD"))
    private void resetTimer(PlayerEntity player, CallbackInfo ci) {
        if (((CombatTrackerAccess)player).isInCombatBasicSkills()
                && player.canFoodHeal()
                && saturationLevel > 0.0F) {

            this.foodTickTimer = 0;
        }
    }
}