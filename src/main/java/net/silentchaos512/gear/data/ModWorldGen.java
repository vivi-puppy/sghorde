package net.silentchaos512.gear.data;

public class ModWorldGen {// FIXME
    /*public static void init(GatherDataEvent event) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        ResourceLocation bortName = SilentGear.getId("bort_ore");
        ResourceLocation crimsonIronName = SilentGear.getId("crimson_iron_ore");
        ResourceLocation azureSilverName = SilentGear.getId("azure_silver_ore");
        ResourceLocation wildFlaxName = SilentGear.getId("wild_flax");
        ResourceLocation wildFluffyName = SilentGear.getId("wild_fluffy");

        // Bort
        ConfiguredFeature<?, ?> bortFeature = new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(
                        Lists.newArrayList(
                                OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, SgBlocks.BORT_ORE.asBlockState()),
                                OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, SgBlocks.DEEPSLATE_BORT_ORE.asBlockState())
                        ),
                        3
                )
        );
        PlacedFeature bortPlaced = new PlacedFeature(
                holder(bortFeature, ops, bortName),
                commonOrePlacement(40, HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-60), VerticalAnchor.absolute(10)
                        )
                )
        );

        // Crimson Iron
        ConfiguredFeature<?, ?> crimsonIronFeature = new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(
                        Lists.newArrayList(
                                OreConfiguration.target(new TagMatchTest(Tags.Blocks.NETHERRACK), SgBlocks.CRIMSON_IRON_ORE.asBlockState()),
                                OreConfiguration.target(new BlockMatchTest(Blocks.BLACKSTONE), SgBlocks.BLACKSTONE_CRIMSON_IRON_ORE.asBlockState())
                        ),
                        8,
                        0
                )
        );
        PlacedFeature crimsonIronPlaced = new PlacedFeature(
                holder(crimsonIronFeature, ops, crimsonIronName),
                commonOrePlacement(14, PlacementUtils.RANGE_10_10)
        );

        // Azure Silver
        ConfiguredFeature<?, ?> azureSilverFeature = new ConfiguredFeature<>(Feature.ORE,
                new OreConfiguration(
                        new TagMatchTest(Tags.Blocks.END_STONES),
                        SgBlocks.AZURE_SILVER_ORE.asBlockState(),
                        6,
                        0
                )
        );
        PlacedFeature azureSilverPlaced = new PlacedFeature(
                holder(azureSilverFeature, ops, azureSilverName),
                commonOrePlacement(8, HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(16), VerticalAnchor.absolute(92)
                        )
                )
        );

        // Wild Flax
        ConfiguredFeature<?, ?> wildFlaxFeature = new ConfiguredFeature<>(SgWorldFeatures.WILD_PLANT,
                FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(SgBlocks.WILD_FLAX_PLANT.get())),
                        Lists.newArrayList(),
                        32
                )
        );
        PlacedFeature wildFlaxPlaced = new PlacedFeature(
                holder(wildFlaxFeature, ops, wildFlaxName),
                Lists.newArrayList(RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
        );

        // Wild Fluffy
        ConfiguredFeature<?, ?> wildFluffyFeature = new ConfiguredFeature<>(SgWorldFeatures.WILD_PLANT,
                FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(SgBlocks.WILD_FLUFFY_PLANT.get())),
                        Lists.newArrayList(),
                        32
                )
        );
        PlacedFeature wildFluffyPlaced = new PlacedFeature(
                holder(wildFluffyFeature, ops, wildFluffyName),
                Lists.newArrayList(RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
        );

        // Collections of all configured features and placed features
        Map<ResourceLocation, ConfiguredFeature<?, ?>> oreFeatures = ImmutableMap.of(
                bortName, bortFeature,
                crimsonIronName, crimsonIronFeature,
                azureSilverName, azureSilverFeature,
                wildFlaxName, wildFlaxFeature,
                wildFluffyName, wildFluffyFeature
        );
        Map<ResourceLocation, PlacedFeature> orePlacedFeatures = ImmutableMap.of(
                bortName, bortPlaced,
                crimsonIronName, crimsonIronPlaced,
                azureSilverName, azureSilverPlaced,
                wildFlaxName, wildFlaxPlaced,
                wildFluffyName, wildFluffyPlaced
        );

        HolderSet<Biome> overworldBiomes = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_OVERWORLD);
        HolderSet<Biome> netherBiomes = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_NETHER);
        HolderSet<Biome> endBiomes = new HolderSet.Named<>(ops.registry(Registry.BIOME_REGISTRY).get(), BiomeTags.IS_END);

        // Biome modifiers
        BiomeModifier overworldOres = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(
                        holderPlaced(bortPlaced, ops, bortName),
                        holderPlaced(wildFlaxPlaced, ops, wildFlaxName),
                        holderPlaced(wildFluffyPlaced, ops, wildFluffyName)
                ),
                GenerationStep.Decoration.UNDERGROUND_ORES
        );
        BiomeModifier netherOres = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                netherBiomes,
                HolderSet.direct(
                        holderPlaced(crimsonIronPlaced, ops, crimsonIronName)
                ),
                GenerationStep.Decoration.UNDERGROUND_ORES
        );
        BiomeModifier endOres = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                endBiomes,
                HolderSet.direct(
                        holderPlaced(azureSilverPlaced, ops, azureSilverName)
                ),
                GenerationStep.Decoration.UNDERGROUND_ORES
        );

        DataProvider configuredFeatureProvider = JsonCodecProvider.forDatapackRegistry(generator, existingFileHelper, SilentGear.MOD_ID, ops, Registry.CONFIGURED_FEATURE_REGISTRY,
                oreFeatures);
        DataProvider placedFeatureProvider = JsonCodecProvider.forDatapackRegistry(generator, existingFileHelper, SilentGear.MOD_ID, ops, Registry.PLACED_FEATURE_REGISTRY,
                orePlacedFeatures);
        DataProvider biomeModifierProvider = JsonCodecProvider.forDatapackRegistry(generator, existingFileHelper, SilentGear.MOD_ID, ops, ForgeRegistries.Keys.BIOME_MODIFIERS,
                ImmutableMap.of(
                        SilentGear.getId("overworld_ores"), overworldOres,
                        SilentGear.getId("nether_ores"), netherOres,
                        SilentGear.getId("end_ores"), endOres
                )
        );

        generator.addProvider(true, configuredFeatureProvider);
        generator.addProvider(true, placedFeatureProvider);
        generator.addProvider(true, biomeModifierProvider);
    }

    public static Holder<ConfiguredFeature<?, ?>> holder(ConfiguredFeature<?, ?> feature, RegistryOps<JsonElement> ops, ResourceLocation location) {
        return ops.registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, location));
    }

    public static Holder<PlacedFeature> holderPlaced(PlacedFeature feature, RegistryOps<JsonElement> ops, ResourceLocation location) {
        return ops.registryAccess.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY).getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, location));
    }

    private static List<PlacementModifier> orePlacement(PlacementModifier p_195347_, PlacementModifier p_195348_) {
        return Lists.newArrayList(p_195347_, InSquarePlacement.spread(), p_195348_, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier modifier) {
        return orePlacement(CountPlacement.of(count), modifier);
    }*/
}
