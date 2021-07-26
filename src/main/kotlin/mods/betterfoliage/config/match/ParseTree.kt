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
    abstract val configSource: ConfigSource

    class MatchValueList(
        val matchSource: MatchSource,
        val matchMethod: MatchMethod,
        override val configSource: ConfigSource,
        val values: List<Value>
    ) : Node()

    class MatchParam(
        val name: String,
        val values: List<Value>,
        override val configSource: ConfigSource,
    ) : Node()

    class Negate(val node: Node) : Node() {
        override val configSource get() = node.configSource
    }

    class SetParam(val name: String, val value: Value, override val configSource: ConfigSource) : Node()

    class MatchAll(override val configSource: ConfigSource, val list: List<Node>) : Node()

    abstract class Value(override val configSource: ConfigSource, val value: String) : Node() {
        class Literal(configSource: ConfigSource, value: String) : Value(configSource, value)
        class ClassOf(configSource: ConfigSource, value: String) : Value(configSource, value)
        class Texture(configSource: ConfigSource, value: String) : Value(configSource, value)
        class Tint(configSource: ConfigSource, value: String) : Value(configSource, value)
    }
}
