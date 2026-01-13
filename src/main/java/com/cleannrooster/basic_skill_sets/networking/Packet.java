package com.cleannrooster.basic_skill_sets.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public class Packet {
    public record Packets(float yaw, float pitch, float range) implements CustomPayload {
        public static Identifier ID = Identifier.of("basic-skill-sets", "particle");
        public static final CustomPayload.Id<Packets> PACKET_ID;
        public static final PacketCodec<RegistryByteBuf, Packets> CODEC;

        public CustomPayload.Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }

        public void write(RegistryByteBuf buffer) {
            buffer.writeFloat(this.yaw);
            buffer.writeFloat(this.pitch);
            buffer.writeFloat(this.range);
        }

        public static Packets readPacket(RegistryByteBuf buffer) {
            float yaw = buffer.readFloat();
            float pitch = buffer.readFloat();
            float range = buffer.readFloat();
            return new Packets(yaw, pitch, range);
        }

        static {
            PACKET_ID = new CustomPayload.Id(ID);
            CODEC = PacketCodec.of(Packets::write, Packets::readPacket);
        }
    }
    public record Impulse(int id, float mag, float mag2, float x, float y, float z) implements CustomPayload {
        public static Identifier ID = Identifier.of("basic-skill-sets", "move_enemy");
        public static final CustomPayload.Id<Impulse> PACKET_ID;
        public static final PacketCodec<RegistryByteBuf, Impulse> CODEC;

        public CustomPayload.Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }

        public void write(RegistryByteBuf buffer) {
            buffer.writeInt(id);
            buffer.writeFloat(this.mag);
            buffer.writeFloat(this.mag2);

            buffer.writeFloat(this.x);
            buffer.writeFloat(this.y);
            buffer.writeFloat(this.z);
        }

        public static Impulse readPacket(RegistryByteBuf buffer) {
            int id = buffer.readInt();
            float mag = buffer.readFloat();
            float mag2 = buffer.readFloat();

            float yaw = buffer.readFloat();
            float pitch = buffer.readFloat();
            float range = buffer.readFloat();
            return new Impulse(id,mag,mag2,yaw, pitch, range);
        }

        static {
            PACKET_ID = new CustomPayload.Id(ID);
            CODEC = PacketCodec.of(Impulse::write, Impulse::readPacket);
        }
    }
    public record SlowFirst() implements CustomPayload {
        public static Identifier ID = Identifier.of("basic-skill-sets", "first_attack");
        public static final CustomPayload.Id<SlowFirst> PACKET_ID;
        public static final PacketCodec<RegistryByteBuf, SlowFirst> CODEC;

        public CustomPayload.Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }

        public void write(RegistryByteBuf buffer) {

        }

        public static SlowFirst readPacket(RegistryByteBuf buffer) {

            return new SlowFirst();
        }

        static {
            PACKET_ID = new CustomPayload.Id(ID);
            CODEC = PacketCodec.of(SlowFirst::write, SlowFirst::readPacket);
        }
    }
}