package com.cleannrooster.basic_skill_sets.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellProjectile;

@Config(
    name = "server_v1"
)
public class ServerConfig implements ConfigData {
    @Comment("Forward Lunge on attack")
    public boolean moveAttack = false;
    @Comment("Forward Lunge on attack Decay Coeff")
    public float impulseCoeff = 0.4F;
    @Comment("Forward Lunge on attack Max Coeff")
    public float maxImpulse = 1.6F;
    @Comment("Impact recoil")
    public boolean impactRecoil = false;
    @Comment("Enemy Directional Impact Move")
    public boolean impactEnemy = false;
    @Comment("Attack Particles")
    public boolean particles = false;
    @Comment("Hitstop on Self")
    public boolean hitstopSelf = false;
    @Comment("Hitstop on Enemies")
    public boolean hitstopEnemies = false;
    @Comment("Self-Knockback on Projectile Spell")
    public boolean projectileSelfKnockback = false;
    @Comment("40% attackspeed increase when comboing")
    public boolean comboSpeed = false;
    @Comment("Weapons are sized up depending on your entity range")
    public boolean rangeSize = false;
    @Comment("Enable Holster")
    public boolean holster = false;
    @Comment("Holster Speed Boost")
    public float holsterBoost = 1.4000F;
    @Comment("Spread heals over (healSpread) seconds")
    public float healSpread = 0;
}
