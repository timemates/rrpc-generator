package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.generator.kotlin.adapter.internal.LibClassNames
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.RSType

internal object EnumTypeGenerator {
    fun generateEnum(incoming: RSType.Enum, schema: RSResolver): TypeSpec {
        val nested = incoming.nestedTypes.mapNotNull { TypeGenerator.generateType(it, schema) }

        return TypeSpec.enumBuilder(incoming.name)
            .addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
            .addAnnotation(PoetAnnotations.Serializable)
            .apply {
                incoming.constants.forEach { constant ->
                    addEnumConstant(
                        constant.name,
                        TypeSpec.anonymousClassBuilder()
                            .addKdoc(constant.documentation?.replace("%", "%%").orEmpty())
                            .addAnnotation(PoetAnnotations.ProtoNumber(constant.tag))
                            .build()
                    )
                }
            }
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addProperty(
                        PropertySpec.builder("Default", ClassName("", incoming.name))
                            .initializer(incoming.constants.first { it.tag == 0 }.name)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}