package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.spell_engine.api.effect.EntityActionsAllowed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.entity.player.PlayerInventory.isValidHotbarIndex;

@Mixin(PlayerInventory.class)
public class PlayerMixin  {
    @Shadow
    public PlayerEntity player;
    @Inject(at = @At("RETURN"), method = "getMainHandStack", cancellable = true)
    public void getMainHandStack(CallbackInfoReturnable<ItemStack> returnable) {
        if(player instanceof HitstopAccessor hit &&hit.isHolster()){
                returnable.setReturnValue(ItemStack.EMPTY);

        }
    }



}
