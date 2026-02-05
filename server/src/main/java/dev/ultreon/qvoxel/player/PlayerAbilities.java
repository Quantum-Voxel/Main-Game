package dev.ultreon.qvoxel.player;

import dev.ultreon.qvoxel.network.packets.c2s.C2SPlayerAbilitiesPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CAbilitiesPacket;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.Nullable;

public class PlayerAbilities {
    public boolean flying;
    public boolean canFly;
    public boolean invulnerable;
    public boolean instantMine;
    public float flyingSpeed = 0.22f;
    public float walkingSpeed = 0.1f;

    public PlayerAbilities() {
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean canFly() {
        return canFly;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public float getFlyingSpeed() {
        return flyingSpeed;
    }

    public float getWalkingSpeed() {
        return walkingSpeed;
    }

    public void onPacket(S2CAbilitiesPacket packet) {
        flying = packet.flying();
        canFly = packet.canFly();
        invulnerable = packet.invulnerable();
        flyingSpeed = packet.flyingSpeed();
        walkingSpeed = packet.walkingSpeed();
    }

    public void onPacket(C2SPlayerAbilitiesPacket packet) {
        flying = packet.flying();
    }

    @Override
    public String toString() {
        return "PlayerAbilities{" + "flying=" + flying + ", canFly=" + canFly + ", invulnerable=" + invulnerable + ", flyingSpeed=" + flyingSpeed + ", walkingSpeed=" + walkingSpeed + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAbilities that = (PlayerAbilities) o;
        return flying == that.flying && canFly == that.canFly && invulnerable == that.invulnerable && Float.compare(flyingSpeed, that.flyingSpeed) == 0 && Float.compare(walkingSpeed, that.walkingSpeed) == 0;
    }

    @Override
    public int hashCode() {
//        return Objects.hash(flying, canFly, invulnerable, flyingSpeed, walkingSpeed);
        int result = (flying ? 1 : 0) << 31;
        result |= (canFly ? 1 : 0) << 30;
        result |= (invulnerable ? 1 : 0) << 29;
        result = 31 * result + Float.floatToIntBits(flyingSpeed);
        result = 31 * result + Float.floatToIntBits(walkingSpeed);
        return result;
    }

    public PlayerAbilities copy() {
        PlayerAbilities copy = new PlayerAbilities();
        copy.flying = flying;
        copy.canFly = canFly;
        copy.invulnerable = invulnerable;
        copy.flyingSpeed = flyingSpeed;
        copy.walkingSpeed = walkingSpeed;
        return copy;
    }

    public void set(PlayerAbilities abilities) {
        flying = abilities.flying;
        canFly = abilities.canFly;
        invulnerable = abilities.invulnerable;
        flyingSpeed = abilities.flyingSpeed;
        walkingSpeed = abilities.walkingSpeed;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public MapType save() {
        MapType data = new MapType();
        data.putBoolean("canFly", canFly);
        data.putBoolean("flying", flying);
        data.putBoolean("invulnerable", invulnerable);
        data.putFloat("flyingSpeed", flyingSpeed);
        data.putFloat("walkingSpeed", walkingSpeed);
        return data;
    }

    public void load(@Nullable MapType data) {
        if (data == null) return;

        canFly = data.getBoolean("canFly", canFly);
        flying = data.getBoolean("flying", flying);
        invulnerable = data.getBoolean("invulnerable", invulnerable);
        flyingSpeed = data.getFloat("flyingSpeed", flyingSpeed);
        walkingSpeed = data.getFloat("walkingSpeed", walkingSpeed);
    }
}
