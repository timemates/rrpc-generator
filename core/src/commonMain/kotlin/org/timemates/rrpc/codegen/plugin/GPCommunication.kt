package org.timemates.rrpc.codegen.plugin

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource
import okio.EOFException
import org.timemates.rrpc.codegen.plugin.data.*

public typealias PluginCommunication = GPCommunication<GeneratorMessage, PluginMessage>
public typealias GeneratorCommunication = GPCommunication<PluginMessage, GeneratorMessage>

/**
 * Defines an API for plugin-to-generator communication, providing mechanisms to send signals,
 * handle responses, and process incoming signals asynchronously.
 *
 * The implementation is not thread-safe.
 */
public sealed interface GPCommunication<TInput : GPMessage<*>, TOutput : GPMessage<*>> : AutoCloseable {

    /**
     * Sends a signal to the generator and suspends until a response of the expected type is received.
     *
     * @param message The outgoing message to send.
     * @return The response signal matching the expected type.
     * @throws CommunicationException If communication fails or the response type is mismatched.
     */
    public suspend fun send(message: TOutput)

    /**
     * Provides an iterator for processing an incoming message asynchronously.
     *
     * The iterator can be used in a suspending loop to process each signal sequentially.
     */
    public val incoming: GPMessageIterator<TInput>

    /**
     * Returns whether the underlying source accepts any data.
     */
    public val isClosedForSend: Boolean
}

public suspend inline fun <reified TInput : GPSignal> GPCommunication<*, *>.receiveOr(crossinline block: () -> Nothing): TInput = coroutineScope {
    while (isActive) {
        if (incoming.isClosed || isClosedForSend) break
        if (!incoming.hasNext()) continue

        val message = incoming.next()
        val signal = message.signal

        if (signal !is TInput) block() else return@coroutineScope signal
    }

    block()
}

public suspend inline fun <TInput : GPMessage<*>> GPCommunication<TInput, *>.receive(): TInput {
    while (currentCoroutineContext().isActive) {
        if (incoming.isClosed || isClosedForSend) break
        if (!incoming.hasNext()) continue

        return incoming.next()
    }

    error("No message received.")
}

public suspend inline fun <reified TInput : GPSignal> GPCommunication<*, *>.receiveWhileIsAndReturnUnsatisfied(
    processor: (TInput) -> Unit,
): GPSignal {
    while (currentCoroutineContext().isActive) {
        if (incoming.isClosed || isClosedForSend) break
        if (!incoming.hasNext()) continue

        val item = incoming.next().signal
        processor((item as? TInput) ?: return item)
    }

    error("No message received.")
}

public fun PluginCommunication(
    input: BufferedSource,
    output: BufferedSink,
): PluginCommunication =
    GPCommunicationImpl(input, output, GeneratorMessage.serializer(), PluginMessage.serializer())

public fun GeneratorCommunication(
    input: BufferedSource,
    output: BufferedSink,
): GeneratorCommunication =
    GPCommunicationImpl(input, output, PluginMessage.serializer(), GeneratorMessage.serializer())

/**
 * Concrete implementation of [GPCommunication], providing mechanisms for
 * sending messages to the generator and processing incoming messages asynchronously.
 *
 * @property input The source for reading incoming generator messages.
 * @property output The sink for writing outgoing plugin messages.
 * @param coroutineContext The coroutine context used for internal operations.
 */
/**
 * Concrete implementation of [GPCommunication], providing mechanisms for
 * sending messages to the generator and processing incoming messages asynchronously.
 *
 * @property input The source for reading incoming generator messages.
 * @property output The sink for writing outgoing plugin messages.
 * @param coroutineContext The coroutine context used for internal operations.
 */
private class GPCommunicationImpl<TInput : GPMessage<*>, TOutput : GPMessage<*>>(
    private val input: BufferedSource,
    private val output: BufferedSink,
    private val inputSerializer: KSerializer<TInput>,
    private val outputSerializer: KSerializer<TOutput>,
) : GPCommunication<TInput, TOutput> {

    override val incoming: GPMessageIterator<TInput> = object : GPMessageIterator<TInput> {
        private var nextMessage: TInput? = null
        override var isClosed = false
            private set

        override suspend fun hasNext(): Boolean {
            if (nextMessage != null) return true
            if (!input.isOpen) return false
            if (isClosed) return false

            nextMessage = try {
                readMessage()
            } catch (e: EOFException) {
                isClosed = true
                return false // End of stream
            }

            return true
        }

        override suspend fun next(): TInput {
            return nextMessage?.also { nextMessage = null } ?: throw NoSuchElementException("No message available")
        }
    }
    override val isClosedForSend: Boolean
        get() = !output.isOpen

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun send(message: TOutput) {
        val bytes = ProtoBuf.encodeToByteArray(outputSerializer, message)
        val size = bytes.size

        output.writeIntLe(size)
        output.write(bytes)
        output.flush()
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun readMessage(): TInput {
        // Read the size of the message
        val size = input.readIntLe()

        // Read the message bytes based on the size
        val bytes = input.readByteArray(size.toLong())

        // Decode and return the message
        return ProtoBuf.decodeFromByteArray(inputSerializer, bytes)
    }

    override fun close() {
        input.close()
        output.close()
    }
}