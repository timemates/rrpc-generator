package app.timemate.rrpc.generator.plugin.loader

import app.timemate.rrpc.generator.plugin.api.GenerationPluginService
import app.timemate.rrpc.generator.plugin.api.PluginService
import app.timemate.rrpc.generator.plugin.api.ProcessorPluginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.generator.plugin.api.communication.GeneratorCommunication
import app.timemate.rrpc.generator.plugin.api.communication.GeneratorMessage
import app.timemate.rrpc.generator.plugin.api.communication.GeneratorSignal
import app.timemate.rrpc.generator.plugin.api.communication.OptionDescriptor
import app.timemate.rrpc.generator.plugin.api.communication.PluginSignal
import app.timemate.rrpc.generator.plugin.api.communication.SignalId
import app.timemate.rrpc.generator.plugin.api.communication.receiveOr
import app.timemate.rrpc.generator.plugin.api.communication.receiveWhileIsAndReturnUnsatisfied
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.proto.schema.RSFile
import java.util.*

public interface ProcessPluginService : PluginService {
    public companion object {
        public suspend fun load(
            commands: List<String>,
            logger: RLogger,
        ): ProcessPluginService {
            val process = ProcessBuilder(commands)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            if (!process.isAlive)
                error("Process was not started successfully. Commands that were used to run the process: ${commands.joinToString(" ")}")

            val callable = commands.joinToString(" ")

            logger.debug("Process for callable `$callable` is started with ${process.pid()} PID.")

            val communication = GeneratorCommunication(
                input = process.inputStream.source().buffer(),
                output = process.outputStream.sink().buffer(),
            )

            communication.send(
                GeneratorMessage {
                    id = SignalId(UUID.randomUUID().toString())
                    signal = GeneratorSignal.FetchMetadata()
                }.also {
                    logger.debug("Initializing handshake for callable `$callable`. Trying to send the following message: $it")
                }
            )

            logger.lifecycle("sent initial message")

            val metaInfo = communication.receiveOr<PluginSignal.SendMetaInformation> {
                throw IllegalStateException("Error while processing communication (for ${commands.joinToString(" ")}, expected receiving meta information, but didn't receive it.")
            }.info

            logger.debug("Received meta information about `$callable`: $metaInfo. Plugin is successfully loaded.")

            return when (metaInfo.role) {
                PluginService.PluginRole.GENERATOR -> ProcessCodeGeneratorPluginService(
                    name = metaInfo.name,
                    description = metaInfo.description,
                    options = metaInfo.options,
                    communication = communication,
                    process = process,
                    systemLogger = logger,
                )

                else -> ProcessModifierPluginService(
                    name = metaInfo.name,
                    description = metaInfo.description,
                    options = metaInfo.options,
                    communication = communication,
                    process = process,
                    systemLogger = logger,
                )
            }
        }
    }

    /**
     * Finishes the process with a plugin service.
     */
    public suspend fun finish()
}

private class ProcessCodeGeneratorPluginService(
    override val name: String,
    override val description: String,
    override val options: List<OptionDescriptor>,
    private val communication: GeneratorCommunication,
    private val process: Process,
    private val systemLogger: RLogger,
) : GenerationPluginService, ProcessPluginService {

    override suspend fun generateCode(options: GenerationOptions, files: List<RSFile>, logger: RLogger): Unit =
        withContext(Dispatchers.IO) {
            systemLogger.debug("Starting generateCode action in $name, ${files.size} files to be processed.")

            val genOutput = options[GenerationOptions.GEN_OUTPUT]?.toFile()
                ?: error("No generation output folder is provided.")

            communication.send(
                GeneratorMessage {
                    id = SignalId(UUID.randomUUID().toString())
                    signal = GeneratorSignal.SendInput(files, options)
                }.also {
                    systemLogger.debug("Sending the message with the parsed input files. Generation output: ${genOutput.absolutePath}. Options: $options.")
                }
            )

            val lastMessage = communication.receiveWhileIsAndReturnUnsatisfied<PluginSignal.LogMessage> {
                logger.log(it.level, it.message)
            }

            systemLogger.debug("Received non-log message (last that is supposed to be result of the generation): $lastMessage.")

            (lastMessage as? PluginSignal.CodeGenerated ?: error("Unexpected message from plugin: $lastMessage"))
                .also {
                    logger.debug("Finishing plugin with a success state")
                    finish()
                }
        }

    /**
     * Finishes the process with a plugin service.
     */
    override suspend fun finish() {
        systemLogger.debug("Finishing the process with PID ${process.pid()} of `$name` plugin.")
        process.destroy()
        communication.close()
    }

    override fun toString(): String {
        return "Generation Plugin '$name' at ${process.pid()} PID"
    }
}

private class ProcessModifierPluginService(
    override val name: String,
    override val description: String,
    override val options: List<OptionDescriptor>,
    private val communication: GeneratorCommunication,
    private val process: Process,
    private val systemLogger: RLogger,
) : ProcessorPluginService, ProcessPluginService {

    override suspend fun process(options: GenerationOptions, files: List<RSFile>, logger: RLogger): List<RSFile> =
        withContext(Dispatchers.IO) {
            systemLogger.debug("Starting process action in $name, ${files.size} files to be processed.")

            val genOutput = options[GenerationOptions.GEN_OUTPUT]?.toFile()?.absolutePath ?: ""

            communication.send(
                GeneratorMessage {
                    id = SignalId(UUID.randomUUID().toString())
                    signal = GeneratorSignal.SendInput(files, options)
                }.also {
                    systemLogger.debug("Sending the message with the parsed input files.")
                }
            )

            val lastMessage = communication.receiveWhileIsAndReturnUnsatisfied<PluginSignal.LogMessage> {
                logger.log(it.level, it.message)
            }

            systemLogger.debug("Received non-log message (last): $lastMessage.")

            (lastMessage as? PluginSignal.ChangedInput ?: error("Unexpected message from plugin: $lastMessage."))
                .also {
                    finish()
                }
                .files
        }

    /**
     * Finishes the process with a plugin service.
     */
    override suspend fun finish() {
        systemLogger.debug("Finishing the process with PID ${process.pid()} of `$name` plugin.")
        process.destroy()
        communication.close()
    }

    override fun toString(): String {
        return "Processor Plugin '$name' at ${process.pid()} PID"
    }
}