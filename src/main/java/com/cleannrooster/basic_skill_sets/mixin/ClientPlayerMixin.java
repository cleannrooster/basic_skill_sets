package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.utils.TargetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public class ClientPlayerMixin implements HitstopAccessor {
    protected int hitstopTicks = 0;
    protected int hitstopTime = 0;

    protected Vec3d velocityHitstop;
    protected long lastAttackedTemporary = 0;
    protected boolean holster = false;
    protected boolean shouldClamp;
    public boolean isHolster() {
        return BasicSkillSets.config.holster &&  holster;
    }

    @Override
    public boolean shouldClamp() {
        return shouldClamp;
    }

    public void setShouldClamp(boolean shouldClamp) {
        this.shouldClamp = shouldClamp;
    }

    public void setHolster(boolean holster) {
        this.holster = holster;
    }

    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    public void tickHitstop( CallbackInfo info) {
        LivingEntity living = (LivingEntity) (Object) this;
        if(!living.getWorld().isClient()){
            if(lastAttackedTemporary == 0 || living.getWorld().getTime() - lastAttackedTemporary > 80){
                living.getAttributes().removeModifiers(BasicSkillSets.map);
            }
            else{
                living.getAttributes().addTemporaryModifiers(BasicSkillSets.map);

            }
        }
        Spell.Target.Area area = new Spell.Target.Area();
        area.angle_degrees = 150;
        List<Entity> list = TargetHelper.targetsFromArea(living,(living instanceof PlayerEntity player ? (float) player.getEntityInteractionRange() : 4 )*0.8F,area,null);
        List<Entity> list2 = TargetHelper.targetsFromArea(living,living.getWidth()+0.5F,area,null);

        boolean bool = !list.isEmpty();
        boolean trueBool = !list2.isEmpty() || bool;

        if (this.getHitstopTicks() > 0) {
            if(this.getHitstopTicks() > living.getWorld().getTime() - this.getHitstopTime()) {
                living.limbAnimator.setSpeed(0);
                living.setVelocity(0, 0, 0);
                living.velocityDirty = true;
            }
            else{
                this.setHitstop(0);
                if(velocityHitstop != null){
                    living.setVelocity(getVelocityHitstop());
                    setVelocityHitstop(null);
                    living.velocityDirty = true;

                }
            }

        }

    }


    @Override
    public int getHitstopTicks() {
        return hitstopTicks;
    }

    @Override
    public void setVelocityHitstop(Vec3d vec3d) {
        this.velocityHitstop = vec3d;;
    }

    @Override
    public void setHitstop(int hitstop) {
        this.hitstopTicks = hitstop;
    }
    @Override
    public void setHitstopTime(int hitstop) {
        this.hitstopTime = hitstop;
    }
    public int getHitstopTime(){
        return hitstopTime;
    };

    public Vec3d impulseVector = Vec3d.ZERO;

    @Override
    public void setImpulseVector(Vec3d vec3d) {
        impulseVector = vec3d;
    }

    @Override
    public Vec3d getImpulseVector() {
        return impulseVector;
    }

    @Override
    public void setLastAttackedTemporary(long time) {
        this.lastAttackedTemporary = time;
    }

    protected long lastHitstopAppliedTime = 0;

    @Override
    public long getLastHitstopAppliedTime() {
        return lastHitstopAppliedTime;
    }

    @Override
    public void setLastHitstopAppliedTime(long time) {
        this.lastHitstopAppliedTime = time;
    }

    @Override
    public Vec3d getVelocityHitstop() {
        return velocityHitstop;
    }
    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    public void onAttackingHitstop(Entity target, CallbackInfo info) {

    }
    @Inject(at = @At("TAIL"), method = "attack", cancellable = true)
    public void onAttackingHitstopTail(Entity target, CallbackInfo info) {
        LivingEntity living = (LivingEntity) (Object) this;
        if(BasicSkillSets.config.hitstopEnemies && target instanceof HitstopAccessor hitstopAccessor && target instanceof LivingEntity livingEntity ) {
            int hitstunCooldown = (int) Math.min(10, Math.ceil(20.0 / living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)));
            if (target.getWorld().getTime() - hitstopAccessor.getLastHitstopAppliedTime() >= hitstunCooldown) {
                hitstopAccessor.setHitstop((int) Math.ceil(2*(1.6F/living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED))));
                hitstopAccessor.setLastHitstopAppliedTime(target.getWorld().getTime());
                if (hitstopAccessor.getVelocityHitstop() == null) {
                    hitstopAccessor.setVelocityHitstop(livingEntity.getVelocity());
                    livingEntity.setVelocity(Vec3d.ZERO);
                    livingEntity.velocityDirty = true;
                }
            }
        }
    }
}
