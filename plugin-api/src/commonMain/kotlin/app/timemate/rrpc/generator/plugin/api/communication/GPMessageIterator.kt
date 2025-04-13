package app.timemate.rrpc.generator.plugin.api.communication

/**
 * An asynchronous iterator for signals, allowing suspending iteration.
 */
public interface GPMessageIterator<T : GPMessage<*>> {
    public val isClosed: Boolean
    /**
     * Checks if there are more signals available.
     */
    public suspend operator fun hasNext(): Boolean

    /**
     * Retrieves the next signal. Should only be called after [hasNext] returns `true`.
     */
    public suspend operator fun next(): T
}