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

package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.util.Direction;

import java.util.Objects;

public class ModelProperties {
    public FaceProperties top;
    public FaceProperties bottom;
    public FaceProperties left;
    public FaceProperties right;
    public FaceProperties front;
    public FaceProperties back;
    public Direction rotation;
    public String renderPass = "opaque";

    public ModelProperties() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelProperties that = (ModelProperties) o;
        return Objects.equals(top, that.top) && Objects.equals(bottom, that.bottom) && Objects.equals(left, that.left) && Objects.equals(right, that.right) && Objects.equals(front, that.front) && Objects.equals(back, that.back) && Objects.equals(rotation, that.rotation);
    }

    @Override
    public int hashCode() {
        int result = top.hashCode();
        result = 31 * result + bottom.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        result = 31 * result + front.hashCode();
        result = 31 * result + back.hashCode();
        result = 31 * result + rotation.hashCode();
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FaceProperties top = new FaceProperties();
        private FaceProperties bottom = new FaceProperties();
        private FaceProperties left = new FaceProperties();
        private FaceProperties right = new FaceProperties();
        private FaceProperties front = new FaceProperties();
        private FaceProperties back = new FaceProperties();
        private Direction horizontalRotation = Direction.NORTH;
        private String renderPass = "opaque";

        public Builder top(FaceProperties top) {
            this.top = top;
            return this;
        }

        public Builder bottom(FaceProperties bottom) {
            this.bottom = bottom;
            return this;
        }

        public Builder left(FaceProperties left) {
            this.left = left;
            return this;
        }

        public Builder right(FaceProperties right) {
            this.right = right;
            return this;
        }

        public Builder front(FaceProperties front) {
            this.front = front;
            return this;
        }

        public Builder back(FaceProperties back) {
            this.back = back;
            return this;
        }

        public Builder renderPass(String renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        public ModelProperties build() {
            ModelProperties modelProperties = new ModelProperties();
            modelProperties.top = top;
            modelProperties.bottom = bottom;
            modelProperties.left = left;
            modelProperties.right = right;
            modelProperties.front = front;
            modelProperties.back = back;
            modelProperties.rotation = horizontalRotation;
            modelProperties.renderPass = renderPass;
            return modelProperties;
        }

        public Builder rotateHorizontal(Direction direction) {
            horizontalRotation = direction;
            return this;
        }
    }
}
