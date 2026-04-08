package com.cleannrooster.basic_skill_sets.api;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.config.FallbackConfig;
import net.bettercombat.logic.WeaponAttributesFallback;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.utils.PatternMatching;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.spell_engine.api.item.weapon.SpellWeaponItem;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.mixin.registry.RegistryLoaderMixin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static net.spell_engine.api.spell.container.SpellContainerHelper.createForShield;

public class SkillSources {
    public LinkedHashMap<String, List<Identifier>> skills;
    public SpellContainerSource.Entry HOTBAR_SKILL_SOURCE;

    public static void init() {
        SpellContainerSource.ItemEntry OFFHAND_SKILLSOURCE = SpellContainerSource.ItemEntry.of("offhand_skills", new SpellContainerSource.ItemStackSource() {
            public List<ItemStack> getSpellContainerItemStacks(PlayerEntity playerEntity, String s) {
                return List.of(playerEntity.getMainHandStack());
            }

            public List<SpellContainerSource.SourcedContainer> getSpellContainers(PlayerEntity player, String name) {
                this.getSpellContainerItemStacks(player, name);
                ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();
                ItemStack item = player.getOffHandStack();
                var main = player.getMainHandStack();
                if (!item.isEmpty() && item.getItem() instanceof ShieldItem) {
                    if (!main.isEmpty()) {
                        SpellContainer source = SpellContainerHelper.containerFromItemStack(main);
                        if (source != null) {
                            sources.add(new SpellContainerSource.SourcedContainer(name, item, source.withAdditionalSpell(List.of("basic-skill-sets:shield_bash"))));
                        }
                    }
                }

                return sources;
            }
        });
        SpellContainerSource.addItemSource(OFFHAND_SKILLSOURCE);
    }



}