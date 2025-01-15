package net.silentchaos512.gear.gear.part;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.client.model.PartTextures;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum PartTextureSet {
    ABSENT(ImmutableMap.of()),
    LOW_CONTRAST(ImmutableMap.<PartType, List<PartTextures>>builder()
            .put(PartType.MAIN, ImmutableList.of(PartTextures.MAIN_GENERIC_LC))
            .put(PartType.ROD, ImmutableList.of(PartTextures.ROD_GENERIC_LC))
            .put(PartType.TIP, ImmutableList.of(PartTextures.TIP_SHARP))
            .put(PartType.BINDING, ImmutableList.of(PartTextures.BINDING_GENERIC))
            .put(PartType.GRIP, ImmutableList.of(PartTextures.GRIP_WOOL))
            .put(PartType.LINING, ImmutableList.of(PartTextures.LINING_CLOTH))
            .put(PartType.CORD, ImmutableList.of(PartTextures.BOWSTRING_STRING))
            .put(PartType.FLETCHING, ImmutableList.of(PartTextures.FLETCHING_GENERIC))
            .put(PartType.ADORNMENT, ImmutableList.of(PartTextures.ADORNMENT_GENERIC))
            .build()),
    HIGH_CONTRAST(ImmutableMap.<PartType, List<PartTextures>>builder()
            .put(PartType.MAIN, ImmutableList.of(PartTextures.MAIN_GENERIC_HC))
            .put(PartType.ROD, ImmutableList.of(PartTextures.ROD_GENERIC_HC))
            .put(PartType.TIP, ImmutableList.of(PartTextures.TIP_SHARP))
            .put(PartType.BINDING, ImmutableList.of(PartTextures.BINDING_GENERIC))
            .put(PartType.GRIP, ImmutableList.of(PartTextures.GRIP_WOOL))
            .put(PartType.LINING, ImmutableList.of(PartTextures.LINING_CLOTH))
            .put(PartType.CORD, ImmutableList.of(PartTextures.BOWSTRING_STRING))
            .put(PartType.FLETCHING, ImmutableList.of(PartTextures.FLETCHING_GENERIC))
            .put(PartType.ADORNMENT, ImmutableList.of(PartTextures.ADORNMENT_GENERIC))
            .build()),
    HIGH_CONTRAST_WITH_HIGHLIGHT(ImmutableMap.<PartType, List<PartTextures>>builder()
            .put(PartType.MAIN, ImmutableList.of(PartTextures.MAIN_GENERIC_HC, PartTextures.HIGHLIGHT))
            .put(PartType.ROD, ImmutableList.of(PartTextures.ROD_GENERIC_HC))
            .put(PartType.TIP, ImmutableList.of(PartTextures.TIP_SHARP))
            .put(PartType.BINDING, ImmutableList.of(PartTextures.BINDING_GENERIC))
            .put(PartType.GRIP, ImmutableList.of(PartTextures.GRIP_WOOL))
            .put(PartType.LINING, ImmutableList.of(PartTextures.LINING_SLIME, PartTextures.LINING_SLIME_HIGHLIGHT))
            .put(PartType.CORD, ImmutableList.of(PartTextures.BOWSTRING_STRING))
            .put(PartType.FLETCHING, ImmutableList.of(PartTextures.FLETCHING_GENERIC))
            .put(PartType.ADORNMENT, ImmutableList.of(PartTextures.ADORNMENT_GENERIC, PartTextures.ADORNMENT_HIGHLIGHT))
            .build());

    final Map<PartType, List<PartTextures>> layers;

    PartTextureSet(Map<PartType, List<PartTextures>> layers) {
        this.layers = layers;
    }

    public int getIndex() {
        return ordinal();
    }

    public List<PartTextures> getLayers(PartType partType) {
        return layers.getOrDefault(partType, Collections.singletonList(partType.getDefaultTexture()));
    }
}
