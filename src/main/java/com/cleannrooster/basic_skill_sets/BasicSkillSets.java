package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.SkillSources;
import com.cleannrooster.basic_skill_sets.config.ServerConfig;
import com.cleannrooster.basic_skill_sets.config.ServerConfigWrapper;
import com.cleannrooster.basic_skill_sets.networking.ServerNetworkHandler;
import com.cleannrooster.basic_skill_sets.particle.ModParticles;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED;

public class BasicSkillSets implements ModInitializer {
    public static final String MOD_ID = "basic-skill-sets";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ServerConfig config;
    public static ServerConfig clientConfig;



    @Override
    public void onInitialize() {
        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
            ServerPlayNetworking.send(player, new com.cleannrooster.basic_skill_sets.config.ConfigSync(config))
        );

        ModParticles.register();
        ServerNetworkHandler.register();
        ServerEventHandlers.register();
        SkillSources.init();
        Spells.registerDeliveries();

        LOGGER.info("Hello Fabric world!");
    }
}
