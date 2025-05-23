package app.timemate.rrpc.generator.plugin.api.communication

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import app.timemate.rrpc.generator.plugin.api.GenerationOption
import app.timemate.rrpc.generator.plugin.api.OptionTypeKind
import app.timemate.rrpc.generator.plugin.api.RepeatableGenerationOption

public fun OptionDescriptor(
    block: OptionDescriptor.Builder.() -> Unit
): OptionDescriptor = OptionDescriptor.create(block)

@ConsistentCopyVisibility
@Serializable
public data class OptionDescriptor private constructor(
    @ProtoNumber(1)
    public val name: String,
    @ProtoNumber(2)
    public val description: String,
    @ProtoNumber(3)
    public val isRepeatable: Boolean,
    @ProtoOneOf
    private val kindOneOf: OptionTypeKindOneOf,
) {
    public val kind: OptionTypeKind get() = kindOneOf.value

    public companion object {
        public fun create(block: Builder.() -> Unit): OptionDescriptor {
            return Builder().apply(block).build()
        }
    }

    @Serializable
    private sealed interface OptionTypeKindOneOf {
        val value: OptionTypeKind

        @Serializable
        data class TextField(
            @ProtoNumber(4)
            override val value: OptionTypeKind.Text,
        ) : OptionTypeKindOneOf

        @Serializable
        data class BooleanField(
            @ProtoNumber(5)
            override val value: OptionTypeKind.Boolean,
        ) : OptionTypeKindOneOf

        @Serializable
        data class IntField(
            @ProtoNumber(6)
            override val value: OptionTypeKind.Number.Int,
        ) : OptionTypeKindOneOf

        @Serializable
        data class LongField(
            @ProtoNumber(7)
            override val value: OptionTypeKind.Number.Long,
        ) : OptionTypeKindOneOf

        @Serializable
        data class FloatField(
            @ProtoNumber(8)
            override val value: OptionTypeKind.Number.Float,
        ) : OptionTypeKindOneOf

        @Serializable
        data class DoubleField(
            @ProtoNumber(9)
            override val value: OptionTypeKind.Number.Double,
        ) : OptionTypeKindOneOf

        @Serializable
        data class PathField(
            @ProtoNumber(10)
            override val value: OptionTypeKind.Path,
        ) : OptionTypeKindOneOf

        @Serializable
        data class ChoiceField(
            @ProtoNumber(11)
            override val value: OptionTypeKind.Choice,
        ) : OptionTypeKindOneOf
    }

    public class Builder {
        public var name: String? = null
        public var description: String? = null
        public var isRepeatable: Boolean = false

        private var kindOneOf: OptionTypeKindOneOf? = null

        /**
         * The option type.
         * Assigning a value automatically maps it to the appropriate sealed subclass of [OptionTypeKindOneOf].
         */
        public var kind: OptionTypeKind?
            get() = kindOneOf?.value
            set(value) {
                kindOneOf = when (value) {
                    is OptionTypeKind.Text -> OptionTypeKindOneOf.TextField(value)
                    is OptionTypeKind.Boolean -> OptionTypeKindOneOf.BooleanField(value)
                    is OptionTypeKind.Number.Int -> OptionTypeKindOneOf.IntField(value)
                    is OptionTypeKind.Number.Long -> OptionTypeKindOneOf.LongField(value)
                    is OptionTypeKind.Number.Float -> OptionTypeKindOneOf.FloatField(value)
                    is OptionTypeKind.Number.Double -> OptionTypeKindOneOf.DoubleField(value)
                    is OptionTypeKind.Path -> OptionTypeKindOneOf.PathField(value)
                    is OptionTypeKind.Choice -> OptionTypeKindOneOf.ChoiceField(value)
                    else -> null
                }
            }

        internal fun build(): OptionDescriptor = OptionDescriptor(
            name ?: error("Name is required"),
            description ?: error("Description is required"),
            isRepeatable,
            kindOneOf ?: error("Kind is required")
        )
    }
}

public fun GenerationOption.toOptionDescriptor(): OptionDescriptor {
    return OptionDescriptor.create {
        name = this@toOptionDescriptor.name
        description = this@toOptionDescriptor.description
        kind = this@toOptionDescriptor.valueKind

        // Determine repeatability based on the type of GenerationOption
        isRepeatable = this@toOptionDescriptor is RepeatableGenerationOption<*>
    }
}