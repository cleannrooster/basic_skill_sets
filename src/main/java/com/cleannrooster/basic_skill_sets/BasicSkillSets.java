package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.SkillSources;
import net.bettercombat.config.FallbackConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.SpellProjectile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

public class BasicSkillSets implements ModInitializer {
	public static final String MOD_ID = "basic-skill-sets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static SkillSources sources;
	public static LinkedHashMap<String, List<Identifier>> skills = new LinkedHashMap<String, List<Identifier>>();

	public static SkillSources getSkillSources(){
		return sources;
	}
	static{
		skills.put("bettercombat:claymore",    List.of(Identifier.of(BasicSkillSets.MOD_ID, "vaulting_slam")));
		skills.put("bettercombat:hammer",      List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "slam")));
		skills.put("bettercombat:halberd",     List.of(Identifier.of(BasicSkillSets.MOD_ID, "prepare"),
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:scythe",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:glaive",      List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "prepare")));
		skills.put("bettercombat:double_axe",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:spear",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "spear_throw")));

		skills.put("bettercombat:lance",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:anchor",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:battlestaff", List.of(Identifier.of(BasicSkillSets.MOD_ID, "vaulting_slam")));
		skills.put("bettercombat:claw",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "whirling_assault")));
		skills.put("bettercombat:fist",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "whirling_assault")));
		skills.put("bettercombat:trident",     List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "prepare"),
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:katana",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:rapier",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:sickle",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:soul_knife",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:dagger",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:mace",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "slam")));
		skills.put("bettercombat:axe",         List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:coral_blade", List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:twin_blade",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:cutlass",     List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:sword",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "flourish")
		));
	}
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.


		ServerLifecycleEvents.SERVER_STARTING.register((server -> {
			sources =  new SkillSources(skills);

		}));

		Spells.registerDeliveries();
		LOGGER.info("Hello Fabric world!");
	}
}