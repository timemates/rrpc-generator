package app.timemate.rrpc.generator

import com.squareup.wire.schema.*
import app.timemate.rrpc.proto.schema.RSElementLocation
import app.timemate.rrpc.proto.schema.RSEnclosingType
import app.timemate.rrpc.proto.schema.RSEnum
import app.timemate.rrpc.proto.schema.RSEnumConstant
import app.timemate.rrpc.proto.schema.RSExtend
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.proto.schema.RSOneOf
import app.timemate.rrpc.proto.schema.RSOption
import app.timemate.rrpc.proto.schema.RSOptions
import app.timemate.rrpc.proto.schema.RSRpc
import app.timemate.rrpc.proto.schema.RSTypeMemberUrl
import app.timemate.rrpc.proto.schema.StreamableRSTypeUrl
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.proto.schema.value.RSPackageName
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSService
import app.timemate.rrpc.proto.schema.RSType
import app.timemate.rrpc.proto.schema.value.RSFieldLabel
import app.timemate.rrpc.proto.schema.value.LocationPath

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
            else -> error("Unable to convert scalar type '$this'.")
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
        label = when (label) {
            Field.Label.REPEATED -> RSFieldLabel.REPEATED
            Field.Label.ONE_OF -> RSFieldLabel.ONE_OF
            else -> RSFieldLabel.NONE
        },
        isExtension = isExtension,
        location = location.asRSElementLocation(),
        namespacesList = namespaces,
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
        is EnclosingType -> RSEnclosingType(
            name = name,
            documentation = documentation,
            typeUrl = type.asRSTypeUrl(),
            nestedTypes = nestedTypes.map { it.asRSType() },
            nestedExtends = nestedExtendList.map { it.asRSExtend() },
            location = location.asRSElementLocation(),
        )

        is EnumType -> RSEnum(
            name = name,
            constants = constants.map { it.asRSConstant() },
            documentation = documentation,
            options = options.asRSOptions(),
            nestedTypes = nestedTypes.map { it.asRSType() },
            typeUrl = type.asRSTypeUrl(),
            nestedExtends = nestedExtendList.map { it.asRSExtend() },
            location = location.asRSElementLocation(),
        )

        is MessageType -> RSMessage(
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
        location = location.asRSElementLocation(),
    )
}

internal fun Rpc.asRSRpc(): RSRpc {
    return RSRpc(
        name = name,
        requestType = StreamableRSTypeUrl(requestStreaming, requestType!!.asRSTypeUrl()),
        responseType = StreamableRSTypeUrl(responseStreaming, responseType!!.asRSTypeUrl()),
        options = options.asRSOptions(),
        documentation = documentation,
        location = location.asRSElementLocation(),
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
        packageName = packageName?.let { RSPackageName(it) } ?: RSPackageName.EMPTY,
        options = options.asRSOptions(),
        services = services.map { it.asRSService() },
        types = types.map { it.asRSType() },
        extends = extendList.map { it.asRSExtend() },
        location = location.asRSElementLocation(),
        imports = imports.map { LocationPath(it) },
    )
}

internal fun Location.asRSElementLocation(): RSElementLocation =
    RSElementLocation(
        basePath = LocationPath(value = base),
        relativePath = LocationPath(path),
        line = line.takeUnless { it == -1 },
        column = column.takeUnless { it == -1 },
    )