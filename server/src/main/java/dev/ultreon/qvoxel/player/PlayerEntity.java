/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.player;

import dev.ultreon.qvoxel.CommandSender;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.menu.Inventory;
import dev.ultreon.qvoxel.menu.Menu;
import dev.ultreon.qvoxel.menu.MenuTypes;
import dev.ultreon.qvoxel.server.FoodStatus;
import dev.ultreon.qvoxel.server.GameMode;
import dev.ultreon.qvoxel.world.LivingEntity;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;

public abstract class PlayerEntity extends LivingEntity implements CommandSender {
    public int selected;
    public final Inventory inventory = new Inventory(MenuTypes.INVENTORY, this);
    public int regenFlashTimer;
    protected GameMode gameMode = GameMode.SURVIVAL;
    private final FoodStatus foodStatus = new FoodStatus(20, 5, 0);
    protected String username;
    private final ItemStack selectedItem = ItemStack.EMPTY;
    private Menu openMenu = null;
    protected final PlayerAbilities abilities = new PlayerAbilities();
    protected boolean crouching;

    protected PlayerEntity(World world) {
        super(world);
        inventory.build();
    }

    @Override
    public float getEyeHeight() {
        return 1.76f;
    }

    @Override
    public MapType save() {
        MapType save = super.save();
        save.put("Abilities", abilities.save());
        save.putByte("gameMode", (byte) gameMode.ordinal());
        save.putString("username", username);
        save.put("FoodStatus", foodStatus.save());
        return save;
    }

    @Override
    public void load(MapType map) {
        super.load(map);

        abilities.load(map.getMap("Abilities"));
        gameMode = GameMode.byOrdinal(map.getByte("gameMode", (byte) GameMode.SURVIVAL.ordinal()));
        username = map.getString("username", "Player");
        foodStatus.load(map.getMap("FoodStatus"));
    }

    @Override
    public float getMaxHealth() {
        return 20;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public FoodStatus getFoodStatus() {
        return foodStatus;
    }

    public void heal(float v) {
        health += v;
        health = Math.min(health, getMaxHealth());
    }

    public String getUsername() {
        return username;
    }

    public boolean isSpectator() {
        return gameMode == GameMode.SPECTATING;
    }

    public ItemStack getSelectedItem() {
        return selectedItem;
    }

    public Menu getOpenMenu() {
        return openMenu;
    }

    public void openMenu(Menu menu) {
        openMenu = menu;
    }

    public void closeMenu() {
        openMenu = null;
    }

    public void sendMessage(String message) {

    }

    public boolean isInvincible() {
        return gameMode == GameMode.BUILDING || gameMode == GameMode.SPECTATING || abilities.invulnerable;
    }

    public abstract void sendAbilitiesPacket(PlayerAbilities playerAbilities);

    public PlayerAbilities getAbilities() {
        return abilities;
    }

    public boolean isFlying() {
        return abilities.flying;
    }

    @Override
    public boolean isAffectedByGravity() {
        return !abilities.flying && !noGravity;
    }

    public void setFlying(boolean enabled) {
        if (abilities.canFly)
            abilities.flying = enabled;
        else abilities.flying = false;
        sendAbilitiesPacket(abilities);
    }

    public float getSpeed() {
        PlayerAbilities abilities = getAbilities();
        if (abilities.flying)
            return abilities.flyingSpeed;
        return abilities.walkingSpeed;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }
}
