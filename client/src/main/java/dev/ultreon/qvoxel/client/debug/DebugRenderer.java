package dev.ultreon.qvoxel.client.debug;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to specify a custom renderer for a particular type.
 * This annotation is used to associate a specific implementation of
 * the {@code Renderer} interface with a type for
 * debugging or visualizing objects in custom rendering contexts.
 * The specified renderer will provide the logic for rendering the
 * associated type.
 * <p>
 * Example usage:
 * <pre>{@code
 * @DebugRenderer(ColorChooserWidget.Renderer.class)
 * public class ColorChooserWidget extends ContainerWidget {
 *     // Widget implementation
 *
 *     public static class Renderer implements DebugRenderer<ColorChooserWidget> {
 *         @Override
 *         public void render(Framebuffer object, @Nullable Consumer<Framebuffer> setter) {
 *             ImGui.colorPicker4("Color Picker", new float[]{1.0f, 0.0f, 0.0f, 1.0f});
 *         }
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DebugRenderer {
    /**
     * Returns the class of a custom renderer to be used for rendering a specific type.
     * The returned class must implement the {@link Renderer} interface and will be
     * used to provide the logic for rendering objects of the associated type.
     *
     * @return the class of the custom renderer that extends {@link Renderer}.
     */
    Class<? extends Renderer<?>> value();
}
