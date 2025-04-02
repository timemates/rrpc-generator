package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import org.timemates.rrpc.codegen.exception.UnresolvableFileException
import org.timemates.rrpc.codegen.exception.UnresolvableReferenceException
import org.timemates.rrpc.codegen.schema.RSOptions
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.sourceOnly
import org.timemates.rrpc.codegen.schema.kotlinPackage
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.codegen.schema.value.RSPackageName
import org.timemates.rrpc.generator.kotlin.KGFileContext
import org.timemates.rrpc.generator.kotlin.adapter.internal.ImportRequirement
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object RawOptionsCodeGeneration {
    fun generate(
        options: RSOptions,
        resolver: RSResolver,
        optionsType: RSDeclarationUrl,
        context: KGFileContext,
    ): CodeBlock = buildCodeBlock {
        val options = options.list.mapNotNull { option ->
            val field = resolver.resolveField(option.fieldUrl)
                ?: throw UnresolvableReferenceException(option.fieldUrl, context.location)

            if (field.options.sourceOnly)
                return@mapNotNull null

            option to field
        }.associate { it }

        add("%T(", LibClassNames.OptionsWithValue)

        if (options.isEmpty()) {
            add("emptyMap())")
            return@buildCodeBlock
        }

        newline()
        indent()

        options.forEach { (option, field) ->
            val type = field.typeUrl

            context.addImport(
                ImportRequirement(
                    packageName = (
                        resolver.resolveFileAt(field.location)
                            ?: throw UnresolvableFileException(field.location)
                        ).kotlinPackage ?: RSPackageName.EMPTY,
                    simpleNames = listOf(field.name),
                )
            )


            add(
                format = "%T.${option.name} to ",
                when (optionsType) {
                    RSOptions.METHOD_OPTIONS -> LibClassNames.Option.RPC
                    RSOptions.SERVICE_OPTIONS -> LibClassNames.Option.Service
                    else -> error("Unsupported type of option: ${field.typeUrl}")
                }
            )
            add(OptionValueGenerator.generate(type, option.value, resolver))
            newline(before = ",")
        }

        unindent()
        add(")")
    }
}