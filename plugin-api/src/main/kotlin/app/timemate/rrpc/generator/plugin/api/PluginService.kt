package app.timemate.rrpc.generator.plugin.api

import app.timemate.rrpc.generator.plugin.api.communication.*
import app.timemate.rrpc.generator.plugin.api.logger.PluginRLogger
import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public interface PluginService {
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
            Arguments(args.toTypedArray())
            val communication = PluginCommunication(input, output)

            val logger = PluginRLogger(communication)

            val initCommand =
                communication.receive().takeIf { it.signal is GeneratorSignal.FetchMetadata } ?: exitProcess(0)

            val role = when (service) {
                is GenerationPluginService -> PluginRole.GENERATOR
                is ProcessorPluginService -> PluginRole.PROCESS
                else -> error("Unknown type of plugin: ${service.javaClass.canonicalName}")
            }

            communication.send(
                PluginMessage.create {
                    id = initCommand.id
                    signal = PluginSignal.SendMetaInformation(
                        PluginSignal.SendMetaInformation.MetaInformation(
                            options = service.options,
                            name = service.name,
                            description = service.description,
                            role = role,
                        )
                    )
                }
            )

            val command = communication.receive()

            val incomingSignal =
                command.signal as? GeneratorSignal.SendInput ?: error("Invalid command for plugin ${service.name}.")

            communication.send(
                PluginMessage.create {
                    id = SignalId(Uuid.random().toHexString())
                    signal = when (role) {
                        PluginRole.GENERATOR -> {
                            service as? GenerationPluginService ?: error("Role of plugin is not a generator.")

                            service.generateCode(
                                options = incomingSignal.options,
                                files = incomingSignal.files,
                                logger = logger,
                            )

                            PluginSignal.CodeGenerated
                        }

                        PluginRole.PROCESS -> {
                            service as? ProcessorPluginService ?: error("Role of plugin is not a processor.")

                            PluginSignal.ChangedInput(
                                files = service.process(
                                    options = incomingSignal.options,
                                    files = incomingSignal.files,
                                    logger = logger,
                                ),
                            )
                        }
                    }
                }
            )
        }
    }

    @Serializable
    public enum class PluginRole {
        /**
         * Type of the plugin that is used to generate code.
         * Effectively means that the only method will be triggered is [generateCode].
         */
        GENERATOR,

        /**
         * Type of the plugin that is used to modify the input.
         * Effectively means that the only method will be triggered is [modify].
         */
        PROCESS
    }
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
