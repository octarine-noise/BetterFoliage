package mods.betterfoliage.common.util;

public class DeobfNames {

	private DeobfNames() {}
	
	/** MCP name of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_NAME_MCP = "renderBlockByRenderType";
	
	/** Obfuscated name of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_NAME_OBF = "b";
	
	/** MCP signature of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_SIG_MCP = "(Lnet/minecraft/block/Block;III)Z";
	
	/** Obfuscated signature of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_SIG_OBF = "(Lahu;III)Z";
	
	/** MCP signature of BlockRenderTypeOverride.getRenderType(Block) */
	public static final String BRTO_GRT_SIG_MCP = "(Lnet/minecraft/block/Block;)I";
	
	/** Obfuscated signature of BlockRenderTypeOverride.getRenderType(Block) */
	public static final String BRTO_GRT_SIG_OBF = "(Lahu;)I";
	
	/** MCP signature of BlockRenderTypeOverride.getRenderType(Block) */
	public static final String BRTO_GRTO_SIG_MCP = "(ILnet/minecraft/block/Block;)I";
	
	/** Obfuscated signature of BlockRenderTypeOverride.getRenderType(Block) */
	public static final String BRTO_GRTO_SIG_OBF = "(ILahu;)I";
	
	/** MCP name of SimpleReloadableResourceManager.domainResourceManagers */
	public static final String SRRM_DRM_MCP = "domainResourceManagers";

	/** SRG name of SimpleReloadableResourceManager.domainResourceManagers */
	public static final String SRRM_DRM_SRGNAME = "field_110548_a";

	/** MCP name of TextureMap.mapRegisteredSprites */
	public static final String TM_MRS_MCP = "mapRegisteredSprites";
	
	/** Obfuscated name of TextureMap.mapRegisteredSprites */
	public static final String TM_MRS_OBF = "bpr";
	
	/** SRG name of TextureMap.mapRegisteredSprites */
	public static final String TM_MRS_SRG = "field_110574_e";
}
