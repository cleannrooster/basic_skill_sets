package com.cleannrooster.basic_skill_sets.api;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.util.math.Vec3d;

public interface HitstopAccessor {
    int getHitstopTicks();
    void setVelocityHitstop(Vec3d vec3d);
    void setHitstop(int hitstop);
    void setImpulseVector(Vec3d vec3d);
    Vec3d getImpulseVector();
    Vec3d getVelocityHitstop();
    void setHitstopTime(int hitstopTime);
    int getHitstopTime();
    void setLastAttackedTemporary(long time);
    boolean isHolster();
    boolean shouldClamp();
    void setShouldClamp(boolean shouldClamp);
    void setHolster(boolean holster);
}
