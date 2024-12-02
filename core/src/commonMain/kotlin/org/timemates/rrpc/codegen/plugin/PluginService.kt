package org.timemates.rrpc.codegen.plugin

import okio.BufferedSink
import okio.BufferedSource
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.data.GeneratorSignal
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.common.schema.RSFile

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
        public suspend fun main(
            args: List<String>,
            input: BufferedSource,
            output: BufferedSink,
            service: PluginService,
        ) {
            val arguments = Arguments(args.toTypedArray())

            val options = GenerationOptions.create {
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
            }

            PluginCommunication(input, output).receive { signal ->
                when (signal) {
                    GeneratorSignal.FetchOptionsList ->
                        PluginSignal.SendOptions(service.options)

                    is GeneratorSignal.SendInput -> service.generateCode(options, signal.files)
                }
            }
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
    ): PluginSignal.RequestStatusChange

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
    /**
     * @return [Boolean] whether the given [name] was presented in array of arguments.
     */
    fun isPresent(name: String): Boolean {
        return array.any { it.startsWith("-$name") }
    }

    /**
     * Returns value of the given argument with [name] or null.
     */
    fun getNamedOrNull(name: String): String? {
        val index = array.indexOfFirst { it.startsWith("-$name") }
            .takeIf { it >= 0 }
            ?: return null

        return array[index]
            .substringAfter("=")
    }

    fun getNamedList(name: String): List<String> {
        return array.withIndex()
            .filter { (_, value) -> value.startsWith("-$value") }
            .map { (index, _) -> array[index + 1] }
            .toList()
    }
}