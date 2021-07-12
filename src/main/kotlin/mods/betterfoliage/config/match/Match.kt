package mods.betterfoliage.config.match

typealias RuleLogConsumer = (ConfigSource, String) -> Unit

sealed class MatchValue(val description: String) {
    class Found<T>(description: String, val value: T) : MatchValue(description)
    class Missing(description: String) : MatchValue(description)
    class Invalid(description: String) : MatchValue(description)
}

sealed class MatchResult {
    abstract val isSuccess: Boolean
    abstract val isInvariant: Boolean
    abstract val configSource: ConfigSource
    abstract fun log(logger: RuleLogConsumer)

    class UniComparison(
        override val isSuccess: Boolean,
        override val isInvariant: Boolean,
        override val configSource: ConfigSource,
        val sourceValue: MatchValue,
        val targetValue: String,
        val matchMethod: MatchMethod
    ) : MatchResult() {
        override fun log(logger: RuleLogConsumer) = logger(
            configSource,
            "${sourceValue.description} ${matchMethod.description(isSuccess)} \"$targetValue\""
        )
    }

    class BiComparison(
        override val isSuccess: Boolean,
        override val isInvariant: Boolean,
        override val configSource: ConfigSource,
        val source: MatchValue,
        val target: MatchValue,
        val matchMethod: MatchMethod
    ) : MatchResult() {
        override fun log(logger: RuleLogConsumer) = logger(
            configSource,
            "${source.description} ${matchMethod.description(isSuccess)} ${target.description}"
        )
    }

    class InvalidValue(override val configSource: ConfigSource, val value: MatchValue, val description: String) : MatchResult() {
        override val isSuccess = false
        override val isInvariant = true

        override fun log(logger: RuleLogConsumer) = logger(configSource, description)
    }

    class Error(override val configSource: ConfigSource, val description: String) : MatchResult() {
        override val isSuccess = false
        override val isInvariant = true

        override fun log(logger: RuleLogConsumer) = logger(configSource, description)
    }

    class Action(override val configSource: ConfigSource, val description: String) : MatchResult() {
        override val isSuccess = true
        override val isInvariant = false

        override fun log(logger: RuleLogConsumer) = logger(configSource, description)
    }

    class Any(override val configSource: ConfigSource, val results: List<MatchResult>) : MatchResult() {
        override val isSuccess = results.any(MatchResult::isSuccess)
        override val isInvariant = results.all(MatchResult::isInvariant)

        override fun log(logger: RuleLogConsumer) {
            val toLog = if (results.any { it.isSuccess }) results.filter { it.isSuccess } else results
            toLog.forEach { it.log(logger) }
        }
    }

    class RootList(override val configSource: ConfigSource, val results: List<MatchResult>) : MatchResult() {
        override val isSuccess = results.all(MatchResult::isSuccess)
        override val isInvariant = results.all(MatchResult::isInvariant)

        override fun log(logger: RuleLogConsumer) {
            results.forEach { it.log(logger) }
        }
    }
}

fun Node.HasSource.error(description: String) = MatchResult.Error(configSource, description)
fun Node.HasSource.notImplemented() = MatchResult.Error(configSource, "match type not implemented: ${this::class.java.name}")
fun Node.HasSource.action(description: String) = MatchResult.Action(configSource, description)
fun Node.HasSource.invalidValue(value: MatchValue) = MatchResult.InvalidValue(
    configSource, value, "invalid value: ${value.description}"
)
fun Node.HasSource.invalidValueType(comparisonLeft: String, value: Node.Value) = MatchResult.Error(
    configSource, "invalid type for $comparisonLeft: [${value::class.java.name}] \"${value.value}\""
)
fun <T> Node.MatchValueList.compare(sourceValue: MatchValue, targetValue: Node.Value, func: (MatchValue.Found<T>) -> Boolean): MatchResult {
    if (sourceValue is MatchValue.Missing || sourceValue is MatchValue.Invalid) return invalidValue(sourceValue)
    val isSuccess = func(sourceValue as MatchValue.Found<T>)
    return MatchResult.UniComparison(isSuccess, true, configSource, sourceValue, targetValue.value, matchMethod)
}
fun <T> Node.MatchValueList.compare(sourceValue: MatchValue, targetValue: MatchValue, func: (MatchValue.Found<T>, MatchValue.Found<T>) -> Boolean): MatchResult {
    if (sourceValue is MatchValue.Missing || sourceValue is MatchValue.Invalid) return invalidValue(sourceValue)
    if (targetValue is MatchValue.Missing || targetValue is MatchValue.Invalid) return invalidValue(targetValue)
    val isSuccess = func(sourceValue as MatchValue.Found<T>, targetValue as MatchValue.Found<T>)
    return MatchResult.BiComparison(isSuccess, true, configSource, sourceValue, targetValue, matchMethod)
}

enum class MatchMethod {
    EXACT_MATCH, EXTENDS, CONTAINS;

    fun description(isSuccess: Boolean) = when(this) {
        EXACT_MATCH -> if (isSuccess) "matches" else "does not match"
        EXTENDS -> if (isSuccess) "extends" else "does not extend"
        CONTAINS -> if (isSuccess) "contains" else "does not contain"

    }
}

