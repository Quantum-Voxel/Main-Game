package dev.ultreon.qvoxel.client.debug;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.gui.Resizer;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.TimerTask;
import java.util.function.Consumer;

public abstract class FrameBufferRenderer<T> implements Renderer<T> {
    private final Resizer resizer = new Resizer();
    private Framebuffer framebuffer;
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Vector3f pos = new Vector3f(-4, -2, -16);
    protected final Vector3f rot = new Vector3f(0, 0, 0);
    protected Vector3f origin = new Vector3f(0, 0, 0);
    private final int width;
    private final int height;

    public FrameBufferRenderer() {
        int[] size = new int[2];
        getWidthAndHeight(size);
        width = size[0];
        height = size[1];
    }

    protected void getWidthAndHeight(int[] size) {
        size[0] = 1536;
        size[1] = 1536;
    }

    public abstract void offRender(T object, Matrix4f projection, Matrix4f view, Matrix4f model);

    @Override
    public void render(T object, @Nullable Consumer<T> setter) {

        if (framebuffer == null) {
            framebuffer = new Framebuffer(width, height, TextureFormat.RGBA8, true);
            CommonConstants.TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    QuantumClient.invoke(() -> {
                        if (framebuffer == null) return;
                        framebuffer.delete();
                        framebuffer = null;
                    });
                }
            }, 10000);
            QuantumClient.onClose(() -> {
                if (framebuffer == null) return;
                framebuffer.delete();
                framebuffer = null;
            });
        }

        framebuffer.start();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glViewport(0, 0, width, height);
        GL11.glDisable(GL11.GL_CULL_FACE);

        projection.identity();
        projection.setPerspective((float) Math.toRadians(70), 1f, 0.01f, 1000f);

        rot.x = 0.4f;
        rot.y = (float) org.joml.Math.toRadians(System.currentTimeMillis() / 100.0 % 360.0);

        view.identity();
        view.translate(pos);
        view.rotateX(org.joml.Math.toRadians(20));

        model.identity();
        model.scale(0.25f, 0.25f, 0.25f);
        float size = getSize();
        origin = getOrigin(origin);
        model.translate(origin.x, origin.y, origin.z);
        model.translate(size / 2f, size / 2f, size / 2f);
        model.rotateXYZ(rot);
        model.translate(-size / 2f, -size / 2f, -size / 2f);

        offRender(object, projection, view, model);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        framebuffer.end();

        resizer.set(width, height);
        Vector2f fit = resizer.fit(ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY());
        ImGui.image(framebuffer.get(0).getObjectId(), fit.x, fit.y, 0, 1, 1, 0);
        ImGuiEx.editVec3f("Origin", "origin", () -> origin, origin::set);
        ImGuiEx.editVec3f("Rotation", "rot", () -> rot, rot::set);
        ImGuiEx.editVec3f("Position", "pos", () -> pos, pos::set);
    }

    protected abstract Vector3f getOrigin(Vector3f vector3f);

    protected abstract float getSize();
}
