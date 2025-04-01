package org.timemates.rrpc.generator.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.logging.RLogger
import org.timemates.rrpc.codegen.plugin.GenerationPluginService
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.codegen.schema.RSFile
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.FileGenerator
import org.timemates.rrpc.generator.kotlin.adapter.metadata.CombinedFilesMetadataGenerator
import org.timemates.rrpc.generator.kotlin.options.*

public object KotlinPluginService : GenerationPluginService {
    override val options: List<OptionDescriptor> = listOf(
        GenerationOptions.KOTLIN_CLIENT_GENERATION,
        GenerationOptions.KOTLIN_SERVER_GENERATION,
        GenerationOptions.KOTLIN_TYPE_GENERATION,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME
    ).map { it.toOptionDescriptor() }

    override val name: String = "rrpc-kotlin-gen"

    @OptIn(NonPlatformSpecificAccess::class)
    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
        logger: RLogger,
    ): Unit = withContext(Dispatchers.IO) {
        val options = KotlinPluginOptions(options)

        FileSystem.SYSTEM.deleteRecursively(options.output)
        FileSystem.SYSTEM.createDirectories(options.output)

        if (options.isServerGenerationEnabled)
            logger.debug("Configured to generate server stubs.")
        else logger.debug("Configured not to generate server stubs.")

        if (options.isClientGenerationEnabled)
            logger.debug("Configured to generate client-specific code.")
        else logger.debug("Configured not to generate client-specific code.")

        if (!options.isTypesGenerationEnabled)
            logger.debug("Configured not to generate proto types.")

        val resolver = RSResolver(files)
        resolver.resolveAvailableFiles().filterNot {
            it.packageName?.value?.startsWith("google.protobuf") == true ||
                it.packageName?.value?.startsWith("wire") == true
        }.map { file ->
            FileGenerator.generateFile(
                resolver = resolver,
                file = file,
                clientGeneration = options.isClientGenerationEnabled,
                serverGeneration = options.isServerGenerationEnabled,
            )
        }.forEach { spec ->
            spec.writeTo(directory = options.output.toNioPath())
        }

        if (options.metadataGeneration) {
            CombinedFilesMetadataGenerator.generate(
                name = options.metadataScopeName,
                resolver = resolver,
            ).writeTo(options.output.toNioPath())
        }
    }

    override val description: String = """
        Kotlin Code Generator for rRPC: supports base client/server generation 
        as well as schema metadata generation.
        """.trimIndent()
}