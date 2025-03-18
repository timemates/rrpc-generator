package org.timemates.rrpc.generator.kotlin.adapter.internal.ext

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.Language
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.StreamableRSTypeUrl
import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@OptIn(NonPlatformSpecificAccess::class)
internal fun RSDeclarationUrl.asClassName(resolver: RSResolver): ClassName {
    return when (this) {
        RSDeclarationUrl.ANY -> ClassName("com.google.protobuf", "ProtoAny")
        RSDeclarationUrl.TIMESTAMP -> ClassName("com.google.protobuf", "ProtoTimestamp")
        RSDeclarationUrl.DURATION -> ClassName("com.google.protobuf", "ProtoDuration")
        RSDeclarationUrl.STRUCT_MAP -> ClassName("com.google.protobuf", "ProtoStruct")
        RSDeclarationUrl.EMPTY -> ClassName("com.google.protobuf", "ProtoEmpty")
        RSDeclarationUrl.STRING_VALUE -> LibClassNames.Wrappers.STRING_VALUE
        RSDeclarationUrl.INT32_VALUE -> LibClassNames.Wrappers.INT32_VALUE
        RSDeclarationUrl.INT64_VALUE -> LibClassNames.Wrappers.INT64_VALUE
        RSDeclarationUrl.FLOAT_VALUE -> LibClassNames.Wrappers.FLOAT_VALUE
        RSDeclarationUrl.DOUBLE_VALUE -> LibClassNames.Wrappers.DOUBLE_VALUE
        RSDeclarationUrl.UINT32_VALUE -> LibClassNames.Wrappers.UINT32_VALUE
        RSDeclarationUrl.UINT64_VALUE -> LibClassNames.Wrappers.UINT64_VALUE
        RSDeclarationUrl.BOOL_VALUE -> LibClassNames.Wrappers.BOOL_VALUE
        RSDeclarationUrl.BYTES_VALUE -> LibClassNames.Wrappers.BYTES_VALUE
        else -> {
            val file = resolver.resolveFileOf(this) ?: return ClassName(enclosingTypeOrPackage ?: "", simpleName)

            val packageName = file.platformPackageName(Language.KOTLIN)?.value
            val enclosingName: String = (enclosingTypeOrPackage?.replace(file.packageName?.value.orEmpty(), "") ?: "")
                .replace("..", ".")

            ClassName(packageName ?: "", enclosingName.split(".").filterNot { it.isBlank() } + simpleName)
        }
    }
}

internal fun StreamableRSTypeUrl.asClassName(resolver: RSResolver): TypeName {
    return when (isStreaming) {
        true -> LibClassNames.Flow(type.asClassName(resolver))
        false -> type.asClassName(resolver)
    }
}

@OptIn(NonPlatformSpecificAccess::class)
internal fun RSDeclarationUrl.qualifiedName(resolver: RSResolver): String {
    val packageName = resolver.resolveFileOf(this)?.packageName?.value?.plus(".") ?: ""

    return packageName + simpleName
}