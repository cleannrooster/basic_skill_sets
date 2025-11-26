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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ClientPlayerMixin implements HitstopAccessor {
    public int hitstopTicks = 0;
    private Vec3d velocityHitstop;
    private long lastAttackedTemporary = 0;

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
        if (this.getHitstopTicks() > 0) {
            living.limbAnimator.setSpeed(0);
            living.setVelocity(0, 0, 0);
            living.velocityDirty = true;

            hitstopTicks--;
        }
        else
        if(velocityHitstop != null){
            living.setVelocity(getVelocityHitstop());
            setVelocityHitstop(null);
            living.velocityDirty = true;

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
    public Vec3d impulseVector;

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

            hitstopAccessor.setHitstop((int) Math.ceil(2*(1.6F/living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED))));
            if (hitstopAccessor.getVelocityHitstop() == null) {
                hitstopAccessor.setVelocityHitstop(livingEntity.getVelocity());
                livingEntity.setVelocity(Vec3d.ZERO);
                livingEntity.velocityDirty = true;
            }
        }
    }
}
