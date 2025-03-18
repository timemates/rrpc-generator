package org.timemates.rrpc.codegen

import com.squareup.wire.schema.*
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.codegen.schema.*
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.codegen.schema.value.RSPackageName
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.value.LocationPath

internal fun ProtoType.asRSTypeUrl(): RSDeclarationUrl {
    return when {
        isMap -> RSDeclarationUrl("map<${keyType!!.asRSTypeUrl()}, ${valueType!!.asRSTypeUrl()}>")
        isScalar -> when (this) {
            ProtoType.BOOL -> RSDeclarationUrl.BOOL
            ProtoType.BYTES -> RSDeclarationUrl.BYTES
            ProtoType.DOUBLE -> RSDeclarationUrl.DOUBLE
            ProtoType.FLOAT -> RSDeclarationUrl.FLOAT
            ProtoType.FIXED32 -> RSDeclarationUrl.FIXED32
            ProtoType.FIXED64 -> RSDeclarationUrl.FIXED64
            ProtoType.INT32 -> RSDeclarationUrl.INT32
            ProtoType.INT64 -> RSDeclarationUrl.INT64
            ProtoType.SFIXED32 -> RSDeclarationUrl.SFIXED32
            ProtoType.SFIXED64 -> RSDeclarationUrl.SFIXED64
            ProtoType.SINT32 -> RSDeclarationUrl.SINT32
            ProtoType.SINT64 -> RSDeclarationUrl.SINT64
            ProtoType.STRING -> RSDeclarationUrl.STRING
            ProtoType.UINT32 -> RSDeclarationUrl.UINT32
            ProtoType.UINT64 -> RSDeclarationUrl.UINT64
            ProtoType.ANY -> RSDeclarationUrl.ANY
            ProtoType.DURATION -> RSDeclarationUrl.DURATION
            ProtoType.TIMESTAMP -> RSDeclarationUrl.TIMESTAMP
            ProtoType.EMPTY -> RSDeclarationUrl.EMPTY
            ProtoType.STRUCT_MAP -> RSDeclarationUrl.STRUCT_MAP
            ProtoType.STRUCT_LIST -> RSDeclarationUrl.STRUCT_LIST
            ProtoType.STRUCT_VALUE -> RSDeclarationUrl.STRUCT_VALUE
            ProtoType.STRUCT_NULL -> RSDeclarationUrl.STRUCT_NULL
            ProtoType.DOUBLE_VALUE -> RSDeclarationUrl.DOUBLE_VALUE
            ProtoType.FLOAT_VALUE -> RSDeclarationUrl.FLOAT_VALUE
            ProtoType.INT32_VALUE -> RSDeclarationUrl.INT32_VALUE
            ProtoType.INT64_VALUE -> RSDeclarationUrl.INT64_VALUE
            ProtoType.UINT32_VALUE -> RSDeclarationUrl.UINT32_VALUE
            ProtoType.UINT64_VALUE -> RSDeclarationUrl.UINT64_VALUE
            ProtoType.BOOL_VALUE -> RSDeclarationUrl.BOOL_VALUE
            ProtoType.BYTES_VALUE -> RSDeclarationUrl.BYTES_VALUE
            else -> throw GenerationException("Unable to convert scalar type '$this'.")
        }

        else -> RSDeclarationUrl(typeUrl!!)
    }
}

internal fun Field.asRSField(): RSField {
    return RSField(
        tag = tag,
        name = name,
        options = options.asRSOptions(),
        documentation = documentation,
        typeUrl = type!!.asRSTypeUrl(),
        isRepeated = isRepeated,
        isInOneOf = isOneOf,
        isExtension = isExtension,
        location = location.asRSElementLocation(),
    )
}

internal fun OneOf.asRSOneOf(): RSOneOf {
    return RSOneOf(
        name = name,
        fields = fields.map { it.asRSField() },
        documentation = documentation,
        options = options.asRSOptions(),
        location = location.asRSElementLocation(),
    )
}

internal fun EnumConstant.asRSConstant(): RSEnumConstant {
    return RSEnumConstant(
        name = name,
        tag = tag,
        options = options.asRSOptions(),
        documentation = documentation,
        location = location.asRSElementLocation(),
    )
}

