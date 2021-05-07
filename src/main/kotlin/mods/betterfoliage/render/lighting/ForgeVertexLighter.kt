package mods.betterfoliage.render.lighting

interface ForgeVertexLighterAccess {
    var vertexLighter: ForgeVertexLighter
}

interface ForgeVertexLighter {
    fun updateVertexLightmap(normal: FloatArray, lightmap: FloatArray, x: Float, y: Float, z: Float)
    fun updateVertexColor(normal: FloatArray, color: FloatArray, x: Float, y: Float, z: Float, tint: Float, multiplier: Int)
}