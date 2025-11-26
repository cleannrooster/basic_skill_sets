package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.api.SkillSources;
import com.cleannrooster.basic_skill_sets.config.ConfigSync;
import com.cleannrooster.basic_skill_sets.config.ServerConfig;
import com.cleannrooster.basic_skill_sets.networking.Packet;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.mixin.item.client.HeldItemRendererMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.casting.SpellCasterEntity;

public class BasicSkillSetsClient implements ClientModInitializer {
    public static ServerConfig clientConfig;

    @Override
    public void onInitializeClient() {
        BetterCombatClientEvents.ATTACK_START.register((BetterCombatClientEvents.PlayerAttackStart)(player, attackHand) -> {
            if (clientConfig != null && clientConfig.moveAttack) {
                double speed = player.getMovementSpeed() * (double) 4.0F;
                Vec3d base =  player.getRotationVector().normalize().multiply(2*player.getMovementSpeed(),1,2*player.getMovementSpeed()).length() > player.getRotationVector().normalize().multiply(player.getVelocity().length()).length() ? player.getRotationVector().normalize().multiply(player.getVelocity().length()): player.getRotationVector().normalize().multiply(2*player.getMovementSpeed(),1,2*player.getMovementSpeed());

                Vec3d vec = base.multiply(4F,0,4F).multiply(1.6F/player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED),0,1.6F/player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
                Vec3d vecCross = vec.crossProduct(new Vec3d(0
                        ,(float)(!attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.HORIZONTAL_PLANE) ? 0 : (!attackHand.isOffHand() && (!attackHand.attack().animation().contains("left") || attackHand.attack().animation().contains("right")) ? -1 : 1))
                        ,0)).add(0,attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.VERTICAL_PLANE) ? -1 : 0, 0);
                var vecMoveEnemy = vec.add(vecCross.multiply(0.25));
                ClientPlayNetworking.send(new Packet.Impulse(player.getId(),0.8F, (float) vecMoveEnemy.getX(), (float) vecMoveEnemy.getY(), (float) vecMoveEnemy.getZ()));



            }

            if (clientConfig != null && clientConfig.particles && attackHand.attack() != null) {
                ClientPlayNetworking.send(new Packet.Packets((float)(!attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.HORIZONTAL_PLANE) ? 0 : (!attackHand.isOffHand() && (!attackHand.attack().animation().contains("left") || attackHand.attack().animation().contains("right")) ? 180 - (60 + player.getRandom().nextBetween(0, 60)) : 240 + player.getRandom().nextBetween(0, 60))), attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.FORWARD_BOX) ? 0.25F : 1.0F, (float)(attackHand.attributes().attackRange() == (double)0.0F ? player.getEntityInteractionRange() : attackHand.attributes().attackRange() + attackHand.attributes().rangeBonus())));
            }

        });
        PayloadTypeRegistry.playC2S().register(ConfigSync.PACKET_ID, ConfigSync.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ConfigSync.PACKET_ID, (payload, context) -> clientConfig = payload.config());

        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            BasicSkillSets.sources =  new SkillSources(BasicSkillSets.skills);

        });
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, lines)->{
            if(itemStack.getItem() instanceof ShieldItem shieldItem){
                lines.add(Text.translatable("text.basic-skill-sets.shield").formatted(Formatting.GRAY));
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.player != null && ((SpellCasterEntity)client.player).getCurrentSpell() != null &&
                    (((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID
            ,"charge"))) || ((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID
                            ,"shield_charge"))))){
                if(((SpellCasterEntity)client.player).getSpellCastProcess().spellCastTicksSoFar(client.world.getTime()) > 15){
                    double speed = client.player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)  * 4;

                    client.player.setVelocity(client.player.getRotationVec(1).subtract(0, client.player.getRotationVec(1).y, 0).normalize().multiply(speed, 0 , speed).add(0, client.player.getVelocity().y, 0));
                    client.player.velocityDirty = true;
                }
            }
        });
        ItemTooltipCallback.EVENT.register((ItemTooltipCallback)(itemStack, tooltipContext, tooltipType, lines) -> {
            Item patt0$temp = itemStack.getItem();
            if (patt0$temp instanceof ShieldItem shieldItem) {
                lines.add(Text.translatable("text.basic-skill-sets.shield").formatted(Formatting.GRAY));
            }

        });
        ClientTickEvents.START_CLIENT_TICK.register((ClientTickEvents.StartTick)(client) -> {
            if (client.player != null && ((SpellCasterEntity)client.player).getCurrentSpell() != null && (((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).getEntry(Identifier.of("basic-skill-sets", "charge"))) || ((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).getEntry(Identifier.of("basic-skill-sets", "shield_charge")))) && ((SpellCasterEntity)client.player).getSpellCastProcess().spellCastTicksSoFar(client.world.getTime()) > 15) {
                double speed = client.player.getMovementSpeed()*(double)4.0F;
                client.player.addVelocity(client.player.getRotationVec(1.0F).subtract((double)0.0F, client.player.getRotationVec(1.0F).getY(), (double)0.0F).normalize().multiply(speed, (double)0.0F, speed).add((double)0.0F, client.player.getVelocity().getY(), (double)0.0F));
                client.player.velocityDirty = true;
            }

        });

        BetterCombatClientEvents.ATTACK_HIT.register((clientPlayerEntity, attackHand, list, entity) ->{
            Vec3d vecMove = clientPlayerEntity.getRotationVec(1.0F).crossProduct(new Vec3d(0
                    ,(float)(!attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.HORIZONTAL_PLANE) ? 0 : (!attackHand.isOffHand() && (!attackHand.attack().animation().contains("left") || attackHand.attack().animation().contains("right")) ? 1 : -1))
                    ,0)).add(0,attackHand.attack().hitbox().equals(WeaponAttributes.HitBoxShape.VERTICAL_PLANE) ? 1 : 0, 0).normalize().multiply(0.2*Math.max(0.5,1.6F/clientPlayerEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)));
            if(clientConfig.comboSpeed)
            ClientPlayNetworking.send(new Packet.SlowFirst());

                    if(clientConfig.impactRecoil && !list.isEmpty()) {
                        var vecMoveEnemy = (vecMove).add(((HitstopAccessor) clientPlayerEntity).getVelocityHitstop() != null ? ((HitstopAccessor) clientPlayerEntity).getVelocityHitstop() : Vec3d.ZERO);
                        ClientPlayNetworking.send(new Packet.Impulse(clientPlayerEntity.getId(),0.25F, (float) vecMoveEnemy.getX(), (float) vecMoveEnemy.getY(), (float) vecMoveEnemy.getZ()));
                        clientPlayerEntity.velocityDirty = true;
                    }

                    for(Entity entityMove : list){
                Vec3d vecMoveEnemy = vecMove.multiply(-1F);
                if(entityMove instanceof LivingEntity livingEntity){
                    vecMoveEnemy = vecMoveEnemy.multiply(1F-livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                }
                        if(clientConfig.impactEnemy)
                ClientPlayNetworking.send(new Packet.Impulse(entityMove.getId(),0.15F, (float) vecMoveEnemy.getX(), (float) vecMoveEnemy.getY(), (float) vecMoveEnemy.getZ()));

                LivingEntity living = (LivingEntity) (Object) clientPlayerEntity;
                if(!list.isEmpty() && clientConfig.hitstopSelf) {
                    if (living instanceof HitstopAccessor hitstopAccessor) {

                        hitstopAccessor.setHitstop((int) Math.ceil(2 * (1.6F / living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED))));
                        if (hitstopAccessor.getVelocityHitstop() == null) {
                            hitstopAccessor.setVelocityHitstop(living.getVelocity());
                            living.setVelocity(Vec3d.ZERO);
                            living.velocityDirty = true;
                        }
                    }
                }
            }

        }

        );
    }
}
