package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import net.bettercombat.mixin.player.PlayerEntityMixin;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.tags.SpellEngineDamageTypeTags;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellPowerTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements HitstopAccessor {
    protected int hitstopTicks = 0;
    protected Vec3d velocityHitstop = Vec3d.ZERO;
    protected int hitstopTime = 0;
    protected boolean holster = false;


    @Inject(at = @At("RETURN"), method = "getOffHandStack", cancellable = true)
    public void  getMainHandStackHolster(CallbackInfoReturnable<ItemStack> returnable){
        if(this.holster){
                returnable.setReturnValue(ItemStack.EMPTY);

        }
    }
    @Inject(at = @At("HEAD"), method = "swapHandStacks", cancellable = true)

    private void swapHandStacksHolster(CallbackInfo callbackInfo) {
        if(this.holster){
            callbackInfo.cancel();

        }
    }
    @Inject(at = @At("RETURN"), method = "getMainHandStack", cancellable = true)
    public void  getOffHandStackHolster(CallbackInfoReturnable<ItemStack> returnable){
        if(this.holster){
                returnable.setReturnValue(ItemStack.EMPTY);

        }
    }


    private Vec3d impulseVector = Vec3d.ZERO;
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tickHitstopHead( CallbackInfo info) {
        LivingEntity living = (LivingEntity) (Object) this;


    }
    public void setHitstopTime(int hitstop) {
        this.hitstopTime = hitstop;
    }
    public int getHitstopTime(){
        return hitstopTime;
    };
    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    public void tickHitstop( CallbackInfo info) {
        LivingEntity living = (LivingEntity) (Object) this;
        Spell.Target.Area area = new Spell.Target.Area();
        area.angle_degrees = 150;
        List<Entity> list = TargetHelper.targetsFromArea(living,(living instanceof PlayerEntity player ? (float) player.getEntityInteractionRange() : 4 )*0.6F,area,null);
        List<Entity> list2 = TargetHelper.targetsFromArea(living,living.getWidth()+0.5F,area,null);

        boolean bool = !list.isEmpty();
        boolean trueBool = !list2.isEmpty() || bool;
        if(!living.getWorld().isClient() &&   getImpulseVector() != null && getImpulseVector().length() > 0.02) {
            var shouldSub = this.shouldClamp() && trueBool;
            if(!living.isOnGround()) {
                setImpulseVector(getImpulseVector().multiply(0.8F));
            }
            var toAdd = getImpulseVector().multiply(  1.0F);
            var totalVelocity = living.getVelocity().add(toAdd);
            var defaultMovementSpeed = 0.1F;
            var entityMoveSpeed = living.getMovementSpeed();
            var entityMoveSpeedCoeff = 0.5F;
            var coeff = 4F*BasicSkillSets.config.maxImpulse;
            var cap = (defaultMovementSpeed + entityMoveSpeed * entityMoveSpeedCoeff) *  coeff;
            var capSq = cap * cap;
            var finalVel =     (living.getVelocity().lengthSquared() > capSq ?
                    totalVelocity.normalize().multiply(living.getVelocity().length()) :
                    totalVelocity.lengthSquared() > capSq ?
                            totalVelocity.normalize().multiply(cap) :
                            totalVelocity);
            var sub = (shouldSub ? totalVelocity.normalize().multiply(cap*0.5) : Vec3d.ZERO);
            if(finalVel.lengthSquared() < sub.lengthSquared()){
                finalVel = Vec3d.ZERO;
            }
            else{
                finalVel = finalVel.subtract(sub);
            }
          /*  if(finalVel.lengthSquared() <= living.getVelocity().lengthSquared() && shouldSub){
                finalVel = living.getVelocity();
            }*/
            living.setVelocity(
                    finalVel
            );
            setImpulseVector(  getImpulseVector().multiply(BasicSkillSets.config.impulseCoeff));
            living.velocityModified = true;
        }
        if(getImpulseVector() == null || getImpulseVector().length() < 0.02){
            this.setShouldClamp(false);
            this.setImpulseVector(Vec3d.ZERO);
        }
        if (this.getHitstopTicks() > 0) {
            if(getVelocityHitstop() != null) {
                setVelocityHitstop(getVelocityHitstop().add(living.getVelocity()) );
            }
            living.setVelocity(0, 0, 0);
            living.velocityDirty = true;
            living.velocityModified = true;

            living.limbAnimator.updateLimbs(2,0.25F);

            setHitstop(getHitstopTicks()-1);
        }
        else
        if(getVelocityHitstop() != null){
            living.setVelocity(getVelocityHitstop());
            setVelocityHitstop(null);
            living.velocityDirty = true;
            living.velocityModified = true;

        }
    }
    /*@Inject(at = @At("HEAD"), method = "travel", cancellable = true)

    public void travelHitstop(Vec3d movementInput, CallbackInfo info ) {
        if(getHitstopTicks() > 0){
            info.cancel();
        }
    }*/

    @Inject(at = @At("RETURN"), method = "damage", cancellable = true)
    protected void applyDamageHitstop(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        LivingEntity living = (LivingEntity) (Object) this;

        if(BasicSkillSets.config.hitstopEnemies) {
            if(source.isIn(SpellPowerTags.DamageTypes.ALL)){
                if(source.getSource() instanceof SpellProjectile projectile ){

                }
            }
            else
            if (source.isDirect() && source.getAttacker() instanceof HitstopAccessor hitstopAccessor && source.getAttacker() instanceof PlayerEntity hurt
                    && hurt.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED) != null) {
                this.setHitstop(Math.max(this.getHitstopTicks(), (int) Math.ceil(2 * (1.6F / hurt.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)))));
                //living.limbAnimator.setSpeed(0);

                if (this.getVelocityHitstop() == null) {
                    this.setVelocityHitstop(living.getVelocity());
                    living.setVelocity(Vec3d.ZERO);
                }

            }
        }

    }

    @Override
    public int getHitstopTicks() {
        return hitstopTicks;
    }

    @Inject(at = @At("HEAD"), method = "tickMovement", cancellable = true)
    public void tickMovementHistop(CallbackInfo info ) {
        LivingEntity living = (LivingEntity) (Object) this;

        if(living instanceof HitstopAccessor hitstopAccessor) {
            if(hitstopAccessor.getHitstopTicks() > 0) {
                info.cancel();
            }
        }
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
    public void setImpulseVector(Vec3d vec3d) {
        this.impulseVector = vec3d;
    }

    @Override
    public Vec3d getImpulseVector() {
        return impulseVector;
    }

    @Override
    public Vec3d getVelocityHitstop() {
        return velocityHitstop;
    }
    private long lastAttackedTemporary = 0;

    @Override
    public void setLastAttackedTemporary(long time) {
        lastAttackedTemporary = time;
    }

    @Override
    public boolean isHolster() {
        return holster;
    }
    protected boolean shouldClamp = false;
    @Override
    public boolean shouldClamp() {
        return     shouldClamp;

    }

    @Override
    public void setShouldClamp(boolean shouldClamp) {
        this.shouldClamp = shouldClamp;
    }

    @Override
    public void setHolster(boolean holster) {
            this.holster = holster;
    }

    @Inject(at = @At("HEAD"), method = "updateLimbs", cancellable = true)

    public void updateLimbsHitstop(boolean flutter, CallbackInfo info) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if(getHitstopTicks() > 0){
            info.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "updatePostDeath", cancellable = true)
    protected void updatePostDeathHitstop(CallbackInfo info) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if(getHitstopTicks() > 0){
            info.cancel();
        }

    }

}
