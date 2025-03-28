package org.timemates.rrpc.codegen.plugin.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import org.timemates.rrpc.codegen.plugin.data.PluginMessage.SignalOneOf.*

@Serializable
public sealed interface PluginSignal : GPSignal {
    /**
     * Command to request input arguments that were passed to the generator.
     */
    @Serializable
    public object RequestInput : PluginSignal

    @Serializable
    public data class SendMetaInformation(
        @ProtoNumber(1)
        public val info: MetaInformation,
    ) : PluginSignal {
        @Serializable
        public data class MetaInformation(
            @ProtoNumber(1)
            public val options: List<OptionDescriptor>,
            @ProtoNumber(2)
            public val name: String,
            @ProtoNumber(3)
            public val description: String,
        )
    }

    @Serializable
    public sealed interface RequestStatusChange : PluginSignal {
        public val message: String

        @Serializable
        public data class Finished(public override val message: String) : RequestStatusChange
        @Serializable
        public data class Failed(public override val message: String) : RequestStatusChange
    }
}

/**
 * Represents a message sent from the plugin to the generator, containing an identifier and a specific command.
 *
 * This class encapsulates a plugin signal along with a unique signal ID for tracking purposes.
 * It provides a structured way to handle a variety of signals using sealed types.
 *
 * @property id The unique identifier for the signal, used to correlate requests and responses.
 * @property signal The actual signal being transmitted, encapsulated as a [PluginSignal].
 */
@Serializable
public data class PluginMessage @PublishedApi internal constructor(
    @ProtoNumber(1)
    public override val id: SignalId,
    @ProtoOneOf
    private val signalOneOf: SignalOneOf,
) : GPMessage<PluginSignal> {
    /**
     * Provides access to the signal being transmitted.
     */
    public override val signal: PluginSignal get() = signalOneOf.value

    public companion object {
        /**
         * Factory method to create a [PluginMessage] using a builder pattern.
         *
         * @param block A lambda used to configure the builder.
         * @return A fully constructed [PluginMessage] instance.
         */
        public inline fun create(block: Builder.() -> Unit): PluginMessage =
            Builder().apply(block).build()
    }

    /**
     * Builder class for constructing a [PluginMessage].
     *
     * This builder allows for step-by-step creation of plugin messages by setting required properties.
     * It enforces the presence of an ID and a command, ensuring validity at construction time.
     */
    public class Builder {
        /**
         * The unique identifier for the message.
         */
        public var id: SignalId? = null

        private var signalOneOf: SignalOneOf? = null

        /**
         * The signal associated with the message.
         * Assigning a value automatically maps it to the appropriate sealed subclass of [SignalOneOf].
         */
        public var signal: PluginSignal?
            get() = signalOneOf?.value
            set(value) {
                signalOneOf = when (value) {
                    is PluginSignal.RequestInput ->
                        RequestInputField(value)

                    is PluginSignal.RequestStatusChange.Failed ->
                        RequestStatusFailedField(value)

                    is PluginSignal.SendMetaInformation -> SendOptionsField(value)

                    is PluginSignal.RequestStatusChange.Finished ->
                        RequestStatusFinishedField(value)

                    null -> null
                }
            }

        /**
         * Constructs the [PluginMessage] instance, ensuring that all required fields are set.
         *
         * @throws IllegalStateException if either `id` or `command` is null.
         */
        public fun build(): PluginMessage = PluginMessage(
            id ?: error("PluginMessage is required to have an id."),
            signalOneOf ?: error("PluginMessage is required to have a command."),
        )
    }

    /**
     * Sealed class representing the `OneOf` choice for the plugin command.
     */
    @Serializable
    internal sealed interface SignalOneOf {
        val value: PluginSignal

        @Serializable
        data class RequestInputField(
            @ProtoNumber(2)
            override val value: PluginSignal.RequestInput,
        ) : SignalOneOf

        @Serializable
        data class RequestStatusFinishedField(
            @ProtoNumber(3)
            override val value: PluginSignal.RequestStatusChange.Finished,
        ) : SignalOneOf

        @Serializable
        data class RequestStatusFailedField(
            @ProtoNumber(4)
            override val value: PluginSignal.RequestStatusChange.Failed,
        ) : SignalOneOf

        @Serializable
        data class SendOptionsField(
            @ProtoNumber(5)
            override val value: PluginSignal.SendMetaInformation,
        ) : SignalOneOf
    }
}