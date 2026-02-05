package dev.ultreon.qvoxel.client.debug;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// A generic interface for rendering objects of a specific type.
///
/// Implementations of this interface are responsible for rendering
/// the specified object and may optionally perform an action on
/// the object after rendering. The object is rendered using
/// [ImGui], [ImGuiEx] or other related objects
///
/// @param <T> the type of objects this renderer works with
/// @see ImGuiOverlay
public interface Renderer<T> {
    /// Renders the given object and optionally allows a setter action to be performed.
    ///
    /// @param object the object to be rendered
    /// @param setter an optional consumer that can perform an action on the object after rendering, may be null
    void render(T object, @Nullable Consumer<T> setter);
}
