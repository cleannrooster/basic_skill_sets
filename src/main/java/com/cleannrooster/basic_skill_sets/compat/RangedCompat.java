package com.cleannrooster.basic_skill_sets.compat;

import com.cleannrooster.basic_skill_sets.BasicSkillSetsClient;
import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_power.api.SpellPower;

public class RangedCompat {
    private static RegistryEntry<EntityAttribute> rangedDamageAttribute() {
        return FabricLoader.getInstance().isModLoaded("ranged_weapon_api") ? EntityAttributes_RangedWeapon.DAMAGE.entry : EntityAttributes.GENERIC_ATTACK_DAMAGE;
    }
    public static void doScale(MatrixStack matrices, LivingEntity entity, ItemStack stack) {
        var instance = entity.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);

        if(FabricLoader.getInstance().isModLoaded("ranged_weapon_api") && entity instanceof PlayerEntity player && BasicSkillSetsClient.clientConfig.rangeSize &&  (stack.getItem() instanceof RangedWeaponItem crossbowItem) && instance != null) {
            var scaled = SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_RANGED,entity).baseValue();
            var base = 0;
            var att = player.getAttributeInstance(rangedDamageAttribute());
            for(EntityAttributeModifier modifier : att.getModifiers()){
                if(modifier.operation().equals(EntityAttributeModifier.Operation.ADD_VALUE) && modifier.idMatches(AttributeModifierIDs.WEAPON_DAMAGE_ID)){
                    base += modifier.value();
                }
            }

            var totalMult = Math.min((float)Math.pow((scaled/(base)),0.4F),1.75F);
            matrices.scale((float) totalMult,totalMult,totalMult);
        }
    }
}
