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

package dev.ultreon.qvoxel.menu;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.registry.Registries;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public class MenuType<T extends ContainerMenu> {
    private final MenuBuilder<T> menuBuilder;

    public MenuType(MenuBuilder<T> menuBuilder) {
        this.menuBuilder = menuBuilder;
    }

    public @Nullable T create(Entity entity) {
        return menuBuilder.create(this, entity);
    }

    public Identifier getId() {
        return Registries.MENU_TYPE.getId(this);
    }

    @Override
    public String toString() {
        return "MenuType[" + getId() + "]";
    }

    public int getRawId() {
        return Registries.MENU_TYPE.getRawId(this);
    }

    public interface MenuBuilder<T extends ContainerMenu> {
        @Nullable T create(MenuType<T> menuType, Entity entity);
    }
}
