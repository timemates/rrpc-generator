package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import kotlin.jvm.JvmInline


@Serializable
public class RSOption(
    @ProtoNumber(1)
    public val name: String,
    @ProtoNumber(2)
    public val fieldUrl: RSTypeMemberUrl,
    /**
     * Value is present only if RPSOption is present in position where a specific value is
     * possible, it can also be default value if supported.
     */
    @ProtoNumber(3)
    private val raw: Value.Raw? = null,
    @ProtoNumber(4)
    private val rawMap: Value.RawMap? = null,
    @ProtoNumber(5)
    private val rawMessage: Value.MessageMap? = null,
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
    }

    @Serializable
    public sealed interface Value {
        @Serializable
        public data class Raw(@ProtoNumber(1) public val string: String) : Value {
            override fun toString(): String {
                return string
            }
        }
        @Serializable
        public data class RawMap(@ProtoNumber(1) public val map: Map<Raw, Raw>) : Value
        @Serializable
        public data class MessageMap(@ProtoNumber(1) public val map: Map<RSTypeMemberUrl, Value>) : Value
    }
}