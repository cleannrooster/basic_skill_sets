package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellSchool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(SpellHelper.class)
public class SpellHelperMixin {
    @Inject(at = @At("RETURN"), method = "performImpact", cancellable = true)
    private static void performImpactBasicSkills(World world, LivingEntity caster, Entity target, RegistryEntry<Spell> spellEntry, Spell.Impact impact, SpellHelper.ImpactContext context, Collection<ServerPlayerEntity> trackers,
        CallbackInfoReturnable<Boolean> cir) {
        if (  cir.getReturnValue() && impact.action.type == Spell.Impact.Action.Type.DAMAGE && impact != null && spellEntry.value().school != null   && target instanceof HitstopAccessor hitstopAccessor &&  spellEntry.value().school.archetype.equals(SpellSchool.Archetype.MAGIC)) {
            if(BasicSkillSets.config.hitstopEnemies) {
                if (world.getTime() - hitstopAccessor.getLastHitstopAppliedTime() >= 10) {
                    hitstopAccessor.setHitstop(Math.max(hitstopAccessor.getHitstopTicks(), (int) (impact.action.damage.spell_power_coefficient * 2)));
                    hitstopAccessor.setLastHitstopAppliedTime(world.getTime());
                    //living.limbAnimator.setSpeed(0);
                    if (hitstopAccessor.getVelocityHitstop() == null) {
                        hitstopAccessor.setVelocityHitstop(target.getVelocity());
                        target.setVelocity(Vec3d.ZERO);
                    }
                }
            }
            if(BasicSkillSets.config.projectileSelfKnockback){
                if(context.position() != null && context.power() != null) {
                    var resistCoeff = 1F;
                    if(target instanceof LivingEntity living){
                        resistCoeff *= (float) (1F-living.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                        if(living.hasNoGravity()){
                            resistCoeff *= 0;
                        }
                    }
                    hitstopAccessor.setImpulseVector(hitstopAccessor.getImpulseVector().add( target.getBoundingBox().getCenter().subtract(context.position()).normalize().multiply(0.05F).multiply(0.125 * Math.pow(context.total() * context.power().randomValue(), 0.73118F)).multiply(resistCoeff)));
                }
            }
        }
    }
    @WrapOperation( method = "performImpact",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"))
    private static void performImpactHeal(LivingEntity livingEntity, float heal, Operation<Void> original) {
        boolean should =
                (BasicSkillSets.config.healSpread > 0F) &&
                (heal > 0F)                             &&
                (livingEntity.getWorld() instanceof ServerWorld);

        if(!should){
            original.call(livingEntity, heal);
            return;
        }

        var r = 0.75F;
        var i = Math.max(1, Math.round(2F * BasicSkillSets.config.healSpread));
        var A = heal * (1F - r) / (1F - Math.pow(r, i));

        ServerWorld serverWorld = (ServerWorld) livingEntity.getWorld();
        WorldScheduler scheduler = (WorldScheduler) serverWorld;

        for (int j = 0; j < i; ++j) {
            var ii = j;

            scheduler.schedule(1 + j * 10, () -> {
                if (!livingEntity.isAlive()) return;
                if (livingEntity.isRemoved()) return;
                livingEntity.heal((float) (A * Math.pow(r, ii)));
            });
        }
    }
}
