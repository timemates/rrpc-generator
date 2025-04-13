package app.timemate.rrpc.proto.schema.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class RSDeclarationUrl(public val value: String) {
    public val isScalar: Boolean get() = this in SCALAR_TYPES
    public val isWrapper: Boolean get() = this in WRAPPER_TYPES
    public val isGoogleBuiltin: Boolean get() = this in GOOGLE_BUILTIN_TYPES

    public val isMap: Boolean get() = value.startsWith("map<")
    public val firstTypeArgument: RSDeclarationUrl?
        get() =
            if (isMap)
                RSDeclarationUrl(value.substringAfter('<').substringBefore(','))
            else null

    public val secondTypeArgument: RSDeclarationUrl?
        get() =
            if (isMap)
                RSDeclarationUrl(value.substringAfter(',').substringBefore('>').trim())
            else null

    public val simpleName: String get() = value.substringAfterLast('/').substringAfterLast('.')
    public val enclosingTypeOrPackage: String?
        get() {
            val string = value.substringAfterLast('/')
            val dot = string.lastIndexOf('.')
            return if (dot == -1) null else string.substring(0, dot)
        }

    override fun toString(): String {
        return value
    }

    public companion object {
        public val UNKNOWN: RSDeclarationUrl = RSDeclarationUrl("unknown")

        public val INT32: RSDeclarationUrl = RSDeclarationUrl("int32")
        public val INT64: RSDeclarationUrl = RSDeclarationUrl("int64")

        public val STRING: RSDeclarationUrl = RSDeclarationUrl("string")

        public val SINT32: RSDeclarationUrl = RSDeclarationUrl("int32")
        public val SINT64: RSDeclarationUrl = RSDeclarationUrl("int64")

        public val BOOL: RSDeclarationUrl = RSDeclarationUrl("bool")

        public val UINT32: RSDeclarationUrl = RSDeclarationUrl("uint32")
        public val UINT64: RSDeclarationUrl = RSDeclarationUrl("uint64")

        public val SFIXED32: RSDeclarationUrl = RSDeclarationUrl("sfixed32")
        public val SFIXED64: RSDeclarationUrl = RSDeclarationUrl("sfixed64")

        public val FIXED32: RSDeclarationUrl = RSDeclarationUrl("fixed32")
        public val FIXED64: RSDeclarationUrl = RSDeclarationUrl("fixed64")

        public val FLOAT: RSDeclarationUrl = RSDeclarationUrl("float")
        public val DOUBLE: RSDeclarationUrl = RSDeclarationUrl("double")

        public val BYTES: RSDeclarationUrl = RSDeclarationUrl("bytes")

        public fun ofMap(first: RSDeclarationUrl, second: RSDeclarationUrl): RSDeclarationUrl {
            return RSDeclarationUrl("map<${first.value}, ${second.value}>")
        }

        public val SCALAR_TYPES: List<RSDeclarationUrl> = listOf(
            INT32,
            INT64,
            STRING,
            SINT32,
            SINT64,
            BOOL,
            UINT32,
            UINT64,
            STRING,
            FIXED32,
            FIXED64,
            FLOAT,
            DOUBLE,
            BYTES,
        )

        public val ANY: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Any")
        public val TIMESTAMP: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Timestamp")
        public val DURATION: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Duration")
        public val EMPTY: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Empty")
        public val STRUCT: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Struct")
        public val STRUCT_MAP: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.StructMap")
        public val STRUCT_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Value")
        public val STRUCT_NULL: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.NullValue")
        public val STRUCT_LIST: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.ListValue")

        public val GOOGLE_BUILTIN_TYPES: List<RSDeclarationUrl> = listOf(
            ANY,
            TIMESTAMP,
            DURATION,
            EMPTY,
            STRUCT,
            STRUCT_MAP,
            STRUCT_VALUE,
            STRUCT_NULL,
            STRUCT_LIST,
        )

        public val DOUBLE_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.DoubleValue")
        public val FLOAT_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.FloatValue")
        public val INT32_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Int32Value")
        public val INT64_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.Int64Value")
        public val UINT32_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.UInt32Value")
        public val UINT64_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.UINT64Value")
        public val STRING_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.StringValue")
        public val BYTES_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.BytesValue")
        public val BOOL_VALUE: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/google.protobuf.BoolValue")

        public val ACK: RSDeclarationUrl = RSDeclarationUrl("type.googleapis.com/timemates.rrpc.Ack")

        public val WRAPPER_TYPES: List<RSDeclarationUrl> = listOf(
            DOUBLE_VALUE,
            FLOAT_VALUE,
            INT32_VALUE,
            INT64_VALUE,
            UINT32_VALUE,
            UINT64_VALUE,
            STRING_VALUE,
            BYTES_VALUE,
        )
    }
}