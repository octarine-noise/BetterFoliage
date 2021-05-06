package mods.betterfoliage.render.lighting

interface ForgeVertexLighterAccess {
    var vertexLighter: ForgeVertexLighter
}

interface ForgeVertexLighter {
    fun updateVertexLightmap(normal: FloatArray, lightmap: FloatArray, x: Float, y: Float, z: Float)
    fun updateVertexColor(normal: FloatArray, color: FloatArray, x: Float, y: Float, z: Float, tint: Float, multiplier: Int)
}

fun ForgeVertexLighter.grass() = object: ForgeVertexLighter {
    override fun updateVertexLightmap(normal: FloatArray, lightmap: FloatArray, x: Float, y: Float, z: Float) {
        this@grass.updateVertexLightmap(normal, lightmap, x  * 0.5f, 1.0f, z * 0.5f)
    }

    override fun updateVertexColor(normal: FloatArray, color: FloatArray, x: Float, y: Float, z: Float, tint: Float, multiplier: Int)  {
        this@grass.updateVertexColor(normal, color, x  * 0.5f, 1.0f, z * 0.5f, tint, multiplier
        )
    }
}

fun ForgeVertexLighter.grassSimple() = object: ForgeVertexLighter {
    val normalUp = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f)
    override fun updateVertexLightmap(normal: FloatArray, lightmap: FloatArray, x: Float, y: Float, z: Float) {
        this@grassSimple.updateVertexLightmap(normalUp, lightmap, 0.0f, 1.0f, 0.0f)
    }

    override fun updateVertexColor(normal: FloatArray, color: FloatArray, x: Float, y: Float, z: Float, tint: Float, multiplier: Int)  {
        this@grassSimple.updateVertexColor(normalUp, color, 0.0f, 1.0f, 0.0f, tint, multiplier
        )
    }
}