package org.timemates.rrpc.codegen.plugin

import okio.BufferedSink
import okio.BufferedSource
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.data.GeneratorSignal
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginMessage
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.PluginSignal.*
import org.timemates.rrpc.codegen.plugin.data.PluginSignal.SendMetaInformation.*
import org.timemates.rrpc.codegen.plugin.data.SignalId
import org.timemates.rrpc.codegen.schema.RSFile
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public interface PluginService {
    public companion object {
        /**
         * Main entry point for the plugin service execution.
         *
         * This function facilitates communication between the plugin and the rRPC generator,
         * using a communication channel based on `BufferedSource` and `BufferedSink`.
         * It listens for incoming signals from the generator and triggers appropriate plugin
         * service operations.
         *
         * Supported signals:
         * - **FetchOptionsList**: Triggers a request to fetch a list of configurable options
         *   from the plugin.
         * - **SendInput**: Provides a list of files for code generation, delegating the
         *   processing to the plugin.
         *
         * @param input The source stream for receiving signals from the generator.
         * @param output The sink stream for sending responses to the generator.
         * @param service The `PluginService` implementation to handle incoming requests.
         */
        @OptIn(ExperimentalUuidApi::class)
        public suspend fun main(
            args: List<String>,
            input: BufferedSource,
            output: BufferedSink,
            service: PluginService,
        ) {
            /**
             * To avoid messing up with binary data exchange between processes.
             */
            System.setOut(System.err)
            val arguments = Arguments(args.toTypedArray())
            val communication = PluginCommunication(input, output)

            val initSignal = communication.receiveOr<GeneratorSignal.FetchMetadata> {
                exitProcess(0)
            }

            communication.send(
                PluginMessage.create {
                    id = SignalId(Uuid.random().toHexString())
                    signal = SendMetaInformation(
                        MetaInformation(
                            options = service.options,
                            name = service.name,
                            description = service.description,
                        )
                    )
                }
            )

            val sendInputSignal = communication.receiveOr<GeneratorSignal.SendInput> {
                exitProcess(0)
            }

            communication.send(
                PluginMessage.create {
                    id = SignalId(Uuid.random().toHexString())
                    signal = service.generateCode(
                        options = GenerationOptions.create {
                            service.options.forEach { descriptor ->
                                if (descriptor.isRepeatable) {
                                    arguments.getNamedList(descriptor.name).forEach {
                                        rawAppend(descriptor.name, it)
                                    }
                                } else {
                                    arguments.getNamedOrNull(descriptor.name)?.let {
                                        rawSet(descriptor.name, it)
                                    }
                                }
                            }
                            set(GenerationOptions.GEN_OUTPUT, sendInputSignal.outputPath)
                        },
                        files = sendInputSignal.files
                    )
                }
            )
        }
    }

    /**
     * Generates code based on the provided input files.
     *
     * This function is triggered when the generator sends a `SendInput` signal containing
     * a list of files (`RSFile`) to process. The plugin is responsible for handling these
     * files and returning a signal indicating the status of the request.
     *
     * @param files The list of files to be processed by the plugin.
     * @return A `PluginSignal.RequestStatusChange` indicating the status of the generation process.
     */
    public suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
    ): RequestStatusChange

    /**
     * Retrieves a list of options supported by the plugin.
     *
     * This function is triggered when the generator sends a `FetchOptionsList` signal. The
     * plugin should return a list of `OptionDescriptor` objects, each representing a
     * configurable option available for this plugin.
     *
     * @return A list of `OptionDescriptor` representing the plugin's configurable options.
     */
    public val options: List<OptionDescriptor>

    /**
     * Generator's name
     */
    public val name: String

    /**
     * Retrieves description of the plugin.
     *
     * @return A String with detailed information about the Plugin and its functionality.
     */
    public val description: String
}

@JvmInline
private value class Arguments(private val array: Array<String>) {
    fun isPresent(name: String): Boolean {
        return array.any { it.startsWith("--$name") }
    }

    fun getNamedOrNull(name: String): String? {
        val index = array.indexOfFirst { it.startsWith("--$name") }
            .takeIf { it >= 0 }
            ?: return null

        val rawValue = array[index].substringAfter("=", missingDelimiterValue = "")
        return parseValue(rawValue, index)
    }

    fun getNamedList(name: String): List<String> {
        return array.withIndex()
            .filter { (_, value) -> value.startsWith("--$name") }
            .mapNotNull { (index, value) ->
                val rawValue = value.substringAfter("=", missingDelimiterValue = "")
                parseValue(rawValue, index)
            }
    }

    private fun parseValue(value: String, index: Int): String? {
        return when {
            value.isNotEmpty() -> value.trim('"')
            index + 1 < array.size && array[index + 1].startsWith("\"") ->
                extractQuotedValue(index + 1)
            index + 1 < array.size -> array[index + 1]
            else -> null
        }
    }

    private fun extractQuotedValue(startIndex: Int): String {
        val builder = StringBuilder()
        for (i in startIndex until array.size) {
            val part = array[i]
            builder.append(part.trim('"'))
            if (part.endsWith('"')) break
            builder.append(" ")
        }
        return builder.toString()
    }
}
