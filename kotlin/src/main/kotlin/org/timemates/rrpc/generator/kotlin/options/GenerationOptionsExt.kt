package org.timemates.rrpc.generator.kotlin.options

import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.OptionTypeKind
import org.timemates.rrpc.codegen.configuration.SingleGenerationOption

public val GenerationOptions.Companion.KOTLIN_SERVER_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "server_generation",
        description = "Indicates whether server stubs should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrictOrNull() != false }
    )
}

public val GenerationOptions.Companion.KOTLIN_CLIENT_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "client_generation",
        description = "Indicates whether client stubs should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrictOrNull() != false }
    )
}

public val GenerationOptions.Companion.KOTLIN_TYPE_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "type_generation",
        description = "Indicates whether data types should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrictOrNull() != false }
    )
}

public val GenerationOptions.Companion.METADATA_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "metadata_generation",
        description = "Specifies whether metadata should be generated.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrictOrNull() == true }
    )
}

public val GenerationOptions.Companion.METADATA_SCOPE_NAME: SingleGenerationOption<String> by lazy {
    GenerationOption.single(
        name = "metadata_scope_name",
        description = "Specifies the scope name for metadata generation. If not specified and metadata generation is enabled, it will be global-scoped.",
        valueKind = OptionTypeKind.Text,
        constructor = { it }
    )
}