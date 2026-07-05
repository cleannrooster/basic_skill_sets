package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.networking.Packet;
import com.cleannrooster.basic_skill_sets.particle.SlashParticleHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.spell_engine.api.event.CombatEvents;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellEvents;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;

import java.util.List;

public class ServerEventHandlers {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> BasicSkillSets.clientConfig = BasicSkillSets.config);

        SpellEvents.PROJECTILE_SHOOT.register((spell) -> {
            if (!BasicSkillSets.config.projectileSelfKnockback) return;
            if (spell.spellEntry().value().impacts.stream().noneMatch(i -> i.action.type == Spell.Impact.Action.Type.DAMAGE)) return;
            if (!(spell.caster() instanceof HitstopAccessor hitstopAccessor)) return;
            if (!spell.spellEntry().value().deliver.type.equals(Spell.Delivery.Type.PROJECTILE)) return;

            double speed = 0;
            for (Spell.Impact impact : spell.spellEntry().value().impacts) {
                if (impact.action.type == Spell.Impact.Action.Type.DAMAGE) {
                    speed += 0.5 * impact.action.damage.spell_power_coefficient
                            * Math.pow(SpellPower.getSpellPower(
                                    impact.school != null ? impact.school : spell.spellEntry().value().school,
                                    spell.caster()).baseValue(), 0.73118)
                            / 10.0;
                }
            }
            hitstopAccessor.setImpulseVector(
                hitstopAccessor.getImpulseVector().add(
                    spell.caster().getRotationVector().normalize().multiply(speed, speed, speed).multiply(-1))
            );
            spell.caster().velocityDirty = true;
            spell.caster().velocityModified = true;
        });

        CombatEvents.ENTITY_SHIELD_BLOCK.register((CombatEvents.EntityShieldBlock) (c) -> {
            if (BasicSkillSets.config.shieldFlash && c.entity() instanceof ServerPlayerEntity serverPlayer) {
                Packet.ShieldFlash flashPacket = new Packet.ShieldFlash(serverPlayer.getId());
                for (ServerPlayerEntity viewer : serverPlayer.getServerWorld().getPlayers()) {
                    ServerPlayNetworking.send(viewer, flashPacket);
                }
            }

            if (!(c.entity() instanceof PlayerEntity player)) return;
            if (c.source().getAttacker() == null) return;
            if (c.source().getAttacker().distanceTo(c.entity()) >= player.getEntityInteractionRange()) return;
            if (!(c.source().getAttacker() instanceof LivingEntity)) return;


            ((WorldScheduler) c.entity().getWorld()).schedule(10, () -> {
                if (!c.entity().isBlocking() && c.source().getAttacker() != null) {
                    SpellHelper.performSpell(
                        c.entity().getWorld(), player,
                        (RegistryEntry<Spell>) SpellRegistry.from(player.getWorld())
                            .getEntry(Identifier.of("basic-skill-sets", "shield_bash")).get(),
                        new SpellTarget.SearchResult(List.of(c.source().getAttacker()), c.source().getAttacker().getPos()),
                        SpellCast.Action.TRIGGER, 1.0F
                    );
                }
            });
        });

        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {
            for (PlayerEntity player : world.getPlayers()) {
                if (player.age % 100 == 0 && player instanceof ServerPlayerEntity playerEntity
                        && player instanceof HitstopAccessor accessor) {
                    ServerPlayNetworking.send(playerEntity, new Packet.HolsterAssert(accessor.isHolster()));
                }
                if (player instanceof SpellCasterEntity entity
                        && entity.getSpellCastProcess() != null
                        && entity.getCurrentSpell() != null
                        && entity.isCastingSpell()
                        && isWhirlwindSpell(entity, world)) {
                    RegistryEntry<Spell> spell = entity.getSpellCastProcess().spell();
                    SlashParticleHandler.spawnParticlesSlash(player, world,
                        player.age % 10 * 36,
                        270 + player.getRandom().nextBetween(-15, 15),
                        1.0F,
                        (float) SpellHelper.getRange(player, spell));
                }
            }
        });
    }

    private static boolean isWhirlwindSpell(SpellCasterEntity entity, ServerWorld world) {
        var current = entity.getCurrentSpell();
        return current.equals(SpellRegistry.from(world).get(Identifier.of("rogues", "whirlwind")))
            || current.equals(SpellRegistry.from(world).get(Identifier.of("spellbladenext", "eviscerate")))
            || current.equals(SpellRegistry.from(world).get(Identifier.of("spellbladenext", "whirling_assault")));
    }
}
