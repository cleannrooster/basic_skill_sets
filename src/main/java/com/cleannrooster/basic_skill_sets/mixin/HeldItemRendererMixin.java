package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSetsClient;
import com.cleannrooster.basic_skill_sets.client.ShieldFlashState;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.bettercombat.logic.WeaponRegistry;
import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ShieldItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.config.AttributeModifier;
import net.spell_engine.api.item.weapon.SpellSwordItem;
import net.spell_engine.api.item.weapon.SpellWeaponItem;
import net.spell_engine.api.item.weapon.StaffItem;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_power.api.SpellPower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.cleannrooster.basic_skill_sets.compat.RangedCompat.doScale;
import static net.minecraft.client.render.item.ItemRenderer.getItemGlintConsumer;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    @Final
    private  ItemRenderer itemRenderer;

    @Inject(at = @At("HEAD"), method = "renderItem")
    public void renderItemSize(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci ) {
        var instance = entity.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if(entity instanceof PlayerEntity player && BasicSkillSetsClient.clientConfig.rangeSize &&  (stack.getItem() instanceof StaffItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) && instance != null){
            var scaled = player.getEntityInteractionRange();
            var base = instance.getBaseValue() ;
            var weaponAdd =  0D;
            if(WeaponRegistry.getAttributes(stack) != null){
                weaponAdd += WeaponRegistry.getAttributes(stack).rangeBonus();
            }
            var totalMult = (float)Math.pow((float)(scaled/(base+weaponAdd)),0.73118F);
            matrices.scale(totalMult,totalMult,totalMult);

        }
        if(FabricLoader.getInstance().isModLoaded("ranged_weapon_api")){
            doScale(matrices,entity,stack);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderItem", cancellable = true)
    public void shieldFlashPre(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode,
                              boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (stack.getItem() instanceof ShieldItem && ShieldFlashState.isFlashing(entity.getId())) {
            var factor = ShieldFlashState.getFlashFactor(entity.getId());
            int block = (int)(15 * factor);
            int sky = 15;
            RenderLayer renderLayer = RenderLayers.getItemLayer(stack, false);
            var vertexConsumer = getItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
            var l =  (sky << 20) | (block << 4);
            int u = Math.round(ShieldFlashState.getFlashFactor(entity.getId()) * 15);
            this.itemRenderer.renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.getWorld(), l, OverlayTexture.packUv(u,10), entity.getId() + renderMode.ordinal());
            ci.cancel();
        }
    }



}
