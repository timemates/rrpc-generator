package org.timemates.rrpc.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import org.timemates.rrpc.codegen.configuration.*
import org.timemates.rrpc.codegen.plugin.PluginService
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor

public class CodeGenerator(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val plugins: List<PluginService>,
) {
    public companion object {
        public val BASE_OPTIONS: List<GenerationOption> = listOf(
            GenerationOptions.SOURCE_INPUT,
            GenerationOptions.CONTEXT_INPUT,
            GenerationOptions.PERMIT_PACKAGE_CYCLES,
        )
    }

    public fun generateCode(
        options: GenerationOptions,
    ): PluginSignal.RequestStatusChange = runBlocking {
        val schemaLoader = SchemaLoader(fileSystem)
        schemaLoader.permitPackageCycles = options.isPackageCyclesPermitted

        schemaLoader.initRoots(
            sourcePath = options.sourceInputs.map { Location.get(it.toFile().path) },
            protoPath = options.contextInputs.map { Location.get(it.toFile().path) }
        )

        val schema = schemaLoader.loadSchema()
        val files = schema.asRSResolver()
            .resolveAvailableFiles()
            .toList()

        val outputText = buildString {
            plugins.forEach { plugin ->
                when (val result = plugin.generateCode(options, files)) {
                    is PluginSignal.RequestStatusChange.Failed -> append("(!) ${plugin.name}: ${result.message}")
                    is PluginSignal.RequestStatusChange.Finished -> append("\t(OK) ${plugin.name}: ${result.message}")
                }
            }
        }

        PluginSignal.RequestStatusChange.Finished(outputText)
    }

    public val options: List<OptionDescriptor> =
        BASE_OPTIONS.map { it.toOptionDescriptor() } + plugins.flatMap { it.options }
}