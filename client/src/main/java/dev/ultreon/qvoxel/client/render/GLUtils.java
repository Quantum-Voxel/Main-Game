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

package dev.ultreon.qvoxel.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

public class GLUtils {

    public static String getErrorName(int error) {
        return switch (error) {
            case GL11.GL_NO_ERROR -> "No error";
            case GL11.GL_INVALID_ENUM -> "Invalid enum";
            case GL11.GL_INVALID_VALUE -> "Invalid value";
            case GL11.GL_INVALID_OPERATION -> "Invalid operation";
            case GL11.GL_STACK_OVERFLOW -> "Stack overflow";
            case GL11.GL_STACK_UNDERFLOW -> "Stack underflow";
            case GL11.GL_OUT_OF_MEMORY -> "Out of memory";
            case GL30.GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid framebuffer operation";
            case GL45.GL_CONTEXT_LOST -> "Context lost";
            default -> "Unknown error";
        };
    }
}
