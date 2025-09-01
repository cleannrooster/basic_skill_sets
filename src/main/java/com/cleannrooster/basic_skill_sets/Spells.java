package com.cleannrooster.basic_skill_sets;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.spell_engine.api.item.SpellBooks;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.spellbinding.SpellBindingScreen;
import net.spell_engine.utils.AnimationHelper;
import net.spell_engine.utils.SoundHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.cleannrooster.basic_skill_sets.BasicSkillSets.MOD_ID;
import static net.spell_engine.api.spell.event.SpellHandlers.registerCustomDelivery;
import static net.spell_engine.internals.target.EntityRelations.actionAllowed;

public class Spells {
    public static class  Prepare implements SpellHandlers.CustomDelivery {
        public boolean onSpellDelivery(World world, RegistryEntry<Spell> registryEntry, PlayerEntity playerEntity, List<SpellHelper.DeliveryTarget> targets, SpellHelper.ImpactContext impactContext, @Nullable Vec3d vec3d) {
            if(!targets.isEmpty()){
                if(targets.stream().anyMatch((entity) -> {
                    return actionAllowed(SpellTarget.FocusMode.AREA, SpellTarget.Intent.HARMFUL,playerEntity,entity.entity());
                })) {
                    for (SpellHelper.DeliveryTarget target : targets) {
                        if (actionAllowed(SpellTarget.FocusMode.AREA, SpellTarget.Intent.HARMFUL, playerEntity, target.entity())) {


                            SpellHelper.performImpacts(playerEntity.getWorld(), playerEntity, target.entity(), target.entity(), registryEntry, registryEntry.value().impacts, impactContext.channeled(1.0F));
                        }
                    }
                    Supplier<Collection<ServerPlayerEntity>> trackingPlayers = Suppliers.memoize(() -> {
                        return PlayerLookup.tracking(playerEntity);
                    });
                    ParticleHelper.sendBatches(playerEntity, registryEntry.value().release.particles);
                    SoundHelper.playSound(world, playerEntity, registryEntry.value().release.sound);
                    AnimationHelper.sendAnimation(playerEntity, (Collection)trackingPlayers.get(), SpellCast.Animation.RELEASE, registryEntry.value().release.animation, 1.0F);

                    SpellHelper.imposeCooldown(playerEntity, SpellContainerSource.getFirstSourceOfSpell(Identifier.of(registryEntry.getIdAsString()),playerEntity),Identifier.of(registryEntry.getIdAsString()), registryEntry,1.0F);
                    return true;
                }
            }
            return false;


        }
    }

    public static void registerDeliveries(){
        registerCustomDelivery(Identifier.of(MOD_ID,"prepare"),new Prepare());

    }
}
