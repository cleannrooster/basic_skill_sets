package com.cleannrooster.basic_skill_sets.mixin;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import com.cleannrooster.basic_skill_sets.api.SkillSources;
import com.cleannrooster.basic_skill_sets.api.StoredSpells;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.client.input.Keybindings;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Mixin(SpellHotbar.class)
public abstract class SlotMixin extends SpellHotbar {

    private static List<Slot> BSSSlotList = new ArrayList<>();



    @Inject(at = @At("HEAD"), method = "handle", cancellable = true)
    public @Nullable SpellHotbar.Handle handleBSS(ClientPlayerEntity player, GameOptions options) {
        StoredSpells.NULLSPELL = (SpellRegistry.from(player.getWorld()).getEntry(Identifier.of(BasicSkillSets.MOD_ID))).get();
        for(int i = 0; i < 9; i++) {
            RegistryEntry<Spell> spell = StoredSpells.NULLSPELL;
            StoredSpells.spellList.add(spell);

        }
        for(int i = 0; i < 9; i++) {
            RegistryEntry<Spell> spell = null;
            if(StoredSpells.spellList.size() <= i){
                spell = StoredSpells.spellList.get(i);
            }
            BSSSlotList.add(new Slot(spell, SpellCast.Mode.CHANNEL, player.getMainHandStack(), Keybindings.Wrapped.all().get(i) , null));
            i++;
        }

        return this.handle(player, BSSSlotList, options);
    }

}
