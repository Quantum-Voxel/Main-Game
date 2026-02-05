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

package dev.ultreon.qvoxel.resource;

import com.google.gson.JsonElement;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StaticResource implements Resource {
    private final Identifier id;
    protected ThrowingSupplier<InputStream, IOException> opener;

    public StaticResource(Identifier id, ThrowingSupplier<InputStream, IOException> opener) {
        this.id = id;
        this.opener = opener;
    }

    public InputStream openStream() throws IOException {
        return Objects.requireNonNull(opener.get());
    }

    public Identifier id() {
        return id;
    }
}
