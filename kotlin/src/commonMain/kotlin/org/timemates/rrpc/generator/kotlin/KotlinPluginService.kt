package org.timemates.rrpc.generator.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.PluginService
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.common.schema.RSFile
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.FileGenerator
import org.timemates.rrpc.generator.kotlin.adapter.metadata.CombinedFilesMetadataGenerator
import org.timemates.rrpc.generator.kotlin.options.*

public object KotlinPluginService : PluginService {
    override val options: List<OptionDescriptor> = listOf(
        GenerationOptions.KOTLIN_OUTPUT,
        GenerationOptions.KOTLIN_CLIENT_GENERATION,
        GenerationOptions.KOTLIN_SERVER_GENERATION,
        GenerationOptions.KOTLIN_TYPE_GENERATION,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME
    ).map { it.toOptionDescriptor() }

    @OptIn(NonPlatformSpecificAccess::class)
    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
    ): PluginSignal.RequestStatusChange = runCatching {
        withContext(Dispatchers.IO) {
            val options = KotlinPluginOptions(options)
            FileSystem.SYSTEM.deleteRecursively(options.output)

            val resolver = RSResolver(files)
            resolver.resolveAvailableFiles().filterNot {
                it.packageName.value.startsWith("google.protobuf") ||
                    it.packageName.value.startsWith("wire")
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

            PluginSignal.RequestStatusChange.Finished(
                message = "Kotlin Plugin generation finished, output: ${options.output}"
            )
        }
    }.getOrElse { exception ->
        PluginSignal.RequestStatusChange.Failed(
            message = exception.message ?: "Unknown error",
        )
    }

    override val description: String = """
        Kotlin Code Generator for rRPC: supports base client/server generation 
        as well as schema metadata generation.
        """.trimIndent()
}