package app.timemate.rrpc.generator.cli

import app.timemate.rrpc.generator.cli.commands.GenerateCommand
import app.timemate.rrpc.generator.plugin.loader.ProcessPluginService
import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.*
import kotlin.system.exitProcess


private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    System.err.println("rrgcli returned error: ${exception.message}")
    System.err.flush()
    exitProcess(1)
}

/**
 * The `rrgcli` entry.
 *
 * For usage documentation, please refer to the [official documentation](https://rrpc.timemates.org/codegen-cli.html).
 */
fun main(args: Array<String>): Unit = runBlocking(exceptionHandler) {
    val ignoreErrors = args.contains("--ignore-errors")

    val logger = ConsoleRLogger(
        header = "rrgcli",
        isDebugEnabled = args.contains("--debug"),
        onError = {
            if (!ignoreErrors)
                exitProcess(1)
            else println("rrgcli: Generator is configured to ignore errors, continuing.")
        }
    )

    val plugins = parsePlugins(args).map { pluginArgs ->
        async(Dispatchers.IO) {
            ProcessPluginService.load(pluginArgs, logger)
        }
    }.awaitAll()

    GenerateCommand(plugins).main(args)

    System.out.flush()

    exitProcess(0)
}

private fun parsePlugins(args: Array<String>): List<List<String>> {
    return args.filter { it.startsWith("--plugin=") }
        .map { it.removePrefix("--plugin=").trim() }
        .map { it.split(" ").filter { part -> part.isNotBlank() } }
}
