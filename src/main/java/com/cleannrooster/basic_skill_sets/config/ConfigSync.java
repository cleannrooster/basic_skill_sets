package com.cleannrooster.basic_skill_sets.config;

import com.google.gson.Gson;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.spell_engine.network.Packets;

public record ConfigSync(ServerConfig config) implements CustomPayload {
    public static Identifier ID = Identifier.of("basic-skill-sets", "config_sync");
    public static final CustomPayload.Id<ConfigSync> PACKET_ID;
    public static final PacketCodec<RegistryByteBuf, ConfigSync> CODEC;
    private static final Gson gson;

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public void write(PacketByteBuf buffer) {
        String json = gson.toJson(this.config);
        buffer.writeString(json);
    }

    public static ConfigSync read(PacketByteBuf buffer) {
        Gson gson = new Gson();
        String json = buffer.readString();
        ServerConfig config = (ServerConfig)gson.fromJson(json, ServerConfig.class);
        return new ConfigSync(config);
    }

    static {
        PACKET_ID = new CustomPayload.Id(ID);
        CODEC = PacketCodec.of(ConfigSync::write, ConfigSync::read);
        gson = new Gson();
    }
}
