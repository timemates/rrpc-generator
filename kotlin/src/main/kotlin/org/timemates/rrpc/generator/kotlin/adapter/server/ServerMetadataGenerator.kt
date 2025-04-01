package org.timemates.rrpc.generator.kotlin.adapter.server

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import org.timemates.rrpc.codegen.schema.RSOptions
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.codegen.schema.isRequestChannel
import org.timemates.rrpc.codegen.schema.isRequestResponse
import org.timemates.rrpc.codegen.schema.isRequestStream
import org.timemates.rrpc.codegen.schema.kotlinName
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.KGFileContext
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline
import org.timemates.rrpc.generator.kotlin.adapter.options.RawOptionsCodeGeneration

public object ServerMetadataGenerator {
    public fun generateMetadata(
        service: RSService,
        resolver: RSResolver,
        context: KGFileContext,
    ): PropertySpec {
        return PropertySpec.builder("descriptor", LibClassNames.ServiceDescriptor)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(
                buildCodeBlock {
                    add("%T(", LibClassNames.ServiceDescriptor)
                    newline()
                    indent()
                    add("name = %S,", service.name)
                    newline()
                    add("procedures = listOf(")
                    indent()

                    service.rpcs.forEach { rpc ->
                        val requestType = rpc.requestType.type.asClassName(resolver)
                        val responseType = rpc.responseType.type.asClassName(resolver)

                        val type = when {
                            rpc.isRequestResponse -> LibClassNames.ProcedureDescriptor.requestResponse
                            rpc.isRequestStream -> LibClassNames.ProcedureDescriptor.requestStream
                            rpc.isRequestChannel -> LibClassNames.ProcedureDescriptor.requestChannel
                            else -> error("Unsupported type of request for ${service.name}#${rpc.name}")
                        }

                        newline()
                        add("%T(", type)
                        newline()
                        indent()
                        @OptIn(NonPlatformSpecificAccess::class)
                        add("name = %S", rpc.name)
                        newline(",")
                        add("inputSerializer = %T.serializer()", requestType)
                        newline(",")
                        add("outputSerializer = %T.serializer()", responseType)
                        newline(",")
                        add("procedure = { context, data -> %L(context, data) }", rpc.kotlinName)
                        newline(",")

                        add("options = ")
                        add(RawOptionsCodeGeneration.generate(rpc.options, resolver, RSOptions.METHOD_OPTIONS, context))
                        newline(before = ",")
                        unindent()
                        add("),")
                    }
                    unindent()
                    newline()
                    add("),")
                    newline()
                    add("options = ")
                    add(RawOptionsCodeGeneration.generate(service.options, resolver, RSOptions.SERVICE_OPTIONS, context))
                    newline(before = ",")
                    unindent()
                    add(")")
                }
            )
            .build()
    }
}