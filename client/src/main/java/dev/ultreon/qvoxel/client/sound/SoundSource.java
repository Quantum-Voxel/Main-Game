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

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.openal.AL10.alListenerfv;
import static org.lwjgl.openal.AL11.*;

public class SoundSource {
    private final int index;
    private final int sourceID;

    public SoundSource(int index, int sourceID) {
        this.index = index;
        this.sourceID = sourceID;
        alListener3f(AL_POSITION, 0, 0, 0);
        int error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set listener relativePos: " + ALUtils.getErrorString(error));

        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to enable audio features: " + ALUtils.getErrorString(error));

        alListener3f(AL_VELOCITY, 0, 0, 0);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set listener velocity: " + ALUtils.getErrorString(error));

        alSource3f(sourceID, AL_POSITION, 0, 0, 0);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source relativePos: " + ALUtils.getErrorString(error));

        alSource3f(sourceID, AL_VELOCITY, 0, 0, 0);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source velocity: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_GAIN, 1.0f);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source gain: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_PITCH, 1.0f);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source pitch: " + ALUtils.getErrorString(error));

        try (MemoryStack stack = MemoryStack.stackPush()) {
            alListenerfv(AL_ORIENTATION, stack.floats(
                    0f, 0f, -1f, // forward
                    0f, 1f,  0f  // up
            ));
        }
        alSourcei(sourceID, AL_LOOPING, AL_FALSE);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source looping: " + ALUtils.getErrorString(error));

        alSourcei(sourceID, AL_BUFFER, 0);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source buffer: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_MAX_DISTANCE, 100);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source max distance: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_ROLLOFF_FACTOR, 1.0f);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source rolloff factor: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_REFERENCE_DISTANCE, 1.0f);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source reference distance: " + ALUtils.getErrorString(error));

        alSourcef(sourceID, AL_MIN_GAIN, 0.0f);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set source min gain: " + ALUtils.getErrorString(error));

        alDistanceModel(AL_LINEAR_DISTANCE);
        error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException("Failed to set distance model: " + ALUtils.getErrorString(error));
    }

    public SoundSource set(Sound sound) {
        alSourcei(sourceID, AL_BUFFER, sound.bufferID());
        return this;
    }

    public SoundSource setGain(float gain) {
        alSourcef(sourceID, AL_GAIN, gain);
        return this;
    }

    public SoundSource setPitch(float pitch) {
        alSourcef(sourceID, AL_PITCH, pitch);
        return this;
    }

    public SoundSource setPosition(float x, float y, float z) {
        alSource3f(sourceID, AL_POSITION, x, y, z);
        return this;
    }

    public SoundSource setVelocity(float x, float y, float z) {
        alSource3f(sourceID, AL_VELOCITY, x, y, z);
        return this;
    }

    public void play() {
        alSourcePlay(sourceID);
    }

    public void stop() {
        alSourceStop(sourceID);
    }

    public void pause() {
        alSourcePause(sourceID);
    }

    public void resume() {
        if (isPaused())
            alSourcePlay(sourceID);
        else throw new IllegalStateException("Source is not paused");
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public boolean isPaused() {
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PAUSED;
    }

    public boolean isStopped() {
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_STOPPED;
    }

    public void close() {
        alDeleteSources(sourceID);
    }

    public int getIndex() {
        return index;
    }

    public SoundSource setPosition(Vector3f position) {
        alSource3f(sourceID, AL_POSITION, position.x, position.y, position.z);
        return this;
    }

    public SoundSource setVelocity(Vector3f velocity) {
        alSource3f(sourceID, AL_VELOCITY, velocity.x, velocity.y, velocity.z);
        return this;
    }
}
