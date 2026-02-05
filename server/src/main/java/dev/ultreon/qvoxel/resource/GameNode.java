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

import dev.ultreon.qvoxel.debug.HiddenNode;
import dev.ultreon.qvoxel.debug.HideInNodeView;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.*;

/**
 * Represents an abstract game node in a scene graph. A game node can have child nodes,
 * a parent node, and components that describe its behavior or functionality. This class
 * serves as the base for creating hierarchical structures for game objects.
 */
public abstract class GameNode {
    @HiddenNode
    protected GameNode parent;
    private String name;

    @HideInNodeView
    private List<GameNode> children;
    @HideInNodeView
    private Map<Class<? extends GameComponent>, GameComponent> components = new HashMap<>();

    /**
     * Adds a game node as a child to the current game node. The added node will have its
     * parent set to the current node and its name updated to the specified value.
     *
     * @param name the name to assign to the added game node
     * @param node the game node to be added as a child
     * @throws NullPointerException if the provided game node is null
     */
    public void add(@NotNull String name, GameNode node) {
        if (node == null) throw new NullPointerException("Added game node cannot be null");

        if (children == null) children = new ArrayList<>();
        children.add(node);
        node.parent = this;
        node.name = name;
    }

    public GameNode getParent() {
        return parent;
    }

    /**
     * Removes the specified child node from the current game node. If the node is successfully removed,
     * its parent reference is set to null.
     *
     * @param node the child node to be removed from the current game node
     */
    public void remove(GameNode node) {
        if (children == null) return;
        children.remove(node);
        node.parent = null;
    }

    /**
     * Updates the current game node and its child nodes recursively. This method propagates
     * the update call to all child nodes, allowing them to process changes or behaviors
     * relative to the given origin.
     *
     * @param origin the point of origin or reference to be used during the update operation
     */
    public void update(Vector3d origin) {
        if (children == null) return;
        for (GameNode child : children) {
            child.update(origin);
        }
    }

    public Iterable<GameNode> getChildren() {
        if (children == null) return Collections.emptyList();
        return children;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return "";
    }

    public Collection<GameComponent> getComponents() {
        return List.of();
    }

    public <T extends GameComponent> T getComponent(Class<T> type) {
        if (!components.containsKey(type))
            return null;
        return type.cast(components.get(type));
    }

    /**
     * Adds the specified game component to the current game node.
     * If a component of the same type is already registered, an {@link IllegalArgumentException}
     * will be thrown.
     *
     * @param component the game component to be added to the node
     * @throws IllegalArgumentException if a component of the same type is already registered
     */
    public void addComponent(GameComponent component) {
        if (components.containsKey(component.getClass()))
            throw new IllegalArgumentException("Component already registered: " + component.getClass());
        components.put(component.getClass(), component);
    }

    /**
     * Removes the specified game component from the current game node. If the component
     * is not registered within the node, an {@link IllegalArgumentException} is thrown.
     *
     * @param component the game component to be removed from the node
     * @throws IllegalArgumentException if the specified component is not registered
     */
    public void removeComponent(GameComponent component) {
        if (!components.containsKey(component.getClass()))
            throw new IllegalArgumentException("Component not registered: " + component.getClass());
        components.remove(component.getClass());
    }
}
