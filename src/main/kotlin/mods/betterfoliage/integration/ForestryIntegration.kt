package mods.betterfoliage.integration

/*
val TextureLeaves = ClassRefOld<Any>("forestry.arboriculture.models.TextureLeaves")
val TextureLeaves_leafTextures = FieldRefOld(TextureLeaves, "leafTextures", Map)
val TextureLeaves_plain = FieldRefOld(TextureLeaves, "plain", Identifier)
val TextureLeaves_fancy = FieldRefOld(TextureLeaves, "fancy", Identifier)
val TextureLeaves_pollinatedPlain = FieldRefOld(TextureLeaves, "pollinatedPlain", Identifier)
val TextureLeaves_pollinatedFancy = FieldRefOld(TextureLeaves, "pollinatedFancy", Identifier)


val TileLeaves = ClassRefOld<Any>("forestry.arboriculture.tiles.TileLeaves")
val TileLeaves_getLeaveSprite = MethodRefOld(TileLeaves, "getLeaveSprite", Identifier, boolean)
val PropertyWoodType = ClassRefOld<Any>("forestry.arboriculture.blocks.PropertyWoodType")
val IWoodType = ClassRefOld<Any>("forestry.api.arboriculture.IWoodType")
val IWoodType_barkTex = MethodRefOld(IWoodType, "getBarkTexture", String)
val IWoodType_heartTex = MethodRefOld(IWoodType, "getHeartTexture", String)

val PropertyTreeType = ClassRefOld<Any>("forestry.arboriculture.blocks.PropertyTreeType")
val IAlleleTreeSpecies = ClassRefOld<Any>("forestry.api.arboriculture.IAlleleTreeSpecies")
val ILeafSpriteProvider = ClassRefOld<Any>("forestry.api.arboriculture.ILeafSpriteProvider")
val TreeDefinition = ClassRefOld<Any>("forestry.arboriculture.genetics.TreeDefinition")

val IAlleleTreeSpecies_getLeafSpriteProvider = MethodRefOld(IAlleleTreeSpecies, "getLeafSpriteProvider", ILeafSpriteProvider)
val TreeDefinition_species = FieldRefOld(TreeDefinition, "species", IAlleleTreeSpecies)
val ILeafSpriteProvider_getSprite = MethodRefOld(ILeafSpriteProvider, "getSprite", Identifier, boolean, boolean)

object ForestryIntegration {
    init {
    }
}

 */
/*
object ForestryLeafDiscovery : HasLogger, AsyncSpriteProvider<ModelLoader>, ModelRenderRegistry<LeafInfo> {
    override val logger = BetterFoliage.logDetail
    var idToValue = emptyMap<Identifier, LeafInfo>()

    override fun get(state: BlockState, world: BlockView, pos: BlockPos): LeafInfo? {
        // check variant property (used in decorative leaves)
        state.entries.entries.find {
            PropertyTreeType.isInstance(it.key) && TreeDefinition.isInstance(it.value)
        } ?.let {
            val species = it.value[TreeDefinition_species]!!
            val spriteProvider = species[IAlleleTreeSpecies_getLeafSpriteProvider]()
            val textureLoc = spriteProvider[ILeafSpriteProvider_getSprite](false, MinecraftClient.isFancyGraphicsEnabled())
            return idToValue[textureLoc]
        }

        // extract leaf texture information from TileEntity
        val tile = world.getBlockEntity(pos) ?: return null
        if (!TileLeaves.isInstance(tile)) return null
        val textureLoc = tile[TileLeaves_getLeaveSprite](MinecraftClient.isFancyGraphicsEnabled())
        return idToValue[textureLoc]
    }

    override fun setup(manager: ResourceManager, bakeryF: CompletableFuture<ModelLoader>, atlasFuture: AtlasFuture): StitchPhases {
        val futures = mutableMapOf<Identifier, CompletableFuture<LeafInfo>>()

        return StitchPhases(
            discovery = bakeryF.thenRunAsync {
                val allLeaves = TextureLeaves_leafTextures.getStatic()
                allLeaves!!.entries.forEach { (type, leaves) ->
                    log("base leaf type $type")
                    leaves!!
                    listOf(
                        leaves[TextureLeaves_plain], leaves[TextureLeaves_pollinatedPlain],
                        leaves[TextureLeaves_fancy], leaves[TextureLeaves_pollinatedFancy]
                    ).forEach { textureLocation ->
                        futures[textureLocation!!] = defaultRegisterLeaf(textureLocation, atlasFuture)
                    }
                }
            },
            cleanup = atlasFuture.runAfter {
                idToValue = futures.mapValues { it.value.get() }
            }
        )
    }
}

object ForestryLogDiscovery : ModelDiscovery<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail
    override fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<ColumnTextureInfo>? {
        // respect class list to avoid triggering on fences, stairs, etc.
        if (!BetterFoliageMod.blockConfig.logBlocks.matchesClass(ctx.state.block)) return null

        // find wood type property
        val woodType = ctx.state.entries.entries.find {
            PropertyWoodType.isInstance(it.key) && IWoodType.isInstance(it.value)
        }
        if (woodType != null) {
            logger.log(Level.DEBUG, "ForestryLogRegistry: block state ${ctx.state}")
            logger.log(Level.DEBUG, "ForestryLogRegistry:     variant ${woodType.value}")

            // get texture names for wood type
            val bark = woodType.value[IWoodType_barkTex]()
            val heart = woodType.value[IWoodType_heartTex]()
            logger.log(Level.DEBUG, "ForestryLogSupport:    textures [heart=$heart, bark=$bark]")

            val heartSprite = atlas.sprite(heart)
            val barkSprite = atlas.sprite(bark)
            return atlas.mapAfter {
                SimpleColumnInfo(AsyncLogDiscovery.getAxis(ctx.state), heartSprite.get(), heartSprite.get(), listOf(barkSprite.get()))
            }
        }
        return null
    }
}

 */
