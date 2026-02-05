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

package dev.ultreon.qvoxel.event.system;

import java.util.Objects;

public final class PriortizedEventListener<T extends Event> implements EventListener<T> {
    private final int priority;

    public PriortizedEventListener(int priority) {
        this.priority = priority;
    }

    @Override
    public void call(T event) {

    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (PriortizedEventListener) obj;
        return priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }

    @Override
    public String toString() {
        return "PriortizedEventListener[" +
                "priority=" + priority + ']';
    }

}
