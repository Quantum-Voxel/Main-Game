package dev.ultreon.qvoxel.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.client.model.json.JsonModelLoader;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;

import java.io.IOException;

public class ModelLoader {
    private final ResourceManager resourceManager;
    private final JsonModelLoader jsonModelLoader;

    public ModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        jsonModelLoader = new JsonModelLoader(resourceManager);
    }

    public BlockModel load(Block block) throws IOException {
        Identifier id = block.getId();
        Identifier resourceId = new Identifier(id.location(), "models/blocks/" + id.path() + ".json");
        Resource resource = resourceManager.getResource(resourceId);
        if (resource == null) {
            return null;
        }
        JsonElement jsonElement = resource.loadJson();
        if (jsonElement.isJsonObject()) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if (asJsonObject.has("custom")) {
                String custom = asJsonObject.getAsJsonPrimitive("custom").getAsString();
                Identifier parse = Identifier.parse(custom);
                float scale = 1.0f;
                if (asJsonObject.has("scale")) {
                    JsonElement scaleElem = asJsonObject.get("scale");
                    if (scaleElem.isJsonPrimitive()) {
                        JsonPrimitive asJsonPrimitive = scaleElem.getAsJsonPrimitive();
                        if (asJsonPrimitive.isNumber()) {
                            scale = asJsonPrimitive.getAsFloat();
                        } else {
                            CommonConstants.LOGGER.error("Invalid custom model scale, not a number: {}", scaleElem);
                        }
                    } else {
                        CommonConstants.LOGGER.error("Invalid custom model scale, not a primitive: {}", scaleElem);
                    }
                }
                return new AssimpBlockModel(parse, scale);
            }
            return jsonModelLoader.load(Registries.BLOCK.getKey(block), asJsonObject);
        }

        throw new IOException("Invalid block json, expected object at root");
    }

    public ItemModel load(Item item) throws IOException {
        Identifier id = item.getId();
        Identifier resourceId = new Identifier(id.location(), "models/items/" + id.path() + ".json");
        Resource resource = resourceManager.getResource(resourceId);
        if (resource == null) {
            return null;
        }
        JsonElement jsonElement = resource.loadJson();
        if (jsonElement.isJsonObject()) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if (asJsonObject.has("custom")) {
                String custom = asJsonObject.getAsJsonPrimitive("custom").getAsString();
                Identifier parse = Identifier.parse(custom);
                return new AssimpItemModel(parse);
            }
            return jsonModelLoader.load(Registries.ITEM.getKey(item), asJsonObject);
        }

        throw new IOException("Invalid block json, expected object at root");
    }
}
