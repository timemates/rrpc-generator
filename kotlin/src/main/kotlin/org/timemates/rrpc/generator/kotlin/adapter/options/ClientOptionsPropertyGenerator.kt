package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.RSOptions
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.sourceOnly
import org.timemates.rrpc.codegen.schema.kotlinPackage
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline
import org.timemates.rrpc.generator.kotlin.adapter.internal.ImportRequirement

public object ClientOptionsPropertyGenerator {
    public data class Result(
        val property: PropertySpec,
        val imports: List<ImportRequirement>,
    )

    public fun generate(
        optionsMap: Map<String, RSOptions>,
        resolver: RSResolver,
    ): Result {
        val imports = mutableListOf<ImportRequirement>()

        val code = buildCodeBlock {
            if (optionsMap.isEmpty()) {
                add("RPCsOptions.EMPTY")
                return@buildCodeBlock
            }
            add("RPCsOptions(")
            indent()

            optionsMap.filter { it.value.list.isNotEmpty() }.forEach { (rpc, options) ->
                add("\n%S to %T(", rpc, LibClassNames.RPCsOptions)
                newline()
                indent()
                add("mapOf(")
                newline()
                indent()
                options.list.forEach { option ->
                    val field = resolver.resolveField(option.fieldUrl)!!

                    if (field.options.sourceOnly)
                        return@forEach

                    val type = field.typeUrl

                    resolver.resolveFileOf(type)
                        ?.kotlinPackage
                        ?.let {
                            imports.add(ImportRequirement(it, listOf(field.name)))
                        }

                    add("%T.${option.name} to ", LibClassNames.Option.RPC)
                    add(OptionValueGenerator.generate(type, option.value, resolver))
                    add(",\n")
                }
                unindent()
                add(")")
                unindent()
                newline()
                add("),")
            }
            newline()
            unindent()
            add(")")
        }

        return Result(
            property = PropertySpec.builder("rpcsOptions", LibClassNames.RPCsOptions)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(code)
                .build(),
            imports = imports,
        )
    }
}