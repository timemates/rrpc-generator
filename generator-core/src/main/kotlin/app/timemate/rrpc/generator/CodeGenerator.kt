package app.timemate.rrpc.generator

import app.timemate.rrpc.generator.plugin.api.GenerationOption
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.generator.plugin.api.GenerationPluginService
import app.timemate.rrpc.generator.plugin.api.PluginService
import app.timemate.rrpc.generator.plugin.api.ProcessorPluginService
import app.timemate.rrpc.generator.plugin.api.communication.OptionDescriptor
import app.timemate.rrpc.generator.plugin.api.communication.toOptionDescriptor
import app.timemate.rrpc.generator.plugin.api.contextInputs
import app.timemate.rrpc.generator.plugin.api.isPackageCyclesPermitted
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.generator.plugin.api.sourceInputs
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import kotlinx.serialization.protobuf.ProtoType
import okio.FileSystem

public class CodeGenerator(
    private val fileSystem: FileSystem = FileSystem.Companion.SYSTEM,
    private val plugins: List<PluginService>,
) {
    public companion object {
        public val BASE_OPTIONS: List<GenerationOption> = listOf(
            GenerationOptions.Companion.SOURCE_INPUT,
            GenerationOptions.Companion.CONTEXT_INPUT,
            GenerationOptions.Companion.PERMIT_PACKAGE_CYCLES,
        )
    }

    public suspend fun generateCode(
        options: GenerationOptions,
        loggerFactory: (pluginName: String) -> RLogger,
    ) {
        options[GenerationOptions.GEN_OUTPUT] ?: error("gen_output option is required")

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

        plugins.fold(files) { files, plugin ->
            val pluginOptionPrefix = "${plugin.name}:"

            // scope options for plugin-specific and general that might be useful for generator
            val pluginOptions = GenerationOptions.create {
                options.raw.forEach { key, value ->
                    if (key.startsWith(pluginOptionPrefix)) {
                        if (value is Collection<*>) {
                            value.forEach { value ->
                                rawAppend(key.substringAfter(pluginOptionPrefix), value.toString())
                            }
                        } else {
                            rawSet(key.substringAfter(pluginOptionPrefix), value.toString())
                        }
                    }
                }

                // might be useful for generators, like for go, because it must fail if package cycles are permitted
                set(GenerationOptions.PERMIT_PACKAGE_CYCLES, options[GenerationOptions.Companion.PERMIT_PACKAGE_CYCLES]?.toString() ?: "false")
                set(GenerationOptions.GEN_OUTPUT, options[GenerationOptions.Companion.GEN_OUTPUT]!!.toString() + "/${plugin.name}")
            }

            val logger = loggerFactory(plugin.name)

            return@fold when (plugin) {
                is ProcessorPluginService -> plugin.process(
                    options = pluginOptions,
                    files = files,
                    logger = logger,
                )

                is GenerationPluginService -> {
                    plugin.generateCode(pluginOptions, files, logger)
                    files
                }

                else -> error("Unexpected type of plugin ${plugin.javaClass.canonicalName}")
            }
        }
    }

    public val options: List<OptionDescriptor> =
        BASE_OPTIONS.map { it.toOptionDescriptor() } + plugins.flatMap { it.options }
}