package com.cleannrooster.basic_skill_sets.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/**
 * A bright, short-lived particle representing the metallic flash of a blade edge
 * during a slash. Starts as an intense white-silver flash that rapidly expands
 * and fades, simulating light catching on a moving blade.
 */
@Environment(EnvType.CLIENT)
public class SlashFlashParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private final float startScale;

    protected SlashFlashParticle(ClientWorld world, double x, double y, double z,
                                 double velocityX, double velocityY, double velocityZ,
                                 SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;

        this.maxAge = 3 + this.random.nextInt(2); // 3-4 ticks - a brief metallic flash
        this.scale = 0.12F + this.random.nextFloat() * 0.06F;
        this.startScale = this.scale;

        // Bright white-silver color with slight blue-steel tint
        float colorVariance = 0.9F + this.random.nextFloat() * 0.1F;
        this.red = colorVariance;
        this.green = colorVariance;
        this.blue = 0.95F + this.random.nextFloat() * 0.05F; // Slightly cooler blue-steel tint

        this.alpha = 1.0F;

        // Inherit motion from the slash direction with slight damping
        this.velocityX = velocityX * 0.3;
        this.velocityY = velocityY * 0.3;
        this.velocityZ = velocityZ * 0.3;

        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        float lifeRatio = (float) this.age / (float) this.maxAge;

        // Flash: scale up quickly then shrink - simulates the blade edge catching light
        if (lifeRatio < 0.3F) {
            // Rapid expansion - the initial flash
            this.scale = this.startScale * (1.0F + lifeRatio * 4.0F);
        } else {
            // Quick shrink after the flash peak
            this.scale = this.startScale * (1.0F + 1.2F) * (1.0F - (lifeRatio - 0.3F) / 0.7F);
        }

        // Rapid alpha fade — quadratic for a sharp cutoff, then multiplied by proximity fade
        float ageFade = 1.0F - (lifeRatio * lifeRatio);
        this.alpha = ageFade * proximityFade();

        // Slight color shift toward steel-blue as it fades
        this.red = Math.max(0.7F, this.red - lifeRatio * 0.15F);
        this.green = Math.max(0.75F, this.green - lifeRatio * 0.1F);

        // Apply motion with strong damping
        this.velocityX *= 0.7;
        this.velocityY *= 0.7;
        this.velocityZ *= 0.7;

        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public int getBrightness(float tint) {
        return 0xF000F0;
    }

    /**
     * Smooth fade-to-zero as the particle approaches the camera eye.
     * Starts at 2.0 blocks, fully transparent at 0.8 blocks.
     * Uses squared distance to avoid sqrt, and a squared t for a smooth ease-in curve.
     */
    private float proximityFade() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 1.0f;
        double dx = mc.player.getX() - this.x;
        double dy = mc.player.getEyeY() - this.y;
        double dz = mc.player.getZ() - this.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq >= 4.00) return 1.0f;  // beyond 2.0 blocks — no fade
        if (distSq <= 0.64) return 0.0f;  // within 0.8 blocks — fully transparent
        float t = (float) ((distSq - 0.64) / (4.00 - 0.64));
        return t * t; // squared for a soft ease-in fade
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new SlashFlashParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
        }
    }
}
