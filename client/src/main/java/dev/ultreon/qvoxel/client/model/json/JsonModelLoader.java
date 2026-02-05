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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.state.property.BlockDataEntry;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.BlockModel;
import dev.ultreon.qvoxel.item.BlockItem;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.util.Direction;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;

/**
 * The Json5ModelLoader class is responsible for loading and processing JSON5 formatted models
 * for blocks and items in a resource management context. This class provides methods to load
 * models and their associated data such as textures, elements, and overrides.
 * <p>
 * The class supports both block and item models, providing utility methods to map their attributes
 * from JSON5 files to internal data structures for rendering purposes.
 * <p>
 * It uses a ResourceManager for accessing model resources and registry keys to identify them.
 */
public class JsonModelLoader {
    private final ResourceManager resourceManager;

    /**
     * Constructs a new Json5ModelLoader instance, initializing it with the specified ResourceManager.
     *
     * @param resourceManager the resource manager to be used for loading resources
     */
    public JsonModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Default constructor for the Json5ModelLoader class.
     * Initializes the Json5ModelLoader using the default resource manager obtained
     * from the QuantumClient.
     */
    public JsonModelLoader() {
        this(QuantumClient.get().resourceManager);
    }

    /**
     * Loads a {@link JsonModel} associated with the given {@link Block}.
     * This method attempts to resolve the resource path for the block's JSON5 model file,
     * retrieves the resource, and parses it into a {@link JsonModel}.
     * If the resource is not found, it returns null.
     *
     * @param block the {@link Block} whose model is to be loaded
     * @return the loaded {@link JsonModel}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public JsonModel load(Block block) throws IOException {
        Identifier namespaceID = block.getId().mapPath(path -> "models/blocks/" + path + ".json");
        Resource resource = resourceManager.getResource(namespaceID);
        if (resource == null) {
            CommonConstants.LOGGER.warn("Block model not found: {}", namespaceID);
            return null;
        }
        CommonConstants.LOGGER.debug("Loading block model: {}", namespaceID);
        JsonModel model = load(Registries.BLOCK.getKey(block), CommonConstants.GSON.fromJson(resource.openReader(), JsonObject.class));
        model.setBlock(block.getDefaultState());
        return model;
    }

    /**
     * Loads a {@link JsonModel} associated with the specified {@link Item}.
     * This method attempts to resolve the resource path for the item's JSON5 model file,
     * retrieves the resource, and parses it into a {@link JsonModel}. If the resource
     * cannot be found, this method returns null.
     *
     * @param item the {@link Item} whose model is to be loaded
     * @return the loaded {@link JsonModel}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public JsonModel load(Item item) throws IOException {
        Identifier namespaceID = item.getId().mapPath(path -> "models/items/" + path + ".json");
        Resource resource = resourceManager.getResource(namespaceID);
        if (resource == null) {
            CommonConstants.LOGGER.warn("Item model not found: {}", namespaceID);
            return null;
        }
        CommonConstants.LOGGER.debug("Loading item model: {}", namespaceID);
        JsonModel model = load(Registries.ITEM.getKey(item), CommonConstants.GSON.fromJson(resource.openReader(), JsonObject.class));
        if (item instanceof BlockItem) {
            model.setBlock(((BlockItem) item).getBlock().getDefaultState());
        }
        return model;
    }

    /**
     * Loads a {@link JsonModel} based on the provided registry key and model data.
     * Validates the registry key to ensure it belongs to either blocks or items,
     * parses the textures, elements, display properties, and other model-related
     * configurations from the given JSON5 element, and constructs the model.
     *
     * @param key       the {@link RegistryKey} for which the model needs to be loaded;
     *                  must belong to either {@code RegistryKeys.BLOCK} or {@code RegistryKeys.ITEM}
     * @param modelData the JSON5 representation of the model data
     * @return the constructed {@link JsonModel} containing all the parsed and processed model data
     * @throws IllegalArgumentException if the provided registry key does not belong to blocks or items
     */
    @SuppressWarnings("SpellCheckingInspection")
    public JsonModel load(RegistryKey<?> key, JsonObject modelData) {
        if (!Objects.equals(key.parent(), RegistryKeys.BLOCK) && !Objects.equals(key.parent(), RegistryKeys.ITEM)) {
            throw new IllegalArgumentException("Invalid model key, must be block or item: " + key);
        }

        JsonElement textures = modelData.getAsJsonObject().get("textures");
        Map<String, Identifier> textureElements = loadTextures(textures);

//        Vector2i textureSize = loadVector2fi(root.get("texture_size"), new Vector2i(16, 16));
        Vector2i textureSize = new Vector2i(16, 16);

        JsonElement elements = modelData.get("elements");
        List<ModelElement> modelElements = loadElements(elements.getAsJsonArray(), textureSize.x, textureSize.y);

        JsonElement ambientocclusion = modelData.get("ambientocclusion");
        boolean ambientOcclusion = ambientocclusion == null || ambientocclusion.getAsBoolean();

        Table<String, BlockDataEntry<?>, JsonModel> overrides = null;

        JsonElement displayJson = modelData.get("display");
        if (displayJson == null)
            displayJson = new JsonObject();

        // TODO: Allow display properties.
        Display display = Display.read(displayJson.getAsJsonObject());

        return new JsonModel(key.id(), textureElements, modelElements, ambientOcclusion, display, overrides);
    }

