package org.timemates.rrpc.generator.kotlin.adapter.types.message

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.types.TypeGenerator

internal object MessageCompanionObjectGenerator {
    fun generateCompanionObject(
        className: ClassName,
        nested: List<TypeGenerator.Result>,
        generateCreateFun: Boolean,
        typeUrl: RSDeclarationUrl,
    ): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addSuperinterface(LibClassNames.ProtoTypeDefinition(className))
            .addProperty(
                PropertySpec.builder("Default", className)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%T()", className)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("url", STRING)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", typeUrl.value)
                    .build()
            )
            .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
            .apply {
                if (generateCreateFun) {
                    addFunction(
                        FunSpec.builder("create")
                            .addModifiers(KModifier.INLINE)
                            .addParameter(
                                name = "block",
                                type = LambdaTypeName.get(ClassName("", "DSLBuilder"), returnType = UNIT)
                            )
                            .addCode("return DSLBuilder().apply(block).build()", className)
                            .returns(className)
                            .build()

                    )
                }
            }
            .build()
    }
}