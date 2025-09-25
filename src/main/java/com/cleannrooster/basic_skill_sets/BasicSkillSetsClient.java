package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.SkillSources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.casting.SpellCasterEntity;

import java.util.LinkedHashMap;
import java.util.List;

public class BasicSkillSetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {


        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            BasicSkillSets.sources =  new SkillSources(BasicSkillSets.skills);

        });
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, lines)->{
            if(itemStack.getItem() instanceof ShieldItem shieldItem){
                lines.add(Text.translatable("text.basic-skill-sets.shield").formatted(Formatting.GRAY));
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.player != null && ((SpellCasterEntity)client.player).getCurrentSpell() != null &&
                    (((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID
            ,"charge"))) || ((SpellCasterEntity)client.player).getCurrentSpell().equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID
                            ,"shield_charge"))))){
                if(((SpellCasterEntity)client.player).getSpellCastProcess().spellCastTicksSoFar(client.world.getTime()) > 15){
                    double speed = client.player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)  * 4;

                    client.player.setVelocity(client.player.getRotationVec(1).subtract(0, client.player.getRotationVec(1).y, 0).normalize().multiply(speed, 0 , speed).add(0, client.player.getVelocity().y, 0));
                    client.player.velocityDirty = true;
                }
            }
        });
    }
}