    private Table<String, Object, JsonModel> loadOverrides(RegistryKey<Block> key, JsonObject overridesJson5) {
        Table<String, Object, JsonModel> overrides = new HashTable<>();
        Block block = Registries.BLOCK.get(key);
        BlockState meta = block.getDefaultState();

        for (Map.Entry<String, JsonElement> overrideElem : overridesJson5.entrySet()) {
            String keyName = overrideElem.getKey();

            JsonModel model = load(key, overrideElem.getValue().getAsJsonObject());
            Object entry1 = meta.get(block.getDefinition().keyByName(keyName));

            if (model == null)
                throw new IllegalArgumentException("Invalid model override: " + keyName);

            overrides.put(keyName, entry1, model);
        }

        return overrides;
    }

    private List<ModelElement> loadElements(JsonArray elements, int textureWidth, int textureHeight) {
        List<ModelElement> modelElements = new ArrayList<>();

        for (JsonElement elemJson : elements) {
            JsonObject elem = elemJson.getAsJsonObject();
            JsonElement faces = elem.get("faces");
            Map<Direction, FaceElement> blockFaceFaceElementMap = loadFaces(faces.getAsJsonObject(), textureWidth, textureHeight);

            JsonElement shade1 = elem.get("shade");
            boolean shade = shade1 != null && shade1.getAsBoolean();
            JsonElement rotation1 = elem.get("rotation");
            ElementRotation rotation = ElementRotation.deserialize(rotation1 == null || rotation1.isJsonNull() ? null : rotation1.getAsJsonObject());

            Vector3f from = loadVector3f(elem.get("from"));
            Vector3f to = loadVector3f(elem.get("to"));

            ModelElement modelElement = new ModelElement(blockFaceFaceElementMap, shade, rotation, from, to);
            modelElements.add(modelElement);
        }

        return modelElements;
    }

    private Vector3f loadVector3f(JsonElement from) {
        if (from == null)
            return new Vector3f();

        if (!from.isJsonArray())
            throw new IllegalArgumentException("Invalid from value: " + from);

        if (from.getAsJsonArray().size() != 3)
            throw new IllegalArgumentException("Invalid from value: " + from);

        JsonArray fromArray = from.getAsJsonArray();
        return new Vector3f(fromArray.get(0).getAsFloat(), fromArray.get(1).getAsFloat(), fromArray.get(2).getAsFloat());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private Map<Direction, FaceElement> loadFaces(JsonObject faces, int textureWidth, int textureHeight) {
        Map<Direction, FaceElement> faceElems = new HashMap<>();
        for (Map.Entry<String, JsonElement> faceDataJson : faces.entrySet()) {
            JsonObject faceData = faceDataJson.getValue().getAsJsonObject();
            Direction direction = Direction.valueOf(faceDataJson.getKey().toUpperCase(Locale.ROOT));
            JsonArray uvs = faceData.getAsJsonArray("uv");
            String texture = faceData.get("texture").getAsString();
            JsonPrimitive rotation1 = faceData.getAsJsonPrimitive("rotation");
            int rotation = rotation1 == null ? 0 : rotation1.getAsInt();
            JsonPrimitive tintIndex1 = faceData.getAsJsonPrimitive("tintindex");
            int tintIndex = tintIndex1 == null ? -1 : tintIndex1.getAsInt();
            JsonPrimitive cullface = faceData.getAsJsonPrimitive("cullface");
            String cullFace = cullface == null ? null : cullface.getAsString();

            faceElems.put(direction, new FaceElement(texture, new UVs(uvs.get(0).getAsInt(), uvs.get(1).getAsInt(), uvs.get(2).getAsInt(), uvs.get(3).getAsInt(), textureWidth, textureHeight), rotation, tintIndex, cullFace));
        }

        return faceElems;
    }

    private Map<String, Identifier> loadTextures(JsonElement textures) {
        Map<String, Identifier> textureElements = new HashMap<>();

        for (var entry : textures.getAsJsonObject().entrySet()) {
            String name = entry.getKey();
            String stringId = entry.getValue().getAsString();
            Identifier id = Identifier.parse(stringId);
            textureElements.put(name, id);
        }

        return textureElements;
    }

    public BlockModel load(RegistryKey<?> key, Identifier id) {
        Resource resource = resourceManager.getResource(id.mapPath(path -> "models/" + path + ".json"));
        if (resource != null) {
            return load(key, resource.loadJson().getAsJsonObject());
        }

        return null;
    }
}
