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
import net.spell_engine.api.item.SpellBooks;
import net.spell_engine.api.item.trinket.ISpellBookItem;
import net.spell_engine.api.item.trinket.SpellBookItem;
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

import static net.spell_engine.api.spell.container.SpellContainerHelper.contentTypeForSpell;
import static net.spell_engine.api.spell.container.SpellContainerHelper.createForShield;

public class SkillSources {
    public LinkedHashMap<String, List<Identifier>> skills;
    public SpellContainerSource.Entry HOTBAR_SKILL_SOURCE;

    public SkillSources(LinkedHashMap<String, List<Identifier>> skillList) {
        this.skills = skillList;
        SpellContainerSource.ItemEntry OFFHAND_SKILLSOURCE = SpellContainerSource.ItemEntry.of("offhand_skills", new SpellContainerSource.ItemStackSource() {
            public List<ItemStack> getSpellContainerItemStacks(PlayerEntity playerEntity, String s) {
                return List.of(playerEntity.getOffHandStack());
            }

            public List<SpellContainerSource.SourcedContainer> getSpellContainers(PlayerEntity player, String name) {
                this.getSpellContainerItemStacks(player, name);
                ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();
                ItemStack item = player.getOffHandStack();
                if (!item.isEmpty() && item.getItem() instanceof ShieldItem) {
                    SpellContainer source = SkillSources.createNonItemSpellContainer(name, player, List.of(Identifier.of("basic-skill-sets", "shield_charge"), Identifier.of("basic-skill-sets", "shield_bash")));
                    sources.add(new SpellContainerSource.SourcedContainer(name, item, source));
                }

                return sources;
            }
        });
        SpellContainerSource.ItemEntry SKILL_SOURCE = this.getEntry();
        SpellContainerSource.addItemSource(SKILL_SOURCE);
        SpellContainerSource.addItemSource(OFFHAND_SKILLSOURCE);
    }

    private SpellContainerSource.@NotNull ItemEntry getEntry() {
        final FallbackConfig.CompatibilitySpecifier[] specifiers = FallbackConfig.createDefault().fallback_compatibility;
        SpellContainerSource.ItemEntry SKILL_SOURCE = SpellContainerSource.ItemEntry.of("basic_skills_hotbar", new SpellContainerSource.ItemStackSource() {
            public List<ItemStack> getSpellContainerItemStacks(PlayerEntity playerEntity, String s) {
                List<ItemStack> stacks = new ArrayList();

                for(int i = 0; i < 9; ++i) {
                    stacks.add(playerEntity.getInventory().getStack(i));
                }

                return stacks;
            }

            public List<SpellContainerSource.SourcedContainer> getSpellContainers(PlayerEntity player, String name) {
                List<ItemStack> itemStacks = this.getSpellContainerItemStacks(player, name);
                ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();

                for(ItemStack stack : itemStacks) {
                    for(FallbackConfig.CompatibilitySpecifier fallbackOption : specifiers) {
                        if (PatternMatching.matches(stack.getItem().toString(), fallbackOption.item_id_regex) && !stack.isEmpty() && SpellContainerHelper.hasUsableContainer(stack)) {
                            Identifier id2 = Registries.ITEM.getId(stack.getItem());
                            if (SkillSources.this.sourceForWeapon(name, stack, player, fallbackOption.weapon_attributes) != null) {
                                sources.add(SkillSources.this.sourceForWeapon(name, stack, player, fallbackOption.weapon_attributes));
                                break;
                            }
                        }
                    }
                }

                return sources;
            }
        });
        return SKILL_SOURCE;
    }

    private static void addSourceIfValid(ItemStack fromItemStack, List<SpellContainerSource.SourcedContainer> sources, String name) {
        SpellContainer container = SpellContainerHelper.containerFromItemStack(fromItemStack);
        if (container != null && container.isValid()) {
            sources.addFirst(new SpellContainerSource.SourcedContainer(name, fromItemStack, container));
        }

    }

    private void addSourceToItemType(LivingEntity player, ItemStack stack, List<SpellContainerSource.SourcedContainer> sources) {
    }

    public SpellContainerSource.@Nullable SourcedContainer sourceForWeapon(String sourceName, ItemStack stack, PlayerEntity player, String string) {
        SpellContainer source = SpellContainer.EMPTY;
        if (this.skillsForWeapon(string) != null) {
            source = createNonItemSpellContainer(sourceName, player, this.skillsForWeapon(string));
            return source != null && !source.equals(SpellContainer.EMPTY) ? new SpellContainerSource.SourcedContainer(sourceName, stack, source) : null;
        } else {
            return null;
        }
    }

    public List<Identifier> skillsForWeapon(String string) {
        return (List)this.skills.get(string);
    }

    public static SpellContainer createNonItemSpellContainer(String string, PlayerEntity player, List<Identifier> spells) {
        if (spells != null && !spells.isEmpty()) {
            if (SpellRegistry.from(player.getWorld()).getEntry((Identifier)spells.get(0)).isPresent()) {
                Spell spell = (Spell)((RegistryEntry.Reference)SpellRegistry.from(player.getWorld()).getEntry((Identifier)spells.get(0)).get()).value();
                SpellContainer.ContentType contentType = SpellContainerHelper.contentTypeForSpell(spell);
                List<String> spellIds = spells.stream().filter((entry) -> SpellContainerHelper.contentTypeForSpell(spell) == contentType && spell.type.equals(Spell.Type.ACTIVE)).map(Identifier::toString).toList();
                return new SpellContainer(contentType, true, string, spellIds.size(), spellIds);
            } else {
                return SpellContainer.EMPTY;
            }
        } else {
            return SpellContainer.EMPTY;
        }
    }
}