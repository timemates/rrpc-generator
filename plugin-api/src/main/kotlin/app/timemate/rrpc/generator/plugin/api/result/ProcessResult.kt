package app.timemate.rrpc.generator.plugin.api.result

public sealed interface ProcessResult<out T> {

    public data class Success<out T>(
        public val data: T,
    ) : ProcessResult<T>

    public data class Failure(
        public val errors: List<ProcessingError>,
    ) : ProcessResult<Nothing> {
        public constructor(error: ProcessingError) : this(listOf(error))
    }
}

/**
 * Transforms the success value if present.
 * If this is a failure, the same errors are propagated.
 */
public fun <T, R> ProcessResult<T>.map(transform: (T) -> R): ProcessResult<R> =
    when (this) {
        is ProcessResult.Success -> ProcessResult.Success(transform(this.data))
        is ProcessResult.Failure -> this
    }

/**
 * Chains an operation that returns a GenerationResult.
 * Use this to sequence two operations where the second may fail.
 */
public fun <T, R> ProcessResult<T>.flatMap(transform: (T) -> ProcessResult<R>): ProcessResult<R> =
    when (this) {
        is ProcessResult.Success -> transform(this.data)
        is ProcessResult.Failure -> this
    }

/**
 * Executes different lambda expressions based on the result.
 */
public fun <T, R> ProcessResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (List<ProcessingError>) -> R
): R =
    when (this) {
        is ProcessResult.Success -> onSuccess(this.data)
        is ProcessResult.Failure -> onFailure(this.errors)
    }

/**
 * Retrieves the success value or provides an alternative value.
 * This is useful when you want to recover from errors.
 */
public inline fun <T> ProcessResult<T>.getOrElse(default: (ProcessResult.Failure) -> T): T =
    when (this) {
        is ProcessResult.Success -> this.data
        is ProcessResult.Failure -> default(this)
    }

/**
 * Retrieves the success value or throws an exception containing the error details.
 */
public fun <T> ProcessResult<T>.getOrThrow(): T =
    when (this) {
        is ProcessResult.Success -> this.data
        is ProcessResult.Failure -> throw IllegalStateException(
            "Generation failed with errors: ${this.errors.joinToString(", ") { it.message }}"
        )
    }

// MARK: - Side-Effect Handlers

/**
 * Invoke a lambda if success, useful for logging or further side-effects.
 */
public inline fun <T> ProcessResult<T>.onSuccess(action: (T) -> Unit): ProcessResult<T> = apply {
    if (this is ProcessResult.Success) action(this.data)
}

/**
 * Invoke a lambda if failure, letting you log errors or perform additional actions.
 */
public inline fun <T> ProcessResult<T>.onFailure(action: (ProcessResult.Failure) -> Unit): ProcessResult<T> = apply {
    if (this is ProcessResult.Failure) action(this)
}

// MARK: - Filtering

/**
 * Filters a successful result using the given predicate.
 * If the predicate is false, the success is turned into a failure with the provided error.
 */
public fun <T> ProcessResult<T>.filter(predicate: (T) -> Boolean, error: () -> ProcessingError): ProcessResult<T> =
    when (this) {
        is ProcessResult.Success -> {
            if (predicate(this.data)) this
            else ProcessResult.Failure(error())
        }
        is ProcessResult.Failure -> this
    }

// MARK: - Combining / Zipping

/**
 * Combines two GenerationResults.
 * If both are success, returns a pair of their values.
 * If either is failure, accumulates the errors (if both fail, errors are concatenated).
 */
public fun <A, B> ProcessResult<A>.zip(other: ProcessResult<B>): ProcessResult<Pair<A, B>> =
    when {
        this is ProcessResult.Success && other is ProcessResult.Success ->
            ProcessResult.Success(Pair(this.data, other.data))
        this is ProcessResult.Failure && other is ProcessResult.Failure ->
            ProcessResult.Failure(this.errors + other.errors)
        this is ProcessResult.Failure -> this
        else -> other as ProcessResult.Failure
    }

// MARK: - Sequencing Collections

/**
 * Transforms a collection of GenerationResults into a single GenerationResult
 * that contains a list of all success values, or the accumulated errors if any failures occurred.
 */
public fun <T> Iterable<ProcessResult<T>>.flatten(): ProcessResult<List<T>> {
    val successes = mutableListOf<T>()
    val errors = mutableListOf<ProcessingError>()

    forEach { result ->
        when (result) {
            is ProcessResult.Success -> successes.add(result.data)
            is ProcessResult.Failure -> errors.addAll(result.errors)
        }
    }
    return if (errors.isNotEmpty())
        ProcessResult.Failure(errors)
    else
        ProcessResult.Success(successes)
}

/**
 * Combines two GenerationResult computations.
 * Both operations are executed regardless of the outcome of the first.
 *
 * If both operations succeed, returns a Success holding a Pair of results.
 * If any of operations failed, returns the concatenation of both errors.
 */
public inline fun <T, R> ProcessResult<T>.andAccumulate(next: () -> ProcessResult<R>): ProcessResult<Pair<T, R>> {
    // Run the second operation regardless
    val secondResult = next()

    return when {
        // Both succeed
        this is ProcessResult.Success && secondResult is ProcessResult.Success ->
            ProcessResult.Success(Pair(this.data, secondResult.data))

        // Both fail: accumulate errors from both operations
        this is ProcessResult.Failure && secondResult is ProcessResult.Failure ->
            ProcessResult.Failure(this.errors + secondResult.errors)

        // Only the first fails: even if the second succeeded, final result is failure with first errors.
        this is ProcessResult.Failure ->
            ProcessResult.Failure(this.errors)

        // Otherwise, the second must be failure (and the first succeeded); yield second's errors.
        else -> (secondResult as ProcessResult.Failure)
    }
}
