package net.silentchaos512.gear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.fml.DistExecutor;
import net.silentchaos512.gear.network.NetworkHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.silentchaos512.gear.util.ModResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

@Mod(SilentGear.MOD_ID)
public final class SilentGear {
    public static final String MOD_ID = "silentgear";
    public static final String MOD_NAME = "Silent Gear";

    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    public static final Random RANDOM = new Random();
    public static final RandomSource RANDOM_SOURCE = RandomSource.create();
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static SilentGear INSTANCE;
    public static IProxy PROXY;

    public SilentGear() {
        INSTANCE = this;
        NetworkHandler.init();
        PROXY = DistExecutor.unsafeRunForDist(() -> SideProxy.Client::new, () -> SideProxy.Server::new);
    }

    public static String getVersion() {
        Optional<? extends ModContainer> o = ModList.get().getModContainerById(MOD_ID);
        if (o.isPresent()) {
            return o.get().getModInfo().getVersion().toString();
        }
        return "0.0.0";
    }

    @Deprecated
    public static String getVersion(boolean correctInDev) {
        return getVersion();
    }

    public static boolean isDevBuild() {
        return "NONE".equals(getVersion()) || !FMLLoader.isProduction();
    }

    public static ModResourceLocation getId(String path) {
        if (path.contains(":")) {
            throw new IllegalArgumentException("path contains namespace");
        }
        return new ModResourceLocation(path);
    }

    @Nullable
    public static ResourceLocation getIdWithDefaultNamespace(String name) {
        if (name.contains(":"))
            return ResourceLocation.tryParse(name);
        return ResourceLocation.tryParse(RESOURCE_PREFIX + name);
    }

    public static String shortenId(@Nullable ResourceLocation id) {
        if (id == null)
            return "null";
        if (MOD_ID.equals(id.getNamespace()))
            return id.getPath();
        return id.toString();
    }
}
