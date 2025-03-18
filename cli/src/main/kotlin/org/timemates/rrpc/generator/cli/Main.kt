package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.*
import okio.buffer
import okio.sink
import okio.source
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.plugin.GeneratorCommunication
import org.timemates.rrpc.codegen.plugin.data.*
import org.timemates.rrpc.generator.kotlin.KotlinPluginService
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess


object RRpcGeneratorMain {
    /**
     * List of options for functionality that is builtin in the rrgcli by default,
     * such as Kotlin Code Generation.
     */
    private val BUILTIN_OPTIONS =
        (CodeGenerator.BASE_OPTIONS.map { it.toOptionDescriptor() } + KotlinPluginService.options)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("rrgcli returned error: ${exception.message}")
        exitProcess(1)
    }

    var outputPath: String by Delegates.notNull()

    /**
     * The `rrgcli` entry.
     *
     * For usage documentation, please refer to the [official documentation](https://rrpc.timemates.org/codegen-cli.html).
     */
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking(exceptionHandler) {
        // Phase 1: Load in the specified plugins
        // accepts both --plugin=X and --plugin="X"
        // also, may include commands to be run, like "java -jar ..."
        val plugins = args.filter { it.startsWith("--plugin=") }
            .map { it.replace("--plugin=", "").replace("\"", "") }
            .map { callable ->
                async {
                    val process = ProcessBuilder(callable.split(" "))
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

                    while (isActive) {
                        if (communication.incoming.isClosed) break
                        if (!communication.incoming.hasNext()) continue
                        break
                    }

                    val metaInfo = (communication.incoming.next().signal as PluginSignal.SendMetaInformation).info

                    Plugin(
                        name = metaInfo.name,
                        description = metaInfo.description,
                        options = metaInfo.options,
                        communication = communication,
                        process = process,
                    )
                }
            }.awaitAll()

        val scope = CoroutineScope(Dispatchers.IO)

        // Phase 2: ask plugin about its options
        val pluginsOptions = plugins.flatMap { it.options }


        // Phase 3: Start generation command with received custom options
        GenerateCommand(plugins, BUILTIN_OPTIONS + pluginsOptions, scope).main(args)

        plugins.forEach { plugin ->
            plugin.communication.close()
            plugin.process.destroy()
        }

        exitProcess(0)
    }
}