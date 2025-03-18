package org.timemates.rrpc.generator.kotlin.adapter.types

import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

internal object TypeDefaultValueGenerator {
    fun generateTypeDefault(field: RSField): String {
        if (field.isRepeated)
            return "emptyList()"

        return when (field.typeUrl) {
            RSDeclarationUrl.INT32,
            RSDeclarationUrl.INT64,
            RSDeclarationUrl.DURATION,
            RSDeclarationUrl.FIXED32,
            RSDeclarationUrl.FIXED64,
            RSDeclarationUrl.SFIXED32,
            RSDeclarationUrl.SFIXED64,
            RSDeclarationUrl.SINT32,
            RSDeclarationUrl.SINT64,
                -> "0"

            RSDeclarationUrl.UINT32, RSDeclarationUrl.UINT64 -> "0u"
            RSDeclarationUrl.STRING -> "\"\""
            RSDeclarationUrl.BOOL -> "false"
            RSDeclarationUrl.BYTES -> "byteArrayOf()"
            RSDeclarationUrl.DOUBLE -> "0.0"
            RSDeclarationUrl.FLOAT -> "0.0f"
            else -> "null"
        }
    }

}