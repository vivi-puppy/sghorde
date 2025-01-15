package net.silentchaos512.gear.client.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.api.item.GearType;
import net.silentchaos512.gear.api.material.IMaterialDisplay;
import net.silentchaos512.gear.api.material.IMaterialInstance;
import net.silentchaos512.gear.api.material.IMaterialLayerList;
import net.silentchaos512.gear.api.material.MaterialLayerList;
import net.silentchaos512.gear.api.part.IPartData;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.util.PartGearKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialDisplay implements IMaterialDisplay {
    protected final Map<PartGearKey, MaterialLayerList> map = new LinkedHashMap<>();
    private final ResourceLocation id;

    public static MaterialDisplay of(ResourceLocation id, Map<PartGearKey, MaterialLayerList> display) {
        MaterialDisplay model = new MaterialDisplay(id);
        model.map.putAll(display);
        return model;
    }

    MaterialDisplay(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getMaterialId() {
        return id;
    }

    @Override
    public IMaterialLayerList getLayerList(GearType gearType, IPartData part, IMaterialInstance materialIn) {
        return map.getOrDefault(getMostSpecificKey(gearType, part.getType()), MaterialLayerList.DEFAULT);
    }

    private PartGearKey getMostSpecificKey(GearType gearType, PartType partType) {
        PartGearKey key = PartGearKey.of(gearType, partType);
        if (map.containsKey(key)) {
            return key;
        }

        PartGearKey parent = key.getParent();
        while (parent != null) {
            if (map.containsKey(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }

        // No match
        return key;
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        this.map.forEach((key, layerList) -> json.add(key.toString(), layerList.serialize()));
        return json;
    }

    public static MaterialDisplay deserialize(ResourceLocation modelId, JsonObject json) {
        MaterialDisplay ret = new MaterialDisplay(modelId);
        json.entrySet().forEach(entry -> {
            PartGearKey key = PartGearKey.read(entry.getKey());
            JsonElement value = entry.getValue();
            ret.map.put(key, MaterialLayerList.deserialize(key, value, MaterialLayerList.DEFAULT));
        });
        return ret;
    }

    public static MaterialDisplay fromNetwork(ResourceLocation materialId, FriendlyByteBuf buf) {
        Map<PartGearKey, MaterialLayerList> map = new LinkedHashMap<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; ++i) {
            PartGearKey key = PartGearKey.fromNetwork(buf);
            MaterialLayerList layerList = MaterialLayerList.read(buf);
            map.put(key, layerList);
        }
        return of(materialId, map);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(map.size());
        map.forEach((key, list) -> {
            key.toNetwork(buf);
            list.write(buf);
        });
    }
}
