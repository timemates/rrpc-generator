package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.CodeBlock
import org.timemates.rrpc.codegen.schema.RSOption
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.RSType
import org.timemates.rrpc.codegen.schema.RSTypeMemberUrl
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.protoByteStringToByteArray

public object OptionValueGenerator {
    public fun generate(
        typeUrl: RSDeclarationUrl,
        value: RSOption.Value?,
        resolver: RSResolver,
    ): String {
        @Suppress("UNCHECKED_CAST")
        return when {
            typeUrl.isScalar || typeUrl == RSDeclarationUrl.STRING || typeUrl == RSDeclarationUrl.STRING_VALUE -> generateScalarOrString(
                typeUrl,
                value as RSOption.Value.Raw,
            )

            typeUrl.isWrapper -> generateWrapperValue(typeUrl, (value as RSOption.Value.MessageMap).map)
            typeUrl.isMap -> generateMap(typeUrl, (value as RSOption.Value.RawMap).map, resolver)
            else -> generateCustomType(typeUrl, value!!, resolver)
        }
    }

    private fun generateScalarOrString(
        protoType: RSDeclarationUrl,
        value: RSOption.Value.Raw,
    ): String {
        val value = value.string


        return when (protoType) {
            RSDeclarationUrl.STRING, RSDeclarationUrl.STRING_VALUE -> "\"${value}\""
            RSDeclarationUrl.BOOL -> value
            // We don't support special types such as fixed32, sfixed32 and sint32,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Int.
            RSDeclarationUrl.INT32, RSDeclarationUrl.FIXED32, RSDeclarationUrl.SFIXED32, RSDeclarationUrl.SINT32 -> value
            // We don't support special types such as fixed64, sfixed64 and sint64,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Long.
            RSDeclarationUrl.INT64, RSDeclarationUrl.FIXED64, RSDeclarationUrl.SFIXED64, RSDeclarationUrl.SINT64 -> "${value}L"
            RSDeclarationUrl.FLOAT -> "${value}f"
            RSDeclarationUrl.DOUBLE -> "${value}.toDouble()"
            RSDeclarationUrl.UINT32 -> "${value}.toUInt()"
            RSDeclarationUrl.UINT64 -> "${value}uL"
            RSDeclarationUrl.BYTES -> byteArrayToSourceCode(value.protoByteStringToByteArray())
            else -> error("Unsupported type")
        }
    }

    private fun generateWrapperValue(
        protoType: RSDeclarationUrl,
        value: Map<RSTypeMemberUrl, Any>,
    ): String {
        val element = value.values.first().toString()

        return when (protoType) {
            RSDeclarationUrl.STRING_VALUE -> "\"${element}\""
            RSDeclarationUrl.BOOL_VALUE -> element
            RSDeclarationUrl.INT32_VALUE -> element
            RSDeclarationUrl.INT64_VALUE -> "${element}L"
            RSDeclarationUrl.FLOAT_VALUE -> "${element}f"
            RSDeclarationUrl.DOUBLE -> "${element}.toDouble()"
            RSDeclarationUrl.UINT32_VALUE -> "${element}u"
            RSDeclarationUrl.UINT64_VALUE -> "${element}uL"
            else -> TODO("Unsupported for now: $protoType")
        }
    }

    private fun generateMap(
        typeUrl: RSDeclarationUrl,
        value: Map<RSOption.Value.Raw, RSOption.Value.Raw>,
        schema: RSResolver,
    ): String {
        requireNotNull(typeUrl.firstTypeArgument)
        requireNotNull(typeUrl.secondTypeArgument)

        return CodeBlock.builder()
            .add("mapOf(")
            .indent()
            .apply {
                value.entries.forEach { (key, value) ->
                    add(
                        "\n" + generate(typeUrl.firstTypeArgument!!, key, schema) +
                            " to " +
                            generate(typeUrl.secondTypeArgument!!, value, schema) + ","
                    )
                }
            }
            .unindent()
            .add(")")
            .toString()
    }

    private fun generateCustomType(
        typeUrl: RSDeclarationUrl,
        value: RSOption.Value?,
        schema: RSResolver,
    ): String {
        require(typeUrl != RSDeclarationUrl.ANY) { "google.protobuf.Any type is not supported." }
        val type = schema.resolveType(typeUrl)
        val className = typeUrl.asClassName(schema)

        return when (type) {
            is RSType.Message -> {
                return CodeBlock.builder()
                    .add("%T.create {", className)
                    .indent()
                    .apply {
                        @Suppress("UNCHECKED_CAST")
                        (value as Map<RSTypeMemberUrl, RSOption.Value>).entries.forEach { (key, value) ->
                            val field = type.field(key.memberName)!!
                            add("\n${field.name} = ${generate(field.typeUrl, value, schema)}")
                        }
                    }
                    .unindent()
                    .add("\n")
                    .add("}")
                    .build()
                    .toString()
            }

            is RSType.Enum -> CodeBlock.of("%T.%L", className, value).toString()
            is RSType.Enclosing -> "null"
            null -> error("Unable to resolve custom type for: $typeUrl.")
        }
    }

    private fun byteArrayToSourceCode(byteArray: ByteArray): String {
        return byteArray.joinToString(prefix = "byteArrayOf(", postfix = ")") { byte ->
            "0x${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
        }
    }
}