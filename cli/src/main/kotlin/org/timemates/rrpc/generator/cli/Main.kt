package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.*
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.generator.kotlin.KotlinPluginService
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import kotlin.properties.Delegates
import kotlin.system.exitProcess


public object RRpcGeneratorMain {
    /**
     * List of options for functionality that is builtin in the rrgcli by default,
     * such as Kotlin Code Generation.
     */
    private val BUILTIN_OPTIONS =
        (CodeGenerator.BASE_OPTIONS.map { it.toOptionDescriptor() } + KotlinPluginService.options)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        System.err.println("rrgcli returned error: ${exception.message}")
        System.err.flush()
        exitProcess(1)
    }

    internal var outputPath: String by Delegates.notNull()

    /**
     * The `rrgcli` entry.
     *
     * For usage documentation, please refer to the [official documentation](https://rrpc.timemates.org/codegen-cli.html).
     */
    @JvmStatic
    public fun main(args: Array<String>): Unit = runBlocking(exceptionHandler) {
        // Phase 1: Load in the specified plugins
        // accepts both --plugin=X and --plugin="X"
        // also, may include commands to be run, like "java -jar ..."
        val plugins = args.filter { it.startsWith("--plugin=") }
            .map { it.replace("--plugin=", "").replace("\"", "") }
            .map { callable ->
                async(Dispatchers.IO) {
                    ProcessPluginService.load(callable.split(" "))
                }
            }.awaitAll()

        // Phase 2: ask plugin about its options
        val pluginsOptions = plugins.flatMap { it.options }


        // Phase 3: Start generation command with received custom options
        GenerateCommand(plugins, BUILTIN_OPTIONS + pluginsOptions).main(args)

        plugins.forEach { plugin ->
            plugin.finish()
        }

        System.out.flush()

        exitProcess(0)
    }
}