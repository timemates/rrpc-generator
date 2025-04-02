package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.U_LONG
import org.timemates.rrpc.codegen.annotation.ExperimentalGeneratorFunctionality
import org.timemates.rrpc.codegen.option.ExtensionGenerationStrategy
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.extensionGenerationStrategy
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName

public object OptionGenerator {
    @OptIn(ExperimentalGeneratorFunctionality::class)
    public fun generateOption(
        field: RSField,
        type: ClassName,
        resolver: RSResolver,
        topLevel: Boolean,
    ): PropertySpec {
        // the decision is dependent on context, please refer to the documentation of strategy
        val strategy = field.options.extensionGenerationStrategy
            ?: if (topLevel) ExtensionGenerationStrategy.EXTENSION else ExtensionGenerationStrategy.REGULAR

        return PropertySpec.Companion.builder(field.name, type.parameterizedBy(toKotlinTypeName(field.typeUrl, resolver)))
            .addKdoc(field.documentation?.replace("%", "%%").orEmpty())
            .apply {
                if (strategy == ExtensionGenerationStrategy.EXTENSION)
                    receiver(type.nestedClass("Companion"))
            }
            .delegate(
                format = "lazy·{·%T(%S, %L)·}",
                type,
                field.name,
                field.tag
            )
            .build()
    }

    private fun toKotlinTypeName(type: RSDeclarationUrl, resolver: RSResolver): TypeName {
        return when (type) {
            RSDeclarationUrl.Companion.STRING, RSDeclarationUrl.Companion.STRING_VALUE -> return STRING
            RSDeclarationUrl.Companion.DOUBLE, RSDeclarationUrl.Companion.DOUBLE_VALUE -> return DOUBLE
            RSDeclarationUrl.Companion.BYTES, RSDeclarationUrl.Companion.BYTES_VALUE -> return BYTE_ARRAY
            RSDeclarationUrl.Companion.BOOL, RSDeclarationUrl.Companion.BOOL_VALUE -> return BOOLEAN
            RSDeclarationUrl.Companion.UINT64, RSDeclarationUrl.Companion.UINT64_VALUE -> return U_LONG
            RSDeclarationUrl.Companion.UINT32, RSDeclarationUrl.Companion.UINT32_VALUE -> return U_INT
            RSDeclarationUrl.Companion.INT32, RSDeclarationUrl.Companion.INT32_VALUE, RSDeclarationUrl.Companion.SFIXED32, RSDeclarationUrl.Companion.FIXED32 -> return INT
            RSDeclarationUrl.Companion.INT64, RSDeclarationUrl.Companion.INT64_VALUE, RSDeclarationUrl.Companion.SFIXED64, RSDeclarationUrl.Companion.FIXED64 -> LONG
            RSDeclarationUrl.Companion.FLOAT, RSDeclarationUrl.Companion.FLOAT_VALUE -> return FLOAT
            else -> type.asClassName(resolver)
        }
    }
}