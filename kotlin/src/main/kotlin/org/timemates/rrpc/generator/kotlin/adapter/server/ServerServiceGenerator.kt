package org.timemates.rrpc.generator.kotlin.adapter.server

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.generator.kotlin.KGFileContext

public object ServerServiceGenerator {
    public fun generateService(
        service: RSService,
        resolver: RSResolver,
        context: KGFileContext,
    ): TypeSpec {
        return TypeSpec.classBuilder(service.name)
            .addModifiers(KModifier.ABSTRACT)
            .addSuperinterface(LibClassNames.RRpcServerService)
            .addProperty(
                ServerMetadataGenerator.generateMetadata(service, resolver, context)
            )
            .addFunctions(service.rpcs.map { ServerRpcGenerator.generateRpc(it, resolver) })
            .build()
    }
}