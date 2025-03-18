package org.timemates.rrpc.generator.kotlin.adapter

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName

public object OptionGenerator {
    public fun generateOption(field: RSField, type: ClassName, resolver: RSResolver): PropertySpec =
        PropertySpec.builder(field.name, type.parameterizedBy(toKotlinTypeName(field.typeUrl, resolver)))
            .addKdoc(field.documentation?.replace("%", "%%").orEmpty())
            .receiver(type.nestedClass("Companion"))
            .delegate("lazy·{·%T(%S, %L)·}", type, field.name, field.tag)
            .build()

    private fun toKotlinTypeName(type: RSDeclarationUrl, resolver: RSResolver): TypeName {
        return when (type) {
            RSDeclarationUrl.STRING, RSDeclarationUrl.STRING_VALUE -> return STRING
            RSDeclarationUrl.DOUBLE, RSDeclarationUrl.DOUBLE_VALUE -> return DOUBLE
            RSDeclarationUrl.BYTES, RSDeclarationUrl.BYTES_VALUE -> return BYTE_ARRAY
            RSDeclarationUrl.BOOL, RSDeclarationUrl.BOOL_VALUE -> return BOOLEAN
            RSDeclarationUrl.UINT64, RSDeclarationUrl.UINT64_VALUE -> return U_LONG
            RSDeclarationUrl.UINT32, RSDeclarationUrl.UINT32_VALUE -> return U_INT
            RSDeclarationUrl.INT32, RSDeclarationUrl.INT32_VALUE, RSDeclarationUrl.SFIXED32, RSDeclarationUrl.FIXED32 -> return INT
            RSDeclarationUrl.INT64, RSDeclarationUrl.INT64_VALUE, RSDeclarationUrl.SFIXED64, RSDeclarationUrl.FIXED64 -> LONG
            RSDeclarationUrl.FLOAT, RSDeclarationUrl.FLOAT_VALUE -> return FLOAT
            else -> type.asClassName(resolver)
        }
    }
}