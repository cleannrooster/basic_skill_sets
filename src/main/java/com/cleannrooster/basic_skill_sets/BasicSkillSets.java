package com.cleannrooster.basic_skill_sets;

import com.cleannrooster.basic_skill_sets.api.HitstopAccessor;
import com.cleannrooster.basic_skill_sets.api.SkillSources;
import com.cleannrooster.basic_skill_sets.config.ConfigSync;
import com.cleannrooster.basic_skill_sets.config.ServerConfig;
import com.cleannrooster.basic_skill_sets.config.ServerConfigWrapper;
import com.cleannrooster.basic_skill_sets.networking.Packet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.event.CombatEvents;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellEvents;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.TargetHelper;
import net.spell_engine.utils.VectorHelper;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED;

public class BasicSkillSets implements ModInitializer {
	public static final String MOD_ID = "basic-skill-sets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ServerConfig config;
    public static ServerConfig clientConfig;
    public static SkillSources sources;
	public static LinkedHashMap<String, List<Identifier>> skills = new LinkedHashMap<String, List<Identifier>>();

	public static SkillSources getSkillSources(){
		return sources;
	}
	static{
		skills.put("bettercombat:claymore",    List.of(Identifier.of(BasicSkillSets.MOD_ID, "vaulting_slam")));
		skills.put("bettercombat:hammer",      List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "slam")));
		skills.put("bettercombat:halberd",     List.of(Identifier.of(BasicSkillSets.MOD_ID, "prepare"),
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:scythe",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:glaive",      List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "prepare")));
		skills.put("bettercombat:double_axe",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:spear",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "spear_throw")));

		skills.put("bettercombat:lance",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:anchor",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:battlestaff", List.of(Identifier.of(BasicSkillSets.MOD_ID, "vaulting_slam")));
		skills.put("bettercombat:claw",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "whirling_assault")));
		skills.put("bettercombat:fist",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "whirling_assault")));
		skills.put("bettercombat:trident",     List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "prepare"),
				Identifier.of(BasicSkillSets.MOD_ID, "charge")));
		skills.put("bettercombat:katana",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:rapier",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:sickle",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "devastating_cut")));
		skills.put("bettercombat:soul_knife",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:dagger",      List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:mace",        List.of(Identifier.of(BasicSkillSets.MOD_ID, "slam")));
		skills.put("bettercombat:axe",         List.of(Identifier.of(BasicSkillSets.MOD_ID, "skullcrusher")));
		skills.put("bettercombat:coral_blade", List.of(Identifier.of(BasicSkillSets.MOD_ID, "knife_throw")));
		skills.put("bettercombat:twin_blade",  List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:cutlass",     List.of(Identifier.of(BasicSkillSets.MOD_ID, "flourish")));
		skills.put("bettercombat:sword",       List.of(
				Identifier.of(BasicSkillSets.MOD_ID, "flourish")
		));
	}
    public static final  Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map;
    static{
        map = HashMultimap.<RegistryEntry<EntityAttribute>, EntityAttributeModifier>create();
        map.put(GENERIC_ATTACK_SPEED,new EntityAttributeModifier(Identifier.of(MOD_ID,"attack_speed_penalty_airborne"), 0.4F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    }
	@Override
	public void onInitialize() {

        AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        config = AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((ServerLifecycleEvents.SyncDataPackContents)(player, joined) -> ServerPlayNetworking.send(player, new ConfigSync(config)));
        PayloadTypeRegistry.playS2C().register(ConfigSync.PACKET_ID, ConfigSync.CODEC);
        PayloadTypeRegistry.playS2C().register(Packet.HolsterAssert.PACKET_ID, Packet.HolsterAssert.CODEC);

        PayloadTypeRegistry.playC2S().register(Packet.Packets.PACKET_ID, Packet.Packets.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.Impulse.PACKET_ID, Packet.Impulse.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.SlowFirst.PACKET_ID, Packet.SlowFirst.CODEC);
        PayloadTypeRegistry.playC2S().register(Packet.Holster.PACKET_ID, Packet.Holster.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Packet.Holster.PACKET_ID, (payload, context) ->{
                    if(context.player() instanceof HitstopAccessor hitstopAccessor){
                            hitstopAccessor.setHolster(payload.bool() || !hitstopAccessor.isHolster());

                    }
                }

        );
        ServerPlayNetworking.registerGlobalReceiver(Packet.SlowFirst.PACKET_ID, (payload, context) ->{
                    if(context.player() instanceof HitstopAccessor hitstopAccessor){
                        hitstopAccessor.setLastAttackedTemporary(context.player().getWorld().getTime());
                    }
                }

        );
        ServerPlayNetworking.registerGlobalReceiver(Packet.Impulse.PACKET_ID, (payload, context) ->{
                    Entity entity = context.player().getWorld().getEntityById(payload.id());
                    if(payload.shouldCheck()) {
                        Spell.Target.Area area = new Spell.Target.Area();

                        area.angle_degrees = 150;
                        List<Entity> list = TargetHelper.targetsFromArea(context.player(),  (float) context.player().getEntityInteractionRange() * 1.4F, area, null);

                        if (context.player() instanceof HitstopAccessor accessor) {
                            accessor.setShouldClamp(!list.isEmpty());
                        }
                    }

                    if(entity instanceof HitstopAccessor hitstopAccessor) {
                        hitstopAccessor.setImpulseVector (hitstopAccessor.getImpulseVector().multiply(payload.mag2()).add((new Vec3d(payload.x(), payload.y(), payload.z()))).multiply(payload.mag()));
                        entity.setVelocity(entity.getVelocity());
                        entity.velocityModified = true;
                        entity.velocityDirty = true;
                    }
                }

                );
        SpellEvents.PROJECTILE_SHOOT.register((spell) -> {
            if(BasicSkillSets.config.projectileSelfKnockback &&  spell.spellEntry().value().impacts.stream().anyMatch(impact -> {return impact.action.type.equals(Spell.Impact.Action.Type.DAMAGE);})) {
                if (spell.caster() instanceof HitstopAccessor hitstopAccessor &&  spell.spellEntry().value().deliver.type.equals(Spell.Delivery.Type.PROJECTILE)){
                    double speed = 0;
                    for(Spell.Impact impact : spell.spellEntry().value().impacts){
                        if(impact.action.type.equals(Spell.Impact.Action.Type.DAMAGE) ){
                            speed += 0.5F*impact.action.damage.spell_power_coefficient* Math.pow(SpellPower.getSpellPower(impact.school != null ? impact.school : spell.spellEntry().value().school,spell.caster()).baseValue(),0.73118F
                            )/10F;

                        }

                    }
                 /*   if(spell.spellEntry().value().active != null && spell.spellEntry().value().active.cast != null && spell.spellEntry().value().active.cast.channel_ticks > 0){
                        speed *= (double) spell.spellEntry().value().active.cast.channel_ticks /20;
                    }*/
                    System.out.println(spell.caster().getRotationVector().normalize().multiply(speed, (double)speed, speed).multiply(-1).length());
                    hitstopAccessor.setImpulseVector(hitstopAccessor.getImpulseVector().add( spell.caster().getRotationVector().normalize().multiply(speed, (double)speed, speed).multiply(-1)));
                    spell.caster().velocityDirty = true;
                    spell.caster().velocityModified = true;

                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Packet.Packets.PACKET_ID, (payload, context) -> spawnParticlesSlash(context.player(), context.player().getServerWorld(), payload.yaw(), payload.pitch(), payload.range()));
        ServerLifecycleEvents.SERVER_STARTING.register((ServerLifecycleEvents.ServerStarting)(server) -> clientConfig = config);
        CombatEvents.ENTITY_SHIELD_BLOCK.register((CombatEvents.EntityShieldBlock)(c) -> {
            Entity patt0$temp = c.entity();
            if (patt0$temp instanceof PlayerEntity player) {
                if (c.source().getAttacker() != null && (double)c.source().getAttacker().distanceTo(c.entity()) < player.getEntityInteractionRange()) {
                    Entity patt1$temp = c.source().getAttacker();
                    if (patt1$temp instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)patt1$temp;
                        ((WorldScheduler)c.entity().getWorld()).schedule(10, () -> {
                            if (!c.entity().isBlocking() && c.source().getAttacker() != null) {

                                SpellHelper.performSpell(c.entity().getWorld(), player, (RegistryEntry<Spell>) SpellRegistry.from(player.getWorld()).getEntry(Identifier.of("basic-skill-sets", "shield_bash")).get(), new SpellTarget.SearchResult(List.of(c.source().getAttacker()), c.source().getAttacker().getPos()), SpellCast.Action.TRIGGER, 1.0F);
                            }

                        });
                    }
                }
            }

        });
        ServerTickEvents.END_WORLD_TICK.register((server) -> {
            if(!server.getPlayers().isEmpty()){
                for(PlayerEntity player: server.getPlayers()){
                    if(player.age % 100 == 0 &&  player instanceof ServerPlayerEntity playerEntity && player instanceof HitstopAccessor accessor){
                        ServerPlayNetworking.send(playerEntity,new Packet.HolsterAssert(accessor.isHolster()));
                    }
                    if( player instanceof SpellCasterEntity entity){
                        if( entity.getSpellCastProcess() != null && entity.getCurrentSpell() != null && entity.isCastingSpell() &&
                                (entity.getCurrentSpell().equals(SpellRegistry.from(server).get(Identifier.of("rogues","whirlwind")))
                        || entity.getCurrentSpell().equals(SpellRegistry.from(server).get(Identifier.of("spellbladenext","eviscerate")))
                        || entity.getCurrentSpell().equals(SpellRegistry.from(server).get(Identifier.of("spellbladenext","whirling_assault"))))){
                            RegistryEntry<Spell> spell = entity.getSpellCastProcess().spell();
                            spawnParticlesSlash(player,server,player.age % 10 * 36, 270+player.getRandom().nextBetween(-15,15),1.0F, (float) SpellHelper.getRange(player,spell));

                        }
                    }
                }
            }
        });
        SkillSources.init();
        Spells.registerDeliveries();
        LOGGER.info("Hello Fabric world!");
	}
    public static void spawnParticlesSlash(LivingEntity entity, ServerWorld world,float offset, float yaw, float pitch, float range) {
        int iii = -200;

        for(int i = 0; i < 5; ++i) {
            for(int ii = 0; ii < 80; ++ii) {
                ++iii;
                int finalIii = iii;
                int finalIi = ii;
                ((WorldScheduler)world).schedule(i + 1, () -> {
                    if (world instanceof ServerWorld) {
                        ServerWorld serverWorld = world;
                        double x = (double)0.0F;
                        double x2 = (double)0.0F;
                        double z = (double)0.0F;
                        x = ((double)(range * entity.getWidth()) + (double)(range * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.cos((double) finalIii / 126.96);
                        x2 = -((1.2 * (double)range * (double)entity.getWidth() + (double)(range * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.cos((double) finalIii / 126.96));
                        z = (double)pitch * (1.2 * (double)range * (double)entity.getWidth() + (double)(0.0F * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.sin((double) finalIii / 126.96);
                        float f7 = entity.getYaw() + -90.0F + offset;
                        float f = yaw - 90.0F;
                        Vec3d vec3d2 = rotate(x2, (double)0.0F, z, Math.toRadians((double)(-f7)), Math.toRadians((double)f), (double)0.0F);
                        vec3d2 = VectorHelper.rotateTowards(vec3d2, new Vec3d((double)0.0F, (double)-1.0F, (double)0.0F), (double)0);
                        Vec3d vec3d4 = vec3d2.add(entity.getEyePos().getX(), entity.getEyeY(), entity.getEyePos().getZ());
                        double y = entity.getY() + (double)(entity.getHeight() / 2.0F);
                        double sigma = 0.6;
                        double distance = vec3d2.length();
                        double p = (Math.abs(range-distance) >= range) ? 0.0 : Math.exp(-((range-distance) * (range-distance)) / ((range-2*entity.getWidth()) * sigma * sigma));
                        double p2 = (Math.abs(range-distance) >= range) ? 0.0 : Math.exp(-((range-distance) * (range-distance)) / ((1) * sigma * sigma));

                        for(ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                        }

                        if (entity instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity) entity;
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                        }
                    }

                });
            }
        }

    }

    public static void spawnParticlesSlash(LivingEntity entity, ServerWorld world, float yaw, float pitch, float range) {
        int iii = -200;

        for(int i = 0; i < 5; ++i) {
            for(int ii = 0; ii < 80; ++ii) {
                ++iii;
                int finalIii = iii;
                int finalIi = ii;
                ((WorldScheduler)world).schedule(i + 1, () -> {
                    if (world instanceof ServerWorld) {
                        ServerWorld serverWorld = world;
                        double x = (double)0.0F;
                        double x2 = (double)0.0F;
                        double z = (double)0.0F;
                        x = ((double)(range * entity.getWidth()) + (double)(range * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.cos((double) finalIii / 126.96);
                        x2 = -((1.2 * (double)range * (double)entity.getWidth() + (double)(range * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.cos((double) finalIii / 126.96));
                        z = (double)pitch * (1.2 * (double)range * (double)entity.getWidth() + (double)(0.0F * entity.getWidth()) * Math.sin((double)20.0F * ((double) finalIii / 126.96))) * Math.sin((double) finalIii / 126.96);
                        float f7 = entity.getYaw() + -90.0F;
                        float f = yaw - 90.0F;
                        Vec3d vec3d2 = rotate(x2, (double)0.0F, z, Math.toRadians((double)(-f7)), Math.toRadians((double)f), (double)0.0F);
                        vec3d2 = VectorHelper.rotateTowards(vec3d2, new Vec3d((double)0.0F, (double)-1.0F, (double)0.0F), (double)entity.getPitch());
                        Vec3d vec3d4 = vec3d2.add(entity.getEyePos().getX(), entity.getEyeY(), entity.getEyePos().getZ());
                        double y = entity.getY() + (double)(entity.getHeight() / 2.0F);
                        double sigma = 0.6;
                        double distance = vec3d2.length();
                        double p = (Math.abs(range-distance) >= range) ? 0.0 : Math.exp(-((range-distance) * (range-distance)) / ((range-2*entity.getWidth()) * sigma * sigma));
                        double p2 = (Math.abs(range-distance) >= range) ? 0.0 : Math.exp(-((range-distance) * (range-distance)) / ((1) * sigma * sigma));

                        for(ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                        }

                        if (entity instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity)entity;
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPARK, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.FLOAT).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                            }
                            if (player.getRandom().nextFloat() < p2) {
                                serverWorld.spawnParticles(player, SpellEngineParticles.MagicParticles.get(SpellEngineParticles.MagicParticles.Shape.SPELL, SpellEngineParticles.MagicParticles.Motion.BURST).particleType(), true, vec3d4.getX(), vec3d4.getY(), vec3d4.getZ(), 1, (double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 0.0F);
                            }
                        }
                    }

                });
            }
        }

    }

    public static Vec3d rotate(double x, double y, double z, double pitch, double roll, double yaw) {
        double cosa = Math.cos(yaw);
        double sina = Math.sin(yaw);
        double cosb = Math.cos(pitch);
        double sinb = Math.sin(pitch);
        double cosc = Math.cos(roll);
        double sinc = Math.sin(roll);
        double Axx = cosa * cosb;
        double Axy = cosa * sinb * sinc - sina * cosc;
        double Axz = cosa * sinb * cosc + sina * sinc;
        double Ayx = sina * cosb;
        double Ayy = sina * sinb * sinc + cosa * cosc;
        double Ayz = sina * sinb * cosc - cosa * sinc;
        double Azx = -sinb;
        double Azy = cosb * sinc;
        double Azz = cosb * cosc;
        Vec3d vec3 = new Vec3d(Axx * x + Axy * y + Axz * z, Ayx * x + Ayy * y + Ayz * z, Azx * x + Azy * y + Azz * z);
        return vec3;
    }
}