package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@ConsistentCopyVisibility
@Serializable
public data class RSOption private constructor(
    @ProtoNumber(1)
    public val name: String = "",
    @ProtoNumber(2)
    public val fieldUrl: RSTypeMemberUrl = RSTypeMemberUrl(),
    /**
     * Value is present only if RPSOption is present in position where a specific value is
     * possible, it can also be default value if supported.
     */
    @ProtoNumber(3)
    private val raw: Value.Raw?,
    @ProtoNumber(4)
    private val rawMap: Value.RawMap?,
    @ProtoNumber(5)
    private val rawMessage: Value.MessageMap?,
) {
    public constructor(name: String, fieldUrl: RSTypeMemberUrl, value: Value?) : this(
        name = name,
        fieldUrl = fieldUrl,
        raw = value as? Value.Raw,
        rawMap = value as? Value.RawMap,
        rawMessage = value as? Value.MessageMap,
    )

    public val value: Value? get() = raw ?: rawMap ?: rawMessage

    public companion object {
        public val DEPRECATED: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.METHOD_OPTIONS, "deprecated")
        public val RETENTION: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FIELD_OPTIONS, "retention")
        public val SOURCE_ONLY_MESSAGE: RSTypeMemberUrl =
            RSTypeMemberUrl(RSOptions.MESSAGE_OPTIONS, "timemate.rrpc.internal.source_only_message")
        public val SOURCE_ONLY_ENUM: RSTypeMemberUrl =
            RSTypeMemberUrl(RSOptions.ENUM_OPTIONS, "timemate.rrpc.internal.source_only_enum")
        public val EXTENSION_GENERATION_STRATEGY: RSTypeMemberUrl =
            RSTypeMemberUrl(RSOptions.ENUM_OPTIONS, "timemate.rrpc.internal.extension_generation_strategy")
    }

    @Serializable
    public sealed interface Value {
        @Serializable
        public data class Raw(@ProtoNumber(1) public val string: String = "") : Value {
            override fun toString(): String {
                return string
            }
        }

        @Serializable
        public data class RawMap(@ProtoNumber(1) public val map: Map<Raw, Raw> = emptyMap()) : Value

        @Serializable
        public data class MessageMap(@ProtoNumber(1) public val map: Map<RSTypeMemberUrl, Value> = emptyMap()) : Value
    }
}