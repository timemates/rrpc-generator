package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@Serializable(with = RSTypeSerializer::class)
public sealed interface RSType : RSNode, Documentable {
    public val name: String
    public val typeUrl: RSDeclarationUrl
    public val nestedTypes: List<RSType>
    public val nestedExtends: List<RSExtend>
    override val documentation: String?
    public val options: RSOptions
    public val location: RSElementLocation


    public data class Enum(
        override val name: String,
        val constants: List<RSEnumConstant> = emptyList(),
        override val documentation: String?,
        override val options: RSOptions,
        override val nestedTypes: List<RSType> = emptyList(),
        override val typeUrl: RSDeclarationUrl,
        override val nestedExtends: List<RSExtend> = emptyList(),
        override val location: RSElementLocation,
    ) : RSType


    public data class Message(
        override val name: String,
        override val documentation: String? = null,
        val fields: List<RSField> = emptyList(),
        val oneOfs: List<RSOneOf> = emptyList(),
        override val options: RSOptions,
        override val typeUrl: RSDeclarationUrl,
        override val nestedTypes: List<RSType> = emptyList(),
        override val nestedExtends: List<RSExtend> = emptyList(),
        override val location: RSElementLocation,
    ) : RSType {
        val allFields: List<RSField> get() = fields + oneOfs.flatMap { it.fields }

        /**
         * Gets [RSField] in current [RSType.Message] by given [tag].
         * If a field with a tag persists in the oneof field – oneof field is returned,
         *  where the field occurs.
         */
        public fun field(tag: Int): RSField? {
            return fields.firstOrNull { field ->
                field.tag == tag
            }
        }

        /**
         * Gets [RSField] in current [RSType.Message] by given [name].
         * If a field with a tag persists in the oneof field – oneof field is returned,
         *  where the field occurs.
         */
        public fun field(name: String): RSField? {
            return fields.firstOrNull { field ->
                field.name == name
            }
        }

        public override fun equals(other: Any?): Boolean {
            return other is Message && other.typeUrl == typeUrl
        }

        override fun hashCode(): Int {
            return typeUrl.hashCode()
        }
    }

    public data class Enclosing(
        override val name: String,
        override val documentation: String?,
        override val typeUrl: RSDeclarationUrl,
        override val nestedTypes: List<RSType> = emptyList(),
        override val nestedExtends: List<RSExtend> = emptyList(),
        override val options: RSOptions = RSOptions.EMPTY,
        override val location: RSElementLocation
    ) : RSType
}

@Serializable
@SerialName("RawRSType")
private data class RawRSType(
    @ProtoNumber(1) val type: Type,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val typeUrl: RSDeclarationUrl,
    @ProtoNumber(4) val nestedTypes: List<RawRSType> = emptyList(),
    @ProtoNumber(5) val nestedExtends: List<RSExtend> = emptyList(),
    @ProtoNumber(6) val documentation: String? = null,
    @ProtoNumber(7) val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(8) val location: RSElementLocation,
    @ProtoNumber(9) val constants: List<RSEnumConstant>? = null,
    @ProtoNumber(10) val fields: List<RSField>? = null,
    @ProtoNumber(11) val oneOfs: List<RSOneOf>? = null
) {
    @Serializable
    enum class Type {
        @ProtoNumber(0) ENUM, @ProtoNumber(1) MESSAGE, @ProtoNumber(2) ENCLOSING_TYPE
    }

    fun toRSType(): RSType = when (type) {
        Type.ENUM -> RSType.Enum(
            name, constants.orEmpty(), documentation, options, nestedTypes.map { it.toRSType() }, typeUrl, nestedExtends, location
        )
        Type.MESSAGE -> RSType.Message(
            name, documentation, fields.orEmpty(), oneOfs.orEmpty(), options, typeUrl, nestedTypes.map { it.toRSType() }, nestedExtends, location
        )
        Type.ENCLOSING_TYPE -> RSType.Enclosing(name, documentation, typeUrl, nestedTypes.map { it.toRSType() }, nestedExtends, options, location)
    }
}

private fun RSType.toRaw(): RawRSType = when (this) {
    is RSType.Enum -> RawRSType(RawRSType.Type.ENUM, name, typeUrl, nestedTypes.map { it.toRaw() }, nestedExtends, documentation, options, location, constants)
    is RSType.Message -> RawRSType(RawRSType.Type.MESSAGE, name, typeUrl, nestedTypes.map { it.toRaw() }, nestedExtends, documentation, options, location, fields = fields, oneOfs = oneOfs)
    is RSType.Enclosing -> RawRSType(RawRSType.Type.ENCLOSING_TYPE, name, typeUrl, nestedTypes.map { it.toRaw() }, nestedExtends, documentation, options, location)
}

@OptIn(ExperimentalSerializationApi::class)
private object RSTypeSerializer : KSerializer<RSType> {
    private val rawSerializer = RawRSType.serializer()

    override val descriptor: SerialDescriptor = rawSerializer.descriptor

    override fun serialize(encoder: Encoder, value: RSType) {
        encoder.encodeSerializableValue(rawSerializer, value.toRaw())
    }

    override fun deserialize(decoder: Decoder): RSType {
        return decoder.decodeSerializableValue(rawSerializer).toRSType()
    }
}
