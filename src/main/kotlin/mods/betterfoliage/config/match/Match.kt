package mods.betterfoliage.config.match

enum class MatchMethod {
    EXACT_MATCH, EXTENDS, CONTAINS;

    fun description(isSuccess: Boolean) = when (this) {
        EXACT_MATCH -> if (isSuccess) "matches" else "does not match"
        EXTENDS -> if (isSuccess) "extends" else "does not extend"
        CONTAINS -> if (isSuccess) "contains" else "does not contain"
    }
}

/**
 * Basic Either monad implementation
 */
sealed class Either<out L, out R> {
    class Left<L>(val left: L) : Either<L, Nothing>()
    class Right<R>(val right: R) : Either<Nothing, R>()

    fun leftOrNull() = if (this is Left) left else null
    fun rightOrNull() = if (this is Right) right else null

    fun <R2> map(func: (R) -> R2): Either<L, R2> = when (this) {
        is Left<L> -> this
        is Right<R> -> Right(func(right))
    }

    fun <L2> mapLeft(func: (L) -> L2): Either<L2, R> = when (this) {
        is Left<L> -> Left(func(left))
        is Right<R> -> this
    }

    fun ifRight(action: (R) -> Unit) {
        if (this is Right) action(right)
    }

    companion object {
        fun <L> ofLeft(left: L) = Left(left)
        fun <R> ofRight(right: R) = Right(right)
    }
}

// this cannot be inside the class for variance reasons
fun <L, R, R2> Either<L, R>.flatMap(func: (R) -> Either<L, R2>) = when (this) {
    is Either.Left<L> -> this
    is Either.Right<R> -> func(right)
}

fun <L, R, L2> Either<L, R>.flatMapLeft(func: (L) -> Either<L2, R>) = when (this) {
    is Either.Left<L> -> func(left)
    is Either.Right<R> -> this
}

fun <T> Either<T, T>.flatten() = when (this) {
    is Either.Left -> left
    is Either.Right -> right
}

interface MAnything<out T> {
    val value: T
    val immutable: Boolean
}

class MListAll(val list: List<MAnything<Boolean>>) : MAnything<Boolean> {
    override val value get() = list.all { it.value }
    override val immutable get() = list.all { it.immutable }
}

class MListAny(val list: List<MValue<Boolean>>) : MAnything<Boolean> {
    override val value get() = list.any { it.value }
    override val immutable get() = list.all { it.immutable }
}

class MNegated(val inner: MAnything<Boolean>) : MAnything<Boolean> {
    override val value get() = !inner.value
    override val immutable get() = inner.immutable
}

/**
 * Value with metadata related to rule matching applied.
 *
 * @param value the wrapped value
 * @param description human-readable description of what the value represents
 * @param configSource identifies where the value is described in the config
 * @param immutable true if the value never changes
 * (another [MValue] constructed in the same way will have the same value)
 *
 */
class MValue<out T>(
    override val value: T,
    val description: String,
    val configSource: ConfigSource,
    override val immutable: Boolean,
) : MAnything<T> {
    companion object {
        fun <T> right(value: T, description: String, configSource: ConfigSource, immutable: Boolean = true) =
            Either.ofRight(MValue(value, description, configSource, immutable))

        fun left(description: String, configSource: ConfigSource, immutable: Boolean = true) =
            Either.ofLeft(MValue(false, description, configSource, immutable))
    }
}

typealias MEither<T> = Either<MValue<Boolean>, MValue<T>>

val Node.Value.asEither get() = MValue.right(value, value, configSource, true)
fun Node.Value.left(description: String) = MValue.left(description, configSource)
fun Node.invalidTypeFor(type: String) = MValue.left("invalid type for $type: [${this::class.java.name}]", configSource)
fun Node.error(description: String) = MValue.left(description, configSource)

fun <T, R> MEither<T>.mapValue(func: (T) -> R) = map {
    MValue(func(it.value), it.description, it.configSource, it.immutable)
}

fun <T> MEither<T>.mapDescription(func: (MValue<T>) -> String) = map {
    MValue(it.value, func(it), it.configSource, it.immutable)
}

fun <T, R> MEither<T>.map(
    func: (T) -> R,
    description: (MValue<T>, R) -> String
) = map { t -> func(t.value).let { r -> MValue(r, description(t, r), t.configSource, t.immutable) } }

fun <T, R> MEither<T>.mapNotNull(
    func: (T) -> R?,
    dLeft: (MValue<T>) -> String = { it.description },
    dRight: (MValue<T>, R) -> String = { m, _ -> m.description }
) = flatMap { t ->
    func(t.value)?.let { r ->
        MValue.right(r, dRight(t, r), t.configSource, t.immutable)
    } ?: MValue.left(dLeft(t), t.configSource, t.immutable)
}

fun <T> MEither<T>.toRight(value: T) =
    flatMapLeft { MValue.right(value, it.description, it.configSource, it.immutable) }

data class MComparison<T1, T2>(
    private val opTrue: String,
    private val opFalse: String,
    val testFunc: (T1, T2) -> Boolean
) {
    fun compare(value1: MEither<T1>, value2: MEither<T2>) = when {
        value1 is Either.Left -> value1
        value2 is Either.Left -> value2
        else -> {
            val isSuccess = testFunc((value1 as Either.Right).right.value, (value2 as Either.Right).right.value)
            MValue.right(
                isSuccess,
                "${value1.right.description} ${if (isSuccess) opTrue else opFalse} ${value2.right.description}",
                value2.right.configSource,
                value1.right.immutable && value2.right.immutable
            )
        }
    }.flatten()

    companion object {
        fun <T1, T2> of(matchMethod: MatchMethod, testFunc: (T1, T2) -> Boolean) =
            MComparison(matchMethod.description(true), matchMethod.description(false), testFunc)

        val equals = of(MatchMethod.EXACT_MATCH) { t1: Any, t2: Any -> t1 == t2 }
    }
}