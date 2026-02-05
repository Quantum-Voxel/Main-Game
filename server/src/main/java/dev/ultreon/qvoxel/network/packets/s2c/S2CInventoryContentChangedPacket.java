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

package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.menu.Inventory;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

import java.util.HashMap;
import java.util.Map;

import static dev.ultreon.qvoxel.network.packets.PacketCodec.*;

public record S2CInventoryContentChangedPacket(
        Map<Integer, ItemStack> changes
) implements Packet<InGameClientPacketHandler> {

    public static final PacketId<S2CInventoryContentChangedPacket> ID = new PacketId<>("clientbound/inventory_item_changed", S2CInventoryContentChangedPacket.class);
    public static final PacketCodec<S2CInventoryContentChangedPacket> CODEC = packed(
            map(Inventory.MAX_SLOTS, VAR_INT, ItemStack.PACKET_CODEC, HashMap::new), S2CInventoryContentChangedPacket::changes,
            S2CInventoryContentChangedPacket::new
    );

    public static S2CInventoryContentChangedPacket read(PacketIO buffer) {
        return new S2CInventoryContentChangedPacket(buffer.readMap(PacketIO::readInt, PacketIO::readItemStack));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeMap(changes, PacketIO::writeInt, PacketIO::writeItemStack);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInventoryItemChanged(this);
    }
}
