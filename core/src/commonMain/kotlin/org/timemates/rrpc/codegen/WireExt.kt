package org.timemates.rrpc.codegen

import com.squareup.wire.schema.*
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.common.schema.*
import org.timemates.rrpc.common.schema.value.RMPackageName
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

internal fun ProtoType.asRMTypeUrl(): RMDeclarationUrl {
    return when {
        isMap -> RMDeclarationUrl("map<${keyType!!.asRMTypeUrl()}, ${valueType!!.asRMTypeUrl()}>")
        isScalar -> when (this) {
            ProtoType.BOOL -> RMDeclarationUrl.BOOL
            ProtoType.BYTES -> RMDeclarationUrl.BYTES
            ProtoType.DOUBLE -> RMDeclarationUrl.DOUBLE
            ProtoType.FLOAT -> RMDeclarationUrl.FLOAT
            ProtoType.FIXED32 -> RMDeclarationUrl.FIXED32
            ProtoType.FIXED64 -> RMDeclarationUrl.FIXED64
            ProtoType.INT32 -> RMDeclarationUrl.INT32
            ProtoType.INT64 -> RMDeclarationUrl.INT64
            ProtoType.SFIXED32 -> RMDeclarationUrl.SFIXED32
            ProtoType.SFIXED64 -> RMDeclarationUrl.SFIXED64
            ProtoType.SINT32 -> RMDeclarationUrl.SINT32
            ProtoType.SINT64 -> RMDeclarationUrl.SINT64
            ProtoType.STRING -> RMDeclarationUrl.STRING
            ProtoType.UINT32 -> RMDeclarationUrl.UINT32
            ProtoType.UINT64 -> RMDeclarationUrl.UINT64
            ProtoType.ANY -> RMDeclarationUrl.ANY
            ProtoType.DURATION -> RMDeclarationUrl.DURATION
            ProtoType.TIMESTAMP -> RMDeclarationUrl.TIMESTAMP
            ProtoType.EMPTY -> RMDeclarationUrl.EMPTY
            ProtoType.STRUCT_MAP -> RMDeclarationUrl.STRUCT_MAP
            ProtoType.STRUCT_LIST -> RMDeclarationUrl.STRUCT_LIST
            ProtoType.STRUCT_VALUE -> RMDeclarationUrl.STRUCT_VALUE
            ProtoType.STRUCT_NULL -> RMDeclarationUrl.STRUCT_NULL
            ProtoType.DOUBLE_VALUE -> RMDeclarationUrl.DOUBLE_VALUE
            ProtoType.FLOAT_VALUE -> RMDeclarationUrl.FLOAT_VALUE
            ProtoType.INT32_VALUE -> RMDeclarationUrl.INT32_VALUE
            ProtoType.INT64_VALUE -> RMDeclarationUrl.INT64_VALUE
            ProtoType.UINT32_VALUE -> RMDeclarationUrl.UINT32_VALUE
            ProtoType.UINT64_VALUE -> RMDeclarationUrl.UINT64_VALUE
            ProtoType.BOOL_VALUE -> RMDeclarationUrl.BOOL_VALUE
            ProtoType.BYTES_VALUE -> RMDeclarationUrl.BYTES_VALUE
            else -> throw GenerationException("Unable to convert scalar type '$this'.")
        }

        else -> RMDeclarationUrl(typeUrl!!)
    }
}

internal fun Field.asRMField(): RSField {
    return RSField(
        tag = tag,
        name = name,
        options = options.asRMOptions(),
        documentation = documentation,
        typeUrl = type!!.asRMTypeUrl(),
        isRepeated = isRepeated,
        isInOneOf = isOneOf,
        isExtension = isExtension,
    )
}

internal fun OneOf.asRMOneOf(): RSOneOf {
    return RSOneOf(
        name = name,
        fields = fields.map { it.asRMField() },
        documentation = documentation,
        options = options.asRMOptions(),
    )
}

internal fun EnumConstant.asRMConstant(): RSEnumConstant {
    return RSEnumConstant(
        name = name,
        tag = tag,
        options = options.asRMOptions(),
        documentation = documentation,
    )
}

internal fun ProtoMember.asRMOption(value: Any?): RSOption {
    return RSOption(
        name = simpleName,
        fieldUrl = RSTypeMemberUrl(type.asRMTypeUrl(), member),
        value = value?.asRMOptionValue(),
    )
}

private fun Any.asRMOptionValue(): RSOption.Value {
    return if (this is Map<*, *>) {
        val firstKey = keys.firstOrNull()

        if (firstKey is ProtoMember) {
            @Suppress("UNCHECKED_CAST")
            RSOption.Value.MessageMap((this as Map<ProtoMember, Any>).map { (key, mapValue) ->
                RSTypeMemberUrl(key.type.asRMTypeUrl(), key.member) to mapValue.asRMOptionValue()
            }.associate { it })
        } else {
            RSOption.Value.RawMap(
                map { (key, value) ->
                    RSOption.Value.Raw(key.toString()) to RSOption.Value.Raw(value.toString()) }
                    .associate { it }
            )
        }
    } else {
        RSOption.Value.Raw(toString())
    }
}

internal fun Options.asRMOptions(): RSOptions {
    return RSOptions(map.map { (key, value) -> key.asRMOption(value) })
}

internal fun Type.asRMType(): RSType {
    return when (this) {
        is EnclosingType -> RSType.Enclosing(
            name = name,
            documentation = documentation,
            typeUrl = type.asRMTypeUrl(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            nestedExtends = nestedExtendList.map { it.asRMExtend() }
        )

        is EnumType -> RSType.Enum(
            name = name,
            constants = constants.map { it.asRMConstant() },
            documentation = documentation,
            options = options.asRMOptions(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            typeUrl = type.asRMTypeUrl(),
            nestedExtends = nestedExtendList.map { it.asRMExtend() },
        )

        is MessageType -> RSType.Message(
            name = name,
            documentation = documentation,
            fields = fields.map { it.asRMField() },
            oneOfs = oneOfs.map { it.asRMOneOf() },
            options = options.asRMOptions(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            nestedExtends = nestedExtendList.map { it.asRMExtend() },
            typeUrl = type.asRMTypeUrl(),
        )
    }
}

internal fun Extend.asRMExtend(): RSExtend {
    return RSExtend(
        typeUrl = type!!.asRMTypeUrl(),
        name = name,
        fields = fields.map { it.asRMField() },
        documentation = documentation,
    )
}

internal fun Rpc.asRMRpc(): RSRpc {
    return RSRpc(
        name = name,
        requestType = StreamableRMTypeUrl(requestStreaming, requestType!!.asRMTypeUrl()),
        responseType = StreamableRMTypeUrl(responseStreaming, responseType!!.asRMTypeUrl()),
        options = options.asRMOptions(),
        documentation = documentation,
    )
}

internal fun Service.asRMService(): RSService {
    return RSService(
        name = name,
        rpcs = rpcs.map { it.asRMRpc() },
        options = options.asRMOptions(),
        typeUrl = type.asRMTypeUrl(),
    )
}

internal fun ProtoFile.asRMFile(): RSFile {
    return RSFile(
        name = name(),
        packageName = RMPackageName(packageName!!),
        options = options.asRMOptions(),
        services = services.map { it.asRMService() },
        types = types.map { it.asRMType() },
        extends = extendList.map { it.asRMExtend() },
    )
}