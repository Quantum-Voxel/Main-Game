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

package dev.ultreon.qvoxel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.ultreon.libs.commons.v0.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.Random;
import java.util.Timer;

public class CommonConstants {
    public static final String NAMESPACE = "quantum";
    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumVoxel");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Cleaner CLEANER = Cleaner.create();
    public static final Timer TIMER = new Timer();
    public static final long KEEP_ALIVE_INTERVAL = 10000;
    public static final float MIN_LIGHT = 0.1f;
    public static final Random RANDOM = new Random();

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
