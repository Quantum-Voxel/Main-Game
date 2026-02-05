/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.particle;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.network.PacketIO;

public class BlockParticleData extends ParticleData {
    public static final ParticleSerializer<BlockParticleData> SERIALIZER = new ParticleSerializer<>() {
        @Override
        public void toBytes(BlockParticleData data, PacketIO buffer) {
            ParticleData.SERIALIZER.toBytes(data, buffer);
            buffer.writeBlockState(data.getBlockState());
        }

        @Override
        public BlockParticleData fromBytes(PacketIO buffer) {
            return new BlockParticleData(buffer);
        }
    };

    private BlockState blockState;

    public BlockParticleData(BlockState blockState) {
        this.blockState = blockState;
    }

    public BlockParticleData(PacketIO buffer) {
        super(buffer);
        blockState = buffer.readBlockState();
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
    }
}
