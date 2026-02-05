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

package dev.ultreon.qvoxel.client.sound;

import dev.ultreon.qvoxel.CommonConstants;
import org.lwjgl.openal.AL11;

import java.util.BitSet;

public class SoundGroup {
    private final String name;
    private final SoundSource[] sources;
    private final long device;
    private final BitSet available = new BitSet();
    private final String msg;

    public SoundGroup(String name, int size, long device) {
        this.name = name;
        sources = new SoundSource[size];
        this.device = device;

        if (device != 0) {
            for (int i = 0; i < size; i++) {
                sources[i] = new SoundSource(i, AL11.alGenSources());
                available.set(i);
                int error = AL11.alGetError();
                if (error != AL11.AL_NO_ERROR)
                    throw new RuntimeException("Failed to create sound source: " + ALUtils.getErrorString(error));
            }
        }

        msg = "SoundGroup '" + name + "' is full";
    }

    public void update() {
        for (SoundSource source : sources) {
            if (source == null) continue;

            if (source.isStopped()) {
                release(source);
            }
        }
    }

    public void close() {
        for (SoundSource source : sources) {
            source.close();
        }
    }

    public SoundSource getSource(int index) {
        return sources[index];
    }

    public SoundSource findAvailable() {
        int i = available.nextSetBit(0);
        if (i == sources.length) {
            CommonConstants.LOGGER.warn(msg);
            return null;
        }
        return sources[i];
    }

    public void release(SoundSource source) {
        available.set(source.getIndex());
    }

    public void releaseAll() {
        available.set(0, sources.length);
        for (SoundSource source : sources) {
            source.stop();
        }
    }

    public SoundSource allocate() {
        if (device == 0) return null;
        synchronized (available) {
            int i = available.nextSetBit(0);
            if (i == -1) {
                CommonConstants.LOGGER.warn(msg);
                return null;
            }
            if (i == sources.length) {
                CommonConstants.LOGGER.warn(msg);
                return null;
            }

            available.clear(i);
            return sources[i];
        }
    }
}
