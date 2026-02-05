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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.client.QuantumClient;
import org.joml.FrustumIntersection;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@SuppressWarnings("UnusedReturnValue")
public class Camera {
    public final Vector3f up = new Vector3f(0, 1, 0);
    public final Vector3f position;
    private final Vector3f direction;
    public float yaw;
    public float pitch;
    public float roll;
    public float fov;
    public float viewportWidth;
    public float viewportHeight;
    public float nearPlane;
    public float farPlane;
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projViewMatrix = new Matrix4f();

    private final Vector3f lastUp = new Vector3f();
    private final Vector3f lastPosition = new Vector3f();
    private final Vector3f lastDirection = new Vector3f();
    private float lastRoll;
    private float lastFov;
    private float lastViewportWidth;
    private float lastViewportHeight;
    private float lastNearPlane;
    private float lastFarPlane;

    public final FrustumIntersection frustum = new FrustumIntersection();

    public Camera() {
        position = new Vector3f();
        direction = new Vector3f(0, 0, -1);
        fov = 70;
        viewportWidth = QuantumClient.get().getWindow().getWidth();
        viewportHeight = QuantumClient.get().getWindow().getHeight();
        nearPlane = 0.01f;
        farPlane = 1000.0f;

        updateProjectionMatrix();
        updateViewMatrix();
    }

    public Camera(float viewportWidth, float viewportHeight) {
        position = new Vector3f();
        direction = new Vector3f(0, 0, -1);
        fov = 70;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        nearPlane = 0.01f;
        farPlane = 1000.0f;

        updateProjectionMatrix();
        updateViewMatrix();
    }

    public Camera(Vector3f position, Vector3f direction, float fov, float viewportWidth, float viewportHeight, float nearPlane, float farPlane) {
        this.position = position;
        this.direction = direction;
        this.fov = fov;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        updateProjectionMatrix();
        updateViewMatrix();
    }

    public Camera(Camera camera) {
        position = new Vector3f(camera.position);
        direction = new Vector3f(camera.direction);
        fov = camera.fov;
        viewportWidth = camera.viewportWidth;
        viewportHeight = camera.viewportHeight;
        nearPlane = camera.nearPlane;
        farPlane = camera.farPlane;

        updateProjectionMatrix();
        updateViewMatrix();
        frustum.set(projViewMatrix);
    }

    public Camera(Vector3f position, Vector3f direction) {
        this.position = position;
        this.direction = direction;
        fov = 70;
        viewportWidth = QuantumClient.get().getWindow().getWidth();
        viewportHeight = QuantumClient.get().getWindow().getHeight();
        nearPlane = 0.1f;
        farPlane = 1000.0f;

        updateProjectionMatrix();
        updateViewMatrix();
        updateFrustum();
    }

    /**
     * Updates the frustum from the combined projection * view matrix.
     */
    public void updateFrustum() {
        projViewMatrix.set(projectionMatrix).mul(viewMatrix);
        frustum.set(projViewMatrix);
    }

    public Camera resize(float width, float height) {
        System.out.println("width = " + width + ", height = " + height);

        viewportWidth = width;
        viewportHeight = height;
        updateProjectionMatrix();
        return this;
    }

    public void updateProjectionMatrix() {
        if (viewportWidth <= 0 || viewportHeight <= 0)
            throw new CameraException("Cannot update projection matrix with invalid viewport dimensions");

        float aspectRatio = viewportWidth / viewportHeight;

        projectionMatrix.identity();
        projectionMatrix.setPerspective(Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
    }

    private void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.rotateZ(Math.toRadians(roll));
        viewMatrix.lookAlong(direction, up);
        viewMatrix.translate(-position.x, -position.y, -position.z);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getUp() {
        return up;
    }

    public Camera setRotation(float yaw, float pitch) {
        direction.set(0, 0, -1);
        direction.rotateX(Math.toRadians(Math.clamp(-89.9f, 89.9f, -pitch)));
        direction.rotateY(Math.toRadians(-yaw));
        this.yaw = yaw;
        this.pitch = pitch;
        updateViewMatrix();
        updateFrustum();
        return this;
    }

    public float getFov() {
        return fov;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setDirection(Vector3f direction) {
        this.direction.set(direction);
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void update() {
        if (!hasMoved())
            return;

        updateProjectionMatrix();
        updateViewMatrix();
    }

    public void finish() {
        lastUp.set(up);
        lastPosition.set(position);
        lastDirection.set(direction);
        lastRoll = roll;
        lastFov = fov;
        lastViewportWidth = viewportWidth;
        lastViewportHeight = viewportHeight;
        lastNearPlane = nearPlane;
        lastFarPlane = farPlane;
    }

    public boolean hasMoved() {
        return !lastPosition.equals(position) || !lastDirection.equals(direction) || lastRoll != roll || lastFov != fov || lastViewportWidth != viewportWidth || lastViewportHeight != viewportHeight || lastNearPlane != nearPlane || lastFarPlane != farPlane || !lastUp.equals(up);
    }
}
