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

package dev.ultreon.qvoxel.item;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.ubo.DataTypes;
import dev.ultreon.ubo.types.MapType;

public class ItemStack {
    public static final PacketCodec<ItemStack> PACKET_CODEC = PacketCodec.packed(
            Item.PACKET_CODEC, ItemStack::getItem,
            PacketCodec.INT, ItemStack::getCount,
            PacketCodec.ubo().nullable(), ItemStack::getData,
            ItemStack::new
    );

    public static final ItemStack EMPTY = new ItemStack(Items.AIR, 0);

    private Item item;
    private int count;
    private MapType data;
    public ItemStack(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public ItemStack(Item item, int count, MapType data) {
        this.item = item;
        this.count = count;
        this.data = data;
    }

    public ItemStack(ItemStack stack) {
        this(stack.item, stack.count);
    }

    public ItemStack(Item item) {
        this(item, 1);
    }

    public ItemStack() {
        this(Items.AIR, 0);
    }

    public static ItemStack empty() {
        return EMPTY;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    private MapType getData() {
        return data;
    }

    public boolean isEmpty() {
        return count <= 0 || item == Items.AIR;
    }

    public ItemStack copy() {
        return new ItemStack(item, count);
    }

    public void shrink(int amount) {
        count -= amount;
    }

    public void grow(int amount) {
        count += amount;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return count + "x " + item.getId();
    }

    /**
     *
     * @param slotItem
     * @param transferAmount
     * @return the amount of items overflown
     */
    public int transferTo(ItemStack slotItem, int transferAmount) {
        if (slotItem.isEmpty()) {
            slotItem.item = item;
            slotItem.count = count;
            count = 0;
            return 0;
        }
        if (slotItem.item != item) return transferAmount;

        if (slotItem.count + count > slotItem.item.getMaxStackSize()) {
            int amount = slotItem.item.getMaxStackSize() - slotItem.count;
            count -= amount;
            slotItem.count += amount;
            return transferAmount - amount;
        }

        count -= transferAmount;
        slotItem.count += transferAmount;
        return 0;
    }

    public ItemStack split() {
        return split(1);
    }

    public ItemStack split(int amount) {
        if (amount > count) return null;
        ItemStack stack = new ItemStack(item, amount);
        shrink(amount);
        return stack;
    }

    public void clear() {
        count = 0;
    }

    public MapType save() {
        MapType map = new MapType();
        map.putString("item", String.valueOf(item.getId()));
        map.putInt("count", count);
        if (data != null) map.put("Data", data);
        return map;
    }

    public void write(PacketIO packetIO) {
        packetIO.writeInt(item.getRawId(packetIO));
        packetIO.writeInt(count);
        packetIO.writeUbo(data == null ? new MapType() : data);
    }

    public static ItemStack load(MapType map) {
        var item = Items.get(Identifier.parse(map.getString("item")));
        var count = map.getInt("count");
        if (map.contains("Data", DataTypes.MAP)) {
            var data = map.getMap("Data");
            return new ItemStack(item, count, data);
        }
        return new ItemStack(item, count);
    }

    public static ItemStack read(PacketIO packetIO) {
        var item = Items.get(packetIO.readInt(), packetIO);
        var count = packetIO.readInt();
        MapType data = packetIO.readUbo();
        return new ItemStack(item, count, data);
    }

    public boolean sameItemSameData(ItemStack slotItem) {
        return item == slotItem.item;
    }
}
