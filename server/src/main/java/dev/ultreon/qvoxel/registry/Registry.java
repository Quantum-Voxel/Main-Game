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

package dev.ultreon.qvoxel.registry;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.qvoxel.event.RegistryDumpEvent;
import dev.ultreon.qvoxel.event.system.EventSystem;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistrySyncPacket;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.resource.GameComponent;
import dev.ultreon.qvoxel.resource.ReloadContext;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("GDXJavaUnsafeIterator")
public abstract class Registry<T> implements IdRegistry<T>, RegistryMap<RegistryKey<T>, T> {
    private static Logger dumpLogger = (level, msg, t) -> {
    };
    private static boolean frozen;
    private final BidiMap<RegistryKey<T>, T> registry = new DualHashBidiMap<>();
    private final BidiMap<Integer, T> idMap = new DualHashBidiMap<>();
    private final BidiMap<Integer, T> ogIdMap = new DualHashBidiMap<>();
    private final Class<T> type;
    private final Identifier id;
    private final boolean overrideAllowed;
    private final boolean syncDisabled;
    protected RegistryKey<Registry<T>> key;

    private final Map<Identifier, NamedTag<T>> tags = new HashMap<>();
    private int curId;
    private final PacketCodec<T> packetCodec = PacketCodec.ID.map(this::get, this::getId);

    protected Registry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        id = builder.key.id();
        this.key = builder.key;
        type = builder.type;
        overrideAllowed = builder.allowOverride;
        syncDisabled = builder.doNotSync;

        EventSystem.addListenerDefault(RegistryDumpEvent.class, event -> dumpRegistry());
    }

    protected Registry(Builder<T> builder) {
        id = builder.key.id();
        key = builder.key;
        type = builder.type;
        overrideAllowed = builder.allowOverride;
        syncDisabled = builder.doNotSync;
        key = RegistryKey.registry(this);

        EventSystem.addListenerDefault(RegistryDumpEvent.class, event -> dumpRegistry());
    }

    private RegistryKey<T> subspace(Identifier namespaceID) {
        return RegistryKey.of(key, namespaceID);
    }

    public static void freeze() {
        Registry.frozen = true;
    }

    public static void unfreeze() {
        Registry.frozen = false;
    }

    public static Logger getDumpLogger() {
        return dumpLogger;
    }

    public static void setDumpLogger(Logger dumpLogger) {
        Registry.dumpLogger = dumpLogger;
    }

    public Identifier id() {
        return id;
    }

    public RegistryKey<Registry<T>> key() {
        return key;
    }

    /**
     * Returns the id id of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the id id of it.
     */
    @Nullable
    public Identifier getId(T obj) {
        @Nullable RegistryKey<T> registryKey = registry.inverseBidiMap().get(obj);
        if (registryKey == null) return null;
        return registryKey.id();
    }

    /**
     * Returns the registry key of the given registered instance.
     *
     * @param obj the registered instance.
     * @return the registry key of it.
     */
    public RegistryKey<T> getKey(T obj) {
        return registry.inverseBidiMap().get(obj);
    }

    /**
     * Returns the registered instance from the given {@link Identifier}
     *
     * @param key the id id.
     * @return a registered instance of the type {@link T}.
     * @throws ClassCastException if the type is invalid.
     */
    public T get(@Nullable Identifier key) {
        return registry.get(RegistryKey.of(this.key, key));
    }

    public boolean contains(Identifier rl) {
        return registry.containsKey(RegistryKey.of(key, rl));
    }

    public void dumpRegistry() {
        Registry.getDumpLogger().log("Registry dump: " + type.getSimpleName());
        for (Map.Entry<RegistryKey<T>, T> entry : entries()) {
            T object = entry.getValue();
            Identifier rl = entry.getKey().id();

            Registry.getDumpLogger().log("  (" + rl + ") -> " + object);
        }
    }

    /**
     * Register an object.
     *
     * @param rl  the resource location.
     * @param val the register item value.
     */
    public T register(Identifier rl, T val) {
        if (!type.isAssignableFrom(val.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + val.getClass() + " expected assignable to " + type);

        RegistryKey<T> key = new RegistryKey<>(this.key, rl);
        if (registry.containsKey(key) && !overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + rl);

        int setId = curId++;
        idMap.put(setId, val);
        ogIdMap.put(setId, val);
        registry.put(key, val);
        return val;
    }

    public boolean isOverrideAllowed() {
        return overrideAllowed;
    }

    public boolean isSyncDisabled() {
        return syncDisabled;
    }

    public Collection<T> values() {
        return registry.values();
    }

    public Set<RegistryKey<T>> keys() {
        return registry.keySet();
    }

    public Set<Map.Entry<RegistryKey<T>, T>> entries() {
        return registry.entrySet();
    }

    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public boolean isEmpty() {
        return registry.isEmpty();
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public int getRawId(T object) {
        int theKey = idMap.inverseBidiMap().getOrDefault(object, -1);
        if (theKey == -1) throw new NoSuchElementException("No such element: " + object);
        return theKey;
    }

    @Override
    public @Nullable T byRawId(int id) {
        return idMap.get(id);
    }

    public void register(RegistryKey<T> id, T element) {
        if (!type.isAssignableFrom(element.getClass()))
            throw new IllegalArgumentException("Not allowed type detected, got " + element.getClass() + " expected assignable to " + type);

        if (registry.containsKey(id) && !overrideAllowed)
            throw new IllegalArgumentException("Already registered: " + id);

        registry.put(id, element);
        ogIdMap.put(curId, element);
        idMap.put(curId++, element);
    }

    public T get(RegistryKey<T> key) {
        T value = registry.get(key);
        if (value == null) throw new NoSuchElementException("No such element: " + key);
        return value;
    }

    public void reload(ReloadContext context) {
        for (NamedTag<T> tag : tags.values())
            tag.reload(context);
    }

    public Optional<NamedTag<T>> getTag(Identifier namespaceID) {
        NamedTag<T> tag = tags.get(namespaceID);
        if (tag == null) return Optional.empty();

        return Optional.of(tag);
    }

    public NamedTag<T> createTag(Identifier namespaceID) {
        NamedTag<T> tag = new NamedTag<>(namespaceID, this);
        tags.put(namespaceID, tag);

        return tag;
    }

//    public void send(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
//        connection.send(new S2CRegistrySync(this));
//    }

    @Override
    public RegistryKey<T> nameById(int rawID) {
        return registry.inverseBidiMap().get(idMap.get(rawID));
    }

    @Override
    public int idByName(RegistryKey<T> key) {
        int rawId = idMap.inverseBidiMap().getOrDefault(registry.get(key), -1);
        if (rawId == -1) throw new RegistryException("Missing key: " + key);
        return rawId;
    }

    public boolean contains(RegistryKey<T> registryKey) {
        return registry.containsKey(registryKey);
    }

    public void sync(Map<Integer, Identifier> registryMap) {
        if (syncDisabled) return;
        idMap.clear();
        for (var e : registryMap.entrySet()) {
            idMap.put(e.getKey(), get(e.getValue()));
        }
    }

    public void unload() {
        idMap.clear();
        idMap.putAll(ogIdMap);
    }

    public void send(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        connection.send(new S2CRegistrySyncPacket(this));
    }

    public void unregister(Identifier id) {
        RegistryKey<T> key = RegistryKey.of(this.key, id);
        if (!registry.containsKey(key)) return;
        registry.remove(key);
        idMap.remove(idMap.inverseBidiMap().get(registry.get(key)));
        ogIdMap.remove(ogIdMap.inverseBidiMap().get(registry.get(key)));
    }

    public PacketCodec<T> packetCodec() {
        return packetCodec;
    }

    public static abstract class Builder<T> {
        private final Class<T> type;
        private final RegistryKey<Registry<T>> key;
        private boolean allowOverride = false;
        private boolean doNotSync = false;

        @SafeVarargs
        @SuppressWarnings("unchecked")
        @Deprecated
        public Builder(Identifier id, T... typeGetter) {
            type = (Class<T>) typeGetter.getClass().getComponentType();
            key = RegistryKey.registry(id);
        }

        public Builder(RegistryKey<Registry<T>> block, T[] typeGetter) {
            type = (Class<T>) typeGetter.getClass().getComponentType();
            key = block;
        }

        public Builder<T> allowOverride() {
            allowOverride = true;
            return this;
        }

        public abstract Registry<T> build();

        public Builder<T> doNotSync() {
            doNotSync = true;
            return this;
        }
    }
}
