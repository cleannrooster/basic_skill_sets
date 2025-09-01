package com.cleannrooster.basic_skill_sets.mixin;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.spell_engine.api.item.trinket.ISpellBookItem;
import net.spell_engine.api.item.trinket.SpellBookItem;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.Ammo;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.utils.WorldScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellHelper.class)
public class SpellCastMixin {
    @Inject(at = @At("HEAD"), method = "attemptCasting", cancellable = true)
    private static void attemptCastingCleann(PlayerEntity player, ItemStack itemStack, Identifier spellId, CallbackInfoReturnable<SpellCast.Attempt> cir) {
        Spell spell = SpellRegistry.from(player.getWorld()).get(spellId);
        if (!((SpellCasterEntity)player).getCooldownManager().isCoolingDown(spellId) && SpellRegistry.from(player.getWorld()).get(spellId).type.equals(Spell.Type.ACTIVE) && player.getAttackCooldownProgress(0F) < 0.5F) {
            cir.setReturnValue(SpellCast.Attempt.none());
            return;
        }
        if(SpellRegistry.from(player.getWorld()).get(spellId) != null && SpellRegistry.from(player.getWorld()).get(spellId).type.equals(Spell.Type.ACTIVE) &&  SpellContainerSource.getFirstSourceOfSpell(spellId,player) != null && !SpellContainerSource.getFirstSourceOfSpell(spellId,player).itemStack().equals(player.getMainHandStack())
        && !SpellContainerHelper.contains(SpellContainerHelper.containerFromItemStack(itemStack),spellId)){
            var bool = (itemStack).getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers().stream().anyMatch(entry ->entry.attribute().equals(spell.school.attributeEntry));
            for(int i = 0; i<9; ++i){
                if(spell != null && SpellContainerSource.getFirstSourceOfSpell(spellId,player) != null && !(SpellContainerSource.getFirstSourceOfSpell(spellId,player).itemStack().getItem() instanceof ISpellBookItem)  &&  player.getInventory().getStack(i).equals(SpellContainerSource.getFirstSourceOfSpell(spellId,player).itemStack())){
                    player.getInventory().selectedSlot = i;
                    if(player.getWorld() instanceof ServerWorld serverWorld){
                        ((WorldScheduler)serverWorld).schedule(1,()->{
                            ((SpellCasterEntity)player).getCooldownManager().set(spellId, (int) (20F/player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)));

                        });
                    }
                    cir.setReturnValue(SpellCast.Attempt.failOnCooldown(new SpellCast.Attempt.OnCooldownInfo()));

                    return;
                }

            }
            for(int i = 0; i<9; ++i) {
                var stack = player.getInventory().getStack(i);

                if(!bool && (stack).getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers().stream().anyMatch(entry ->entry.attribute().equals(spell.school.attributeEntry))){
                    player.getInventory().selectedSlot = i;

                            ((SpellCasterEntity)player).getCooldownManager().set(spellId, (int) (20F/player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)));


                    cir.setReturnValue(SpellCast.Attempt.failOnCooldown(new SpellCast.Attempt.OnCooldownInfo()));

                    return;

                }
            }

        }

    }
}
