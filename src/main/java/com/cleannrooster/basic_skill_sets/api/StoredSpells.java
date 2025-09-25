package com.cleannrooster.basic_skill_sets.api;

import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;

import java.util.ArrayList;
import java.util.List;

public class StoredSpells {
    public static List<RegistryEntry<Spell>> spellList = new ArrayList<>();
    public static RegistryEntry<Spell> NULLSPELL;


    public static void setSpellSlot(RegistryEntry<Spell> spell, int i){
        spellList.set(i,spell);
    }
    public static void nullSpellSlot(RegistryEntry<Spell> spell, int i){
        spellList.set(i,NULLSPELL);
    }
}
