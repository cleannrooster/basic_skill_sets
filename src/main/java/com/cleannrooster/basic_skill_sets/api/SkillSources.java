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
import net.spell_engine.api.tags.SpellEngineItemTags;
import net.spell_engine.compat.trinkets.SpellBookTrinketItem;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.mixin.registry.RegistryLoaderMixin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static net.spell_engine.api.spell.container.SpellContainerHelper.contentTypeForSpell;

public class SkillSources {
    public  SkillSources(LinkedHashMap<String, List<Identifier>> skillList){

        skills = skillList;
        HOTBAR_SKILL_SOURCE = new SpellContainerSource.Entry("hotbar_skills", (player, sourceName) -> {
            ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();

            for(int i = 0; i < 9; ++i) {
                var item = player.getInventory().getStack(i);
                if(!item.isEmpty()) {
                    ArrayList<SpellContainerSource.SourcedContainer> sourcesFirst = new ArrayList();
                    SpellContainer container = SpellContainerHelper.containerFromItemStack(item);
                    if((item.getItem() instanceof ToolItem)&& SpellContainerHelper.containerFromItemStack(item) != null && !SpellContainerHelper.containerFromItemStack(item).equals(SpellContainer.EMPTY)) {
                        if (container != null && container.isValid()) {

                            sources.add(new SpellContainerSource.SourcedContainer(sourceName, item, SpellContainerHelper.containerFromItemStack(item).copyWith(
                                    SpellContainerHelper.containerFromItemStack(item).spell_ids().stream().filter(
                                            spellId ->
                                                    SpellRegistry.from(player.getWorld()).getEntry(Identifier.tryParse(spellId)).isPresent()
                                                            &&
                                                            SpellRegistry.from(player.getWorld())
                                                                    .getEntry(Identifier.tryParse(spellId)).get().value().type.equals(Spell.Type.ACTIVE)
                                    ).toList())));
                        }
                    }

                }
            }

            return sources;
        },LivingEntity::getMainHandStack);
        SpellContainerSource.sources.addFirst(HOTBAR_SKILL_SOURCE);
        var OFFHAND_SKILLSOURCE = new SpellContainerSource.Entry("offhand_skills", (player, sourceName) -> {
            ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();

                var item = player.getOffHandStack();
                if(!item.isEmpty()) {
                        if (item.getItem() instanceof ShieldItem) {
                            var source = createNonItemSpellContainer(sourceName, player, List.of(Identifier.of(BasicSkillSets.MOD_ID,"shield_charge")));

                            sources.add(new SpellContainerSource.SourcedContainer(sourceName,item,source));

                        }

                }


            return sources;
        },LivingEntity::getOffHandStack);

        var SKILL_SOURCE = getEntry();


        SpellContainerSource.sources.addLast(SKILL_SOURCE);
        SpellContainerSource.sources.addLast(OFFHAND_SKILLSOURCE);



    }

    @NotNull
    private SpellContainerSource.Entry getEntry() {
        FallbackConfig.CompatibilitySpecifier[] specifiers = FallbackConfig.createDefault().fallback_compatibility;

        var SKILL_SOURCE = new SpellContainerSource.Entry("basic_skills_hotbar", (player, sourceName) -> {
            ArrayList<SpellContainerSource.SourcedContainer> sources = new ArrayList();






                for (int i = 0; i < 9; ++i) {
                        for (var fallbackOption : specifiers) {

                        var stack2 = player.getInventory().getStack(i);
                        if (PatternMatching.matches(stack2.getItem().toString(), fallbackOption.item_id_regex)) {

                            if (!stack2.isEmpty() && SpellContainerHelper.containerFromItemStack(stack2) != null) {
                                var id2 = Registries.ITEM.getId(stack2.getItem());


                                if (sourceForWeapon(sourceName, stack2, player, fallbackOption.weapon_attributes) != null) {
                                    sources.add(sourceForWeapon(sourceName, stack2, player, fallbackOption.weapon_attributes));
                                    break;
                                }


                            }
                        }

                }
            }



        return sources;
    }, LivingEntity::getMainHandStack);
        return SKILL_SOURCE;
    }

    private static void addSourceIfValid(ItemStack fromItemStack, List<SpellContainerSource.SourcedContainer> sources, String name) {
        SpellContainer container = SpellContainerHelper.containerFromItemStack(fromItemStack);
        if (container != null && container.isValid()) {
            sources.addFirst(new SpellContainerSource.SourcedContainer(name, fromItemStack, container));
        }

    }
    public  LinkedHashMap<String, List<Identifier>> skills;

    public SpellContainerSource.Entry HOTBAR_SKILL_SOURCE ;

    private  void addSourceToItemType(PlayerEntity player, ItemStack stack, List<SpellContainerSource.SourcedContainer> sources) {

    }
@Nullable
    public  SpellContainerSource.SourcedContainer sourceForWeapon(String sourceName, ItemStack stack, PlayerEntity player, String string){
        SpellContainer source = SpellContainer.EMPTY;
        if(skillsForWeapon(string) != null) {
             source = createNonItemSpellContainer(sourceName, player, skillsForWeapon(string));
             if(source == null || source.equals(SpellContainer.EMPTY))
             {
                 return null;
             }
            return new SpellContainerSource.SourcedContainer(sourceName,stack,source);

        }
        else{
            return null;
        }
    };
    public  List<Identifier> skillsForWeapon(String string){
        return this.skills.get(string);
    };
    public static SpellContainer createNonItemSpellContainer(String string, PlayerEntity player, List<Identifier> spells) {
        if(spells != null && !spells.isEmpty()) {
            if(SpellRegistry.from(player.getWorld()).getEntry(spells.get(0)).isPresent()) {
                Spell spell = SpellRegistry.from(player.getWorld()).getEntry(spells.get(0)).get().value();

                SpellContainer.ContentType contentType = contentTypeForSpell((Spell) (spell));
                List<String> spellIds = spells.stream().filter((entry) -> {
                    return contentTypeForSpell((Spell) spell) == contentType && spell.type.equals(Spell.Type.ACTIVE);
                }).map(Identifier::toString).toList();

                return new SpellContainer(contentType, true, string, spellIds.size(), spellIds);
            }
            else{
                return SpellContainer.EMPTY;
            }
        }
        else{
            return SpellContainer.EMPTY;
        }
    }
}