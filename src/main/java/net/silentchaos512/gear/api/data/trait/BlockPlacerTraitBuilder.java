package net.silentchaos512.gear.api.data.trait;

import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.gear.api.ApiConst;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.util.DataResource;
import net.silentchaos512.lib.util.NameUtils;

import java.util.Objects;

public class BlockPlacerTraitBuilder extends TraitBuilder {
    private final Block block;
    private final int damageOnUse;
    private int cooldown;
    private SoundEvent sound;
    private float soundVolume = 1f;
    private float soundPitch = 1f;

    public BlockPlacerTraitBuilder(DataResource<ITrait> trait, int maxLevel, Block block, int damageOnUse) {
        this(trait.getId(), maxLevel, block, damageOnUse);
    }

    public BlockPlacerTraitBuilder(ResourceLocation traitId, int maxLevel, Block block, int damageOnUse) {
        super(traitId, maxLevel, ApiConst.BLOCK_PLACER_TRAIT_ID);
        this.block = block;
        this.damageOnUse = damageOnUse;
        this.sound = this.block.defaultBlockState().getSoundType().getPlaceSound();
    }

    public BlockPlacerTraitBuilder cooldown(int timeInTicks) {
        this.cooldown = timeInTicks;
        return this;
    }

    public BlockPlacerTraitBuilder sound(SoundEvent sound, float volume, float pitch) {
        this.sound = sound;
        this.soundVolume = volume;
        this.soundPitch = pitch;
        return this;
    }

    public BlockPlacerTraitBuilder sound(float volume, float pitch) {
        // Retains default sound
        this.soundVolume = volume;
        this.soundPitch = pitch;
        return this;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();

        json.addProperty("block", NameUtils.fromBlock(this.block).toString());
        json.addProperty("damage_on_use", this.damageOnUse);
        json.addProperty("cooldown", this.cooldown);
        json.addProperty("sound", Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getKey(this.sound)).toString());
        json.addProperty("sound_volume", this.soundVolume);
        json.addProperty("sound_pitch", this.soundPitch);

        return json;
    }
}
