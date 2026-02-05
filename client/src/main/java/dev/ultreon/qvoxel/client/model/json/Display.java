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

package dev.ultreon.qvoxel.client.model.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class Display {
    public String renderPass;

    public Display(String renderPass) {
        this.renderPass = renderPass;
    }

    public static Display read(JsonObject display) {
        JsonElement renderPassJson = display.get("renderPass");
        String renderPass = renderPassJson != null ? renderPassJson.getAsString() : "opaque";
        return new Display(renderPass);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "Display[]";
    }


}
