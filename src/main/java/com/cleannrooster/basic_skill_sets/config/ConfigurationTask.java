package com.cleannrooster.basic_skill_sets.config;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.spell_engine.network.ServerNetwork;

import java.util.function.Consumer;

public record ConfigurationTask(ServerConfig config) implements ServerPlayerConfigurationTask {
    public static final String name = "basic-skill-sets:config";
    public static final ServerPlayerConfigurationTask.Key KEY = new ServerPlayerConfigurationTask.Key("basic-skill-sets:config");
    public ServerPlayerConfigurationTask.Key getKey() {
        return KEY;
    }

    public void sendPacket(Consumer<Packet<?>> sender) {
        ConfigSync packet = new ConfigSync(this.config);
        sender.accept(ServerConfigurationNetworking.createS2CPacket(packet));
    }
}
