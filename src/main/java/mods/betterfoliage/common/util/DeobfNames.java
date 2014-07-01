package mods.betterfoliage.common.util;

public class DeobfNames {

	private DeobfNames() {}
	
	/** MCP name of RenderBlocks */
	public static final String RB_NAME_MCP = "net/minecraft/client/renderer/RenderBlocks";
	
	/** Obfuscated name of RenderBlocks */
	public static final String RB_NAME_OBF = "blm";
	
	/** MCP name of RenderBlocks.blockAccess */
	public static final String RB_BA_NAME_MCP = "blockAccess";
	
	/** Obfuscated name of RenderBlocks.blockAccess */
	public static final String RB_BA_NAME_OBF = "a";
	
	/** MCP signature of RenderBlocks.blockAccess */
	public static final String RB_BA_SIG_MCP = "Lnet/minecraft/world/IBlockAccess;";
	
	/** Obfuscated signature of RenderBlocks.blockAccess */
	public static final String RB_BA_SIG_OBF = "Lahl;";
	
	/** MCP name of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_NAME_MCP = "renderBlockByRenderType";
	
	/** Obfuscated name of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_NAME_OBF = "b";
	
	/** MCP signature of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_SIG_MCP = "(Lnet/minecraft/block/Block;III)Z";
	
	/** Obfuscated signature of RenderBlocks.renderBlockByRenderType() */
	public static final String RB_RBBRT_SIG_OBF = "(Laji;III)Z";
	
	/** MCP signature of BetterFoliageClient.getRenderTypeOverride() */
	public static final String BFC_GRTO_SIG_MCP = "(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/block/Block;I)I";
	
	/** Obfuscated signature of BetterFoliageClient.getRenderTypeOverride() */
	public static final String BFC_GRTO_SIG_OBF = "(Lahl;IIILaji;I)I";
	
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
	
	/** MCP signature of Shaders.pushEntity() */
	public static final String SHADERS_PE_SIG_MCP = "(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;III)V";
	
	/** Obfuscated signature of Shaders.pushEntity() */
	public static final String SHADERS_PE_SIG_OBF = "(Lblm;Laji;III)V";
	
	/** MCP signature of BetterFoliageClient.getGLSLBlockIdOverride() */
	public static final String BFC_GLSLID_SIG_MCP = "(ILnet/minecraft/block/Block;)I"; 
	
	/** Obfuscated signature of BetterFoliageClient.getGLSLBlockIdOverride() */
	public static final String BFC_GLSLID_SIG_OBF = "(ILaji;)I";
	

}
