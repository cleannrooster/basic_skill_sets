package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.client.CombatEventsClient;
import com.cleannrooster.basic_skill_sets.client.ShieldFlashState;
import com.cleannrooster.basic_skill_sets.config.ServerConfig;
import com.cleannrooster.basic_skill_sets.networking.ClientNetworkHandler;
import com.cleannrooster.basic_skill_sets.networking.Packet;
import com.cleannrooster.basic_skill_sets.particle.ModParticles;
import com.cleannrooster.basic_skill_sets.particle.SlashFlashParticle;
import com.cleannrooster.basic_skill_sets.particle.SlashGlintParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.casting.SpellCasterEntity;
import org.lwjgl.glfw.GLFW;

public class BasicSkillSetsClient implements ClientModInitializer {
    public static ServerConfig clientConfig;
    public static KeyBinding holsterBinding;

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.SLASH_FLASH, SlashFlashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SLASH_GLINT, SlashGlintParticle.Factory::new);

        KeyBindingHelper.registerKeyBinding(holsterBinding = new KeyBinding(
            "basic-skill-sets.binds.holster",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "basic-skill-sets.binds.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ShieldFlashState.tick();
            if (holsterBinding.wasPressed() && client.player instanceof HitstopAccessor accessor) {
                accessor.setHolster(!accessor.isHolster());
                ClientPlayNetworking.send(new Packet.Holster(false));
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            var caster = (SpellCasterEntity) client.player;
            var currentSpell = caster.getCurrentSpell();
            if (currentSpell != null
                    && (currentSpell.equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID, "charge")))
                     || currentSpell.equals(SpellRegistry.from(client.world).get(Identifier.of(BasicSkillSets.MOD_ID, "shield_charge"))))
                    && caster.getSpellCastProcess().spellCastTicksSoFar(client.world.getTime()) > 15) {
                double speed = client.player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 4;
                client.player.setVelocity(
                    client.player.getRotationVec(1).subtract(0, client.player.getRotationVec(1).y, 0)
                        .normalize().multiply(speed, 0, speed)
                        .add(0, client.player.getVelocity().y, 0)
                );
                client.player.velocityDirty = true;
            }

            if (client.player instanceof HitstopAccessor hitstopAccessor
                    && hitstopAccessor.isHolster()
                    && client.options.attackKey.isPressed()) {
                client.player.sendMessage(
                    Text.translatable("text.holster.error").append(holsterBinding.getDefaultKey().getLocalizedText()), true
                );
            }
        });

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, lines) -> {
            if (itemStack.getItem() instanceof ShieldItem) {
                lines.add(Text.translatable("text.basic-skill-sets.shield").formatted(Formatting.GRAY));
            }
        });

        ClientNetworkHandler.register();
        CombatEventsClient.register();
    }
}
