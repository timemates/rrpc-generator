package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

internal object BuiltinsGenerator {
    fun generateBuiltin(incoming: RSDeclarationUrl): TypeName {
        return when (incoming) {
            RSDeclarationUrl.STRING -> STRING
            RSDeclarationUrl.BOOL -> BOOLEAN
            RSDeclarationUrl.INT32, RSDeclarationUrl.SINT32, RSDeclarationUrl.FIXED32, RSDeclarationUrl.SFIXED32 -> INT
            RSDeclarationUrl.INT64, RSDeclarationUrl.SINT64, RSDeclarationUrl.FIXED64, RSDeclarationUrl.SFIXED64 -> LONG
            RSDeclarationUrl.BYTES -> BYTE_ARRAY
            RSDeclarationUrl.FLOAT -> FLOAT
            RSDeclarationUrl.UINT32 -> U_INT
            RSDeclarationUrl.UINT64 -> U_LONG
            RSDeclarationUrl.DOUBLE -> DOUBLE
            RSDeclarationUrl.STRING_VALUE -> LibClassNames.Wrappers.STRING_VALUE
            RSDeclarationUrl.INT32_VALUE -> LibClassNames.Wrappers.INT32_VALUE
            RSDeclarationUrl.INT64_VALUE -> LibClassNames.Wrappers.INT64_VALUE
            RSDeclarationUrl.FLOAT_VALUE -> LibClassNames.Wrappers.FLOAT_VALUE
            RSDeclarationUrl.DOUBLE_VALUE -> LibClassNames.Wrappers.DOUBLE_VALUE
            RSDeclarationUrl.UINT32_VALUE -> LibClassNames.Wrappers.UINT32_VALUE
            RSDeclarationUrl.UINT64_VALUE -> LibClassNames.Wrappers.UINT64_VALUE
            RSDeclarationUrl.BOOL_VALUE -> LibClassNames.Wrappers.BOOL_VALUE
            RSDeclarationUrl.BYTES_VALUE -> LibClassNames.Wrappers.BYTES_VALUE
            else -> error("Unsupported protobuf type $incoming")
        }
    }

}