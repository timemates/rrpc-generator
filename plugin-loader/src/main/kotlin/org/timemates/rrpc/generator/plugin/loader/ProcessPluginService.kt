package org.timemates.rrpc.generator.plugin.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okio.buffer
import okio.sink
import okio.source
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.GeneratorCommunication
import org.timemates.rrpc.codegen.plugin.PluginService
import org.timemates.rrpc.codegen.plugin.data.*
import org.timemates.rrpc.codegen.plugin.receiveOr
import org.timemates.rrpc.codegen.schema.RSFile
import java.util.*

public class ProcessPluginService(
    override val name: String,
    override val description: String,
    override val options: List<OptionDescriptor>,
    private val communication: GeneratorCommunication,
    private val process: Process,
) : PluginService {
    public companion object {
        public suspend fun load(commands: List<String>): ProcessPluginService {
            val process = ProcessBuilder(commands)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            val communication = GeneratorCommunication(
                input = process.inputStream.source().buffer(),
                output = process.outputStream.sink().buffer(),
            )

            communication.send(GeneratorMessage {
                id = SignalId(UUID.randomUUID().toString())
                signal = GeneratorSignal.FetchMetadata()
            })

            val metaInfo = communication.receiveOr<PluginSignal.SendMetaInformation> {
                throw IllegalStateException("Error while processing communication (for ${commands.joinToString(" ")}, expected receiving meta information, but didn't receive it.")
            }.info

            return ProcessPluginService(metaInfo.name, metaInfo.description, metaInfo.options, communication, process)
        }
    }

    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
    ): PluginSignal.RequestStatusChange = withContext(Dispatchers.IO) {
        val genOutput = options[GenerationOptions.GEN_OUTPUT]?.toFile()
            ?: error("No generation output folder is provided.")
        communication.send(
            GeneratorMessage {
                id = SignalId(UUID.randomUUID().toString())
                signal = GeneratorSignal.SendInput(genOutput.absolutePath, files)
            }
        )

        while (isActive) {
            if (communication.incoming.isClosed || !process.isAlive) break
            if (!communication.incoming.hasNext()) continue

            yield()
        }

        (communication.incoming.next().signal as PluginSignal.RequestStatusChange)
            .also { communication.close() }
    }

    /**
     * Finishes the process with a plugin service.
     */
    public fun finish() {
        communication.close()
        process.destroy()
    }
}