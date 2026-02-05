package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class ClassRenderer implements Renderer<Class<?>> {

    @Override
    public void render(Class<?> object, @Nullable Consumer<Class<?>> setter) {
        for (Field field1 : object.getDeclaredFields()) {
            if (!Modifier.isStatic(field1.getModifiers()) || field1.isSynthetic()) continue;
            if (field1.getType().equals(object)) {
                Runnable runnable = ImGuiOverlay.renderObject(null, field1, Modifier.isFinal(field1.getModifiers()));
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }
}