internal fun ProtoMember.asRSOption(value: Any?): RSOption {
    return RSOption(
        name = simpleName,
        fieldUrl = RSTypeMemberUrl(type.asRSTypeUrl(), member),
        value = value?.asRSOptionValue(),
    )
}

private fun Any.asRSOptionValue(): RSOption.Value {
    return if (this is Map<*, *>) {
        val firstKey = keys.firstOrNull()

        if (firstKey is ProtoMember) {
            @Suppress("UNCHECKED_CAST")
            RSOption.Value.MessageMap((this as Map<ProtoMember, Any>).map { (key, mapValue) ->
                RSTypeMemberUrl(key.type.asRSTypeUrl(), key.member) to mapValue.asRSOptionValue()
            }.associate { it })
        } else {
            RSOption.Value.RawMap(
                map { (key, value) ->
                    RSOption.Value.Raw(key.toString()) to RSOption.Value.Raw(value.toString())
                }
                    .associate { it }
            )
        }
    } else {
        RSOption.Value.Raw(toString())
    }
}

internal fun Options.asRSOptions(): RSOptions {
    return RSOptions(map.map { (key, value) -> key.asRSOption(value) })
}

internal fun Type.asRSType(): RSType {
    return when (this) {
        is EnclosingType -> RSType.Enclosing(
            name = name,
            documentation = documentation,
            typeUrl = type.asRSTypeUrl(),
            nestedTypes = nestedTypes.map { it.asRSType() },
            nestedExtends = nestedExtendList.map { it.asRSExtend() },
            location = location.asRSElementLocation(),
        )

        is EnumType -> RSType.Enum(
            name = name,
            constants = constants.map { it.asRSConstant() },
            documentation = documentation,
            options = options.asRSOptions(),
            nestedTypes = nestedTypes.map { it.asRSType() },
            typeUrl = type.asRSTypeUrl(),
            nestedExtends = nestedExtendList.map { it.asRSExtend() },
            location = location.asRSElementLocation(),
        )

        is MessageType -> RSType.Message(
            name = name,
            documentation = documentation,
            fields = fields.map { it.asRSField() },
            oneOfs = oneOfs.map { it.asRSOneOf() },
            options = options.asRSOptions(),
            nestedTypes = nestedTypes.map { it.asRSType() },
            nestedExtends = nestedExtendList.map { it.asRSExtend() },
            typeUrl = type.asRSTypeUrl(),
            location = location.asRSElementLocation(),
        )
    }
}

internal fun Extend.asRSExtend(): RSExtend {
    return RSExtend(
        typeUrl = type!!.asRSTypeUrl(),
        name = name,
        fields = fields.map { it.asRSField() },
        documentation = documentation,
    )
}

internal fun Rpc.asRSRpc(): RSRpc {
    return RSRpc(
        name = name,
        requestType = StreamableRSTypeUrl(requestStreaming, requestType!!.asRSTypeUrl()),
        responseType = StreamableRSTypeUrl(responseStreaming, responseType!!.asRSTypeUrl()),
        options = options.asRSOptions(),
        documentation = documentation,
    )
}

internal fun Service.asRSService(): RSService {
    return RSService(
        name = name,
        rpcs = rpcs.map { it.asRSRpc() },
        options = options.asRSOptions(),
        typeUrl = type.asRSTypeUrl(),
        location = location.asRSElementLocation(),
    )
}

internal fun ProtoFile.asRSFile(): RSFile {
    return RSFile(
        name = name(),
        packageName = packageName?.let { RSPackageName(it) },
        options = options.asRSOptions(),
        services = services.map { it.asRSService() },
        types = types.map { it.asRSType() },
        extends = extendList.map { it.asRSExtend() },
        location = location.asRSElementLocation(),
        imports = imports.map { LocationPath(it) },
    )
}

internal fun Location.asRSElementLocation(): RSElementLocation =
    RSElementLocation(LocationPath(base), LocationPath(path), line.takeUnless { it != -1 }, column.takeUnless { it != -1 })