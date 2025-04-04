package org.timemates.rrpc.generator.kotlin.adapter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.RSExtend
import org.timemates.rrpc.codegen.schema.RSOptions
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.sourceOnly
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.options.OptionGenerator

public object ExtendGenerator {
    public fun generateExtend(extend: RSExtend, resolver: RSResolver, topLevel: Boolean): List<PropertySpec> {
        return when (extend.typeUrl) {
            // we don't support for now options generation for anything except methods, files and services.
            RSOptions.FIELD_OPTIONS,
            RSOptions.MESSAGE_OPTIONS,
            RSOptions.ONEOF_OPTIONS,
            RSOptions.ENUM_OPTIONS,
            RSOptions.ENUM_VALUE_OPTIONS,
                -> emptyList()

            RSOptions.METHOD_OPTIONS,
            RSOptions.FILE_OPTIONS,
            RSOptions.SERVICE_OPTIONS,
                -> extend.fields.mapNotNull {
                    if (it.options.sourceOnly)
                        return@mapNotNull null

                OptionGenerator.generateOption(
                    field = it,
                    type = getClassNameFromExtendType(extend.typeUrl),
                    resolver = resolver,
                    topLevel = true,
                )
            }

            else -> throw GenerationException("Extending messages are not supported.")
        }
    }

    private fun getClassNameFromExtendType(type: RSDeclarationUrl): ClassName {
        return when (type) {
            RSOptions.METHOD_OPTIONS -> LibClassNames.Option.RPC
            RSOptions.SERVICE_OPTIONS -> LibClassNames.Option.Service
            RSOptions.FILE_OPTIONS -> LibClassNames.Option.File
            else -> throw GenerationException("Should not reach this state.")
        }
    }
}