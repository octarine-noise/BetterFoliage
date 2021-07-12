package mods.betterfoliage.config.match

data class ConfigSource(
    val configFile: String,
    val line: Int,
    val column: Int
) {
    override fun toString() = "$configFile @ R$line,C$column"
}

sealed class Node {
    enum class MatchSource { BLOCK_CLASS, BLOCK_NAME, MODEL_LOCATION }
    interface HasSource { val configSource: ConfigSource }

    class MatchValueList(
        val matchSource: MatchSource,
        val matchMethod: MatchMethod,
        override val configSource: ConfigSource,
        val values: List<Value>
    ) : Node(), HasSource

    class MatchParam(
        val name: String,
        val values: List<Value>,
        override val configSource: ConfigSource,
    ) : Node(), HasSource

    class SetParam(val name: String, val value: Value, override val configSource: ConfigSource) : Node(), HasSource

    class MatchAll(override val configSource: ConfigSource, val list: List<Node>) : Node(), HasSource

    abstract class Value(val value: String) : Node() {
        class Literal(value: String) : Value(value)
        class ClassOf(value: String) : Value(value)
        class Texture(value: String) : Value(value)
    }
}
