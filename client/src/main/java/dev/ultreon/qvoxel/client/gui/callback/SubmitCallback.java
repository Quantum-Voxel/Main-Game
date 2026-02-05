package dev.ultreon.qvoxel.client.gui.callback;

import dev.ultreon.qvoxel.client.gui.Widget;

@FunctionalInterface
public interface SubmitCallback<T extends Widget> {
    void call(T widget);
}
