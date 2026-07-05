package com.cleannrooster.basic_skill_sets.networking;

import com.cleannrooster.basic_skill_sets.BasicSkillSets;
import com.cleannrooster.basic_skill_sets.BasicSkillSetsClient;
import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.client.ShieldFlashState;
import com.cleannrooster.basic_skill_sets.config.ConfigSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientNetworkHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSync.PACKET_ID, (payload, context) -> {
            BasicSkillSetsClient.clientConfig = payload.config();
            BasicSkillSets.config = payload.config();
        });

        ClientPlayNetworking.registerGlobalReceiver(Packet.HolsterAssert.PACKET_ID, (payload, context) -> {
            if (context.player() instanceof HitstopAccessor accessor) {
                accessor.setHolster(payload.bool());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Packet.ShieldFlash.PACKET_ID, (payload, context) -> {
            ShieldFlashState.startFlash(payload.entityId());
        });
    }
}
