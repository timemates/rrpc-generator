package app.timemate.rrpc.generator.plugin.api.communication

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import kotlinx.serialization.protobuf.ProtoPacked
import kotlinx.serialization.protobuf.ProtoType
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.proto.schema.RSFile

public sealed interface GeneratorSignal : GPSignal {
    /**
     * Command to request a list of all possible options, name and description of the plugin.
     */
    @Serializable
    public data class FetchMetadata(
        @EncodeDefault
        @ProtoNumber(1)
        val dummy: String = "placeholder"
    ) : GeneratorSignal

    /**
     * Sends the user's schema information to the plugin.
     */
    @Serializable
    public data class SendInput(
        @EncodeDefault
        @ProtoPacked
        @ProtoNumber(1)
        public val files: List<RSFile>,
        @ProtoNumber(2)
        @ProtoPacked
        public val options: GenerationOptions,
    ) : GeneratorSignal
}

/**
 * Alias to the [GeneratorMessage.Companion.create].
 */
public fun GeneratorMessage(
    block: GeneratorMessage.Builder.() -> Unit,
): GeneratorMessage = GeneratorMessage.create(block)

/**
 * Represents a message sent from the generator to the plugin, containing an identifier and a specific command.
 *
 * This class is designed to encapsulate a generator signal along with a unique signal ID for tracking.
 * It ensures type safety through a sealed structure of commands, making the message handling robust and extensible.
 *
 * @property id The unique identifier for the signal, used to track and correlate requests and responses.
 * @property signal The actual signal being transmitted, encapsulated as a [GeneratorSignal].
 */
@ConsistentCopyVisibility
@Serializable
public data class GeneratorMessage private constructor(
    @ProtoType(ProtoIntegerType.DEFAULT)
    @ProtoNumber(1)
    public override val id: SignalId,
    @EncodeDefault
    @ProtoOneOf
    private val signalOneOf: GenSignalOneOf,
) : GPMessage<GeneratorSignal> {
    /**
     * Provides access to the signal being transmitted.
     */
    public override val signal: GeneratorSignal get() = signalOneOf.value

    public companion object {
        /**
         * Factory method to create a [GeneratorMessage] using a builder pattern.
         *
         * @param block A lambda used to configure the builder.
         * @return A fully constructed [GeneratorMessage] instance.
         */
        public fun create(block: Builder.() -> Unit): GeneratorMessage =
            Builder().apply(block).build()
    }

    /**
     * Builder class for constructing a [GeneratorMessage].
     *
     * This builder allows for flexible creation of generator messages by setting required properties step by step.
     * It enforces the presence of an ID and a command, ensuring validity at construction time.
     */
    public class Builder {
        /**
         * The unique identifier for the message.
         */
        public var id: SignalId? = null

        private var signalOneOf: GenSignalOneOf? = null

        /**
         * The signal associated with the message.
         * Assigning a value automatically maps it to the appropriate sealed subclass of [GenSignalOneOf].
         */
        public var signal: GeneratorSignal?
            get() = error("Shouldn't be accessed.")
            set(value) {
                signalOneOf = when (value) {
                    is GeneratorSignal.FetchMetadata ->
                        GenSignalOneOf.FetchMetadataField(value)

                    is GeneratorSignal.SendInput ->
                        GenSignalOneOf.SendInputField(value)

                    null -> null
                }
            }

        /**
         * Constructs the [GeneratorMessage] instance, ensuring that all required fields are set.
         *
         * @throws IllegalStateException if either `id` or `command` is null.
         */
        internal fun build(): GeneratorMessage = GeneratorMessage(
            id ?: error("GeneratorMessage is required to have an id."),
            signalOneOf ?: error("GeneratorMessage is required to have a command."),
        )
    }
}


@Serializable
private sealed interface GenSignalOneOf {
    @Serializable
    data class FetchMetadataField(
        @EncodeDefault
        @ProtoNumber(3)
        val value: GeneratorSignal.FetchMetadata,
    ) : GenSignalOneOf

    @Serializable
    data class SendInputField(
        @EncodeDefault
        @ProtoNumber(4)
        val value: GeneratorSignal.SendInput,
    ) : GenSignalOneOf
}

private val GenSignalOneOf.value: GeneratorSignal get() = when (this) {
    is GenSignalOneOf.FetchMetadataField -> value
    is GenSignalOneOf.SendInputField -> value
}