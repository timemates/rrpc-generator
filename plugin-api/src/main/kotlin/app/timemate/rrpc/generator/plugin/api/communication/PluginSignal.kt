package app.timemate.rrpc.generator.plugin.api.communication

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.generator.plugin.api.PluginService
import app.timemate.rrpc.proto.schema.RSFile

@Serializable
public sealed interface PluginSignal : GPSignal {
    @Serializable
    public data class SendMetaInformation(
        @ProtoNumber(1)
        public val info: MetaInformation,
    ) : PluginSignal {
        @Serializable
        public data class MetaInformation(
            @ProtoNumber(1)
            public val name: String,
            @ProtoNumber(2)
            public val description: String,
            @ProtoNumber(3)
            public val options: List<OptionDescriptor>,
            @ProtoNumber(4)
            public val role: PluginService.PluginRole
        )
    }

    @Serializable
    public data class ChangedInput(
        @ProtoNumber(1)
        public val files: List<RSFile>,
    ) : PluginSignal

    @Serializable
    public data class LogMessage(
        @ProtoNumber(1)
        public val message: String,
        @ProtoNumber(2)
        public val level: RLogger.Level,
    ) : PluginSignal

    @Serializable
    public data object CodeGenerated : PluginSignal
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

                    is PluginSignal.ChangedInput ->
                        PluginMessage.SignalOneOf.ChangedInput(value)

                    is PluginSignal.SendMetaInformation -> PluginMessage.SignalOneOf.SendMetadataInformation(value)

                    is PluginSignal.CodeGenerated -> PluginMessage.SignalOneOf.CodeGenerated(value)

                    is PluginSignal.LogMessage -> PluginMessage.SignalOneOf.LogMessage(value)

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
        data class CodeGenerated(
            @ProtoNumber(2)
            override val value: PluginSignal.CodeGenerated,
        ) : SignalOneOf

        @Serializable
        data class ChangedInput(
            @ProtoNumber(3)
            override val value: PluginSignal.ChangedInput,
        ) : SignalOneOf

        @Serializable
        data class SendMetadataInformation(
            @ProtoNumber(4)
            override val value: PluginSignal.SendMetaInformation,
        ) : SignalOneOf

        @Serializable
        data class LogMessage(
            @ProtoNumber(5)
            override val value: PluginSignal.LogMessage,
        ) : SignalOneOf
    }
}