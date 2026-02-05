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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class SoundSystem implements ALObject {
    private final long device;
    private final ALCCapabilities alcCaps;
    private final ALCapabilities alCaps;
    private final SoundGroup musicGroup;
    private final SoundGroup uiGroup;
    private final SoundGroup worldGroup;
    private final long context;

    public SoundSystem() {
        device = ALC11.alcOpenDevice((ByteBuffer) null);
        if (device == 0) {
            context = 0;
            alcCaps = null;
            alCaps = null;
        } else {
            context = ALC11.alcCreateContext(device, (IntBuffer) null);
            alcCaps = ALC.createCapabilities(device);

            ALC11.alcMakeContextCurrent(context);

            int i = ALC11.alcGetError(device);
            if (i != ALC11.ALC_NO_ERROR)
                throw new RuntimeException("Failed to create OpenAL context: " + ALUtils.getErrorString(i));

            alCaps = AL.createCapabilities(alcCaps);

            CommonConstants.LOGGER.info("OpenAL version: {}", AL11.alGetString(AL11.AL_VERSION));
            CommonConstants.LOGGER.info("OpenAL renderer: {}", AL11.alGetString(AL11.AL_RENDERER));
            CommonConstants.LOGGER.info("OpenAL vendor: {}", AL11.alGetString(AL11.AL_VENDOR));
            CommonConstants.LOGGER.info("OpenAL extensions: {}", AL11.alGetString(AL11.AL_EXTENSIONS));
        }

        musicGroup = new SoundGroup("music", 4, device);
        uiGroup = worldGroup = new SoundGroup("world", 256 - 4, device);
    }

    public void update() {
        musicGroup.update();
        worldGroup.update();
    }

    public Sound createSound(@NotNull Resource resource) throws IOException {
        return createSound(resource.openStream());
    }

    public Sound createSound(Identifier identifier) throws IOException {
        Resource resource = QuantumClient.get().resourceManager.getResource(identifier);
        if (resource == null) throw new IOException("Resource not found: " + identifier);
        return createSound(resource);
    }

    public Sound createSound(@NotNull InputStream stream) throws IOException {
        if (device == 0) {
            return Sound.EMPTY;
        }

        byte[] data = stream.readAllBytes();
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer channels = mem.ints(0);
            IntBuffer sampleRate = mem.ints(0);
            ShortBuffer decoded = STBVorbis.stb_vorbis_decode_memory(buffer, channels, sampleRate);
            if (decoded == null) throw new IOException("Failed to decode audio");

            int bufferID = AL11.alGenBuffers();
            AL11.alBufferData(bufferID, channels.get(0) == 1 ? AL11.AL_FORMAT_MONO16 : AL11.AL_FORMAT_STEREO16, decoded, sampleRate.get(0));
            int error = AL11.alGetError();
            if (error != AL11.AL_NO_ERROR) throw new IOException("Failed to create sound buffer: " + ALUtils.getErrorString(error));

            return new Sound(bufferID, channels.get(0), sampleRate.get(0));
        }
    }

    @Override
    public void close() {
        ALC11.alcCloseDevice(device);
    }

    public SoundGroup getMusicGroup() {
        return musicGroup;
    }

    public SoundGroup getWorldGroup() {
        return worldGroup;
    }

    public SoundGroup getUiGroup() {
        return uiGroup;
    }
}
