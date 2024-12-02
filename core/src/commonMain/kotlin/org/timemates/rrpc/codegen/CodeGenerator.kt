package org.timemates.rrpc.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.isPackageCyclesPermitted
import org.timemates.rrpc.codegen.configuration.protoInputs
import org.timemates.rrpc.codegen.plugin.PluginService
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.common.schema.RSFile

public class CodeGenerator(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val plugins: List<PluginService>,
) : PluginService {
    public companion object {
        public val BASE_OPTIONS: List<GenerationOption> = listOf(
            GenerationOptions.PROTOS_INPUT,
            GenerationOptions.PERMIT_PACKAGE_CYCLES,
        )
    }

    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>
    ): PluginSignal.RequestStatusChange {
        val schemaLoader = SchemaLoader(fileSystem)
        schemaLoader.permitPackageCycles = options.isPackageCyclesPermitted

        schemaLoader.initRoots(
            options.protoInputs.map { Location.get(it.toString()) }
        )

        val schema = schemaLoader.loadSchema()
        val files = schema.asRSResolver().resolveAvailableFiles().toList()

        val outputText = buildString {
            plugins.forEach { plugin ->
                when (val result = plugin.generateCode(options, files)) {
                    is PluginSignal.RequestStatusChange.Failed -> append("\t${plugin.name}: ${result.message}")
                    is PluginSignal.RequestStatusChange.Finished -> append("\t(!) ${plugin.name}: ${result.message}")
                }
            }
        }

        return PluginSignal.RequestStatusChange.Finished("Code generation is finished: \n$outputText")
    }

    override val options: List<OptionDescriptor> = BASE_OPTIONS.map { it.toOptionDescriptor() } + plugins.flatMap { it.options }

    override val name: String = "CodeGenerator"
    override val description: String = "Entry point for any generator's plugin."
}