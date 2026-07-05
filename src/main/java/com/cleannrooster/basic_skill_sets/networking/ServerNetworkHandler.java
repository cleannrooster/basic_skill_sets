package com.cleannrooster.basic_skill_sets.networking;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.config.ConfigSync;
import com.cleannrooster.basic_skill_sets.particle.SlashParticleHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.utils.TargetHelper;

import java.util.List;

public class ServerNetworkHandler {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ConfigSync.PACKET_ID, ConfigSync.CODEC);
        PayloadTypeRegistry.playS2C().register(Packet.HolsterAssert.PACKET_ID, Packet.HolsterAssert.CODEC);
        PayloadTypeRegistry.playS2C().register(Packet.ShieldFlash.PACKET_ID, Packet.ShieldFlash.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.Packets.PACKET_ID, Packet.Packets.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.Impulse.PACKET_ID, Packet.Impulse.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.SlowFirst.PACKET_ID, Packet.SlowFirst.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.Holster.PACKET_ID, Packet.Holster.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Packet.Holster.PACKET_ID, (payload, context) -> {
            if (context.player() instanceof HitstopAccessor hitstopAccessor) {
                hitstopAccessor.setHolster(payload.bool() || !hitstopAccessor.isHolster());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Packet.SlowFirst.PACKET_ID, (payload, context) -> {
            if (context.player() instanceof HitstopAccessor hitstopAccessor) {
                hitstopAccessor.setLastAttackedTemporary(context.player().getWorld().getTime());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Packet.Impulse.PACKET_ID, (payload, context) -> {
            Entity entity = context.player().getWorld().getEntityById(payload.id());
            if (payload.shouldCheck()) {
                Spell.Target.Area area = new Spell.Target.Area();
                area.angle_degrees = 150;
                List<Entity> list = TargetHelper.targetsFromArea(context.player(), (float) context.player().getEntityInteractionRange() * 1.4F, area, null);
                if (context.player() instanceof HitstopAccessor accessor) {
                    accessor.setShouldClamp(!list.isEmpty());
                }
            }
            if (entity instanceof HitstopAccessor hitstopAccessor) {
                hitstopAccessor.setImpulseVector(hitstopAccessor.getImpulseVector().multiply(payload.mag2()).add(new net.minecraft.util.math.Vec3d(payload.x(), payload.y(), payload.z())).multiply(payload.mag()));
                entity.setVelocity(entity.getVelocity());
                entity.velocityModified = true;
                entity.velocityDirty = true;
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Packet.Packets.PACKET_ID, (payload, context) ->
            SlashParticleHandler.spawnParticlesSlash(context.player(), context.player().getServerWorld(), payload.yaw(), payload.pitch(), payload.range())
        );
    }
}
