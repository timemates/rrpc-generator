package org.timemates.rrpc.generator.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.PluginService
import org.timemates.rrpc.codegen.plugin.data.*
import org.timemates.rrpc.codegen.schema.RSFile
import java.io.File
import java.util.*

class ProcessAsPluginService(
    private val plugin: Plugin,
) : PluginService {
    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
    ): PluginSignal.RequestStatusChange = withContext(Dispatchers.IO) {
        plugin.communication.send(
            GeneratorMessage {
                id = SignalId(UUID.randomUUID().toString())
                signal = GeneratorSignal.SendInput(File(outputPath, plugin.name).absolutePath, files)
            }
        )

        while (isActive) {
            if (plugin.communication.incoming.isClosed || !plugin.process.isAlive) break
            if (!plugin.communication.incoming.hasNext()) continue

            yield()
        }

        (plugin.communication.incoming.next().signal as PluginSignal.RequestStatusChange)
            .also { plugin.communication.close() }
    }

    override val options: List<OptionDescriptor>
        get() = plugin.options

    override val name: String
        get() = plugin.name
    override val description: String
        get() = plugin.description
}