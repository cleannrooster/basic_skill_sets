package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSetsClient;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.spell_engine.api.item.weapon.SpellSwordItem;
import net.spell_engine.api.item.weapon.SpellWeaponItem;
import net.spell_engine.api.item.weapon.StaffItem;
import net.spell_engine.api.item.weapon.Weapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(at = @At("HEAD"), method = "renderItem")
    public void renderItemSize(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci ) {
        var instance = entity.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if(entity instanceof PlayerEntity player && BasicSkillSetsClient.clientConfig.rangeSize &&  (stack.getItem() instanceof StaffItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) && instance != null){
            var scaled = player.getEntityInteractionRange();
            var base = instance.getBaseValue() ;
            var weaponAdd = WeaponRegistry.getAttributes(stack).rangeBonus();
            var totalMult = (float)(scaled/(base+weaponAdd));
            matrices.scale(totalMult,totalMult,totalMult);

        }
    }
}
