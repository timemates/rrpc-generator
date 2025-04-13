package app.timemate.rrpc.generator.cli.commands

import app.timemate.rrpc.generator.CodeGenerator
import app.timemate.rrpc.generator.cli.ConsoleRLogger
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.generator.plugin.api.OptionTypeKind
import app.timemate.rrpc.generator.plugin.api.communication.OptionDescriptor
import app.timemate.rrpc.generator.plugin.loader.ProcessPluginService
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import okio.FileSystem
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

internal class GenerateCommand(
    private val plugins: List<ProcessPluginService>,
) : SuspendingCliktCommand("rrgcli") {

    override fun help(context: Context): String = """
        Runs the code generation with plugins the plugins that were specified.
        """.trimIndent()

    /**
     * Enables debug logging.
     *
     * When enabled, additional debug information will be logged to help with troubleshooting.
     * The exact level of detail depends on the implementation.
     */
    val debug by option(
        names = arrayOf("--debug"),
        help = "Enables debug logging for troubleshooting."
    ).flag()

    /**
     * Controls error handling for plugin execution.
     *
     * When enabled, execution will continue even if some plugins encounter errors.
     * Errors related to plugins will be ignored where possible, allowing other plugins to proceed.
     *
     * Some plugins may have their own behavior regarding this flag. Instead of failing entirely,
     * they might skip parts of code that cannot be generated or processed.
     *
     * **Note: ** This does *not* apply to errors occurring during `.proto` file parsing.
     */
    private val ignoreErrors: Boolean by option(
        names = arrayOf("--ignore_errors"),
        help = "Allows execution to continue even if some plugins encounter errors. " +
            "Errors related to plugins will be ignored where possible, allowing other plugins to proceed. " +
            "Some plugins may handle this flag differently, for example, by skipping code generation for problematic parts instead of failing entirely. " +
            "Note: This does not apply to errors occurring during .proto file parsing."
    ).flag()

    /**
     * List of registered options within the clikt command and their respective values
     * provided by plugins.
     */
    private val pluginOptions: List<Pair<OptionDescriptor, Option>> =
        plugins.map { plugin ->
            plugin.options.mapNotNull { option ->
                val optionName = "--${plugin.name}:${option.name}"

                if (registeredOptions().any { it.names.first() == optionName })
                    return@mapNotNull null

                option to option(
                    names = arrayOf(optionName),
                    envvar = option.name.uppercase(),
                    help = option.description,
                ).applyTypeDescriptor(option.kind, option.isRepeatable).also { registerOption(it) }
            }
        }.flatMap { it }

    private val sourceInput by option(
        names = arrayOf("--${GenerationOptions.SOURCE_INPUT.name}"),
        help = GenerationOptions.SOURCE_INPUT.description
    ).path(mustExist = true, mustBeReadable = true).multiple(required = true)

    private val contextInput by option(
        names = arrayOf("--${GenerationOptions.CONTEXT_INPUT.name}"),
        help = GenerationOptions.CONTEXT_INPUT.description
    ).path(mustExist = true, mustBeReadable = true).multiple(required = false)

    private val permitPackageCycles by option(
        names = arrayOf("--${GenerationOptions.PERMIT_PACKAGE_CYCLES.name}"),
        help = GenerationOptions.PERMIT_PACKAGE_CYCLES.description
    ).boolean().default(false)

    // unused, just a stub to avoid errors
    private val _plugins by option(
        "--plugin",
        help = "List of plugins to be loaded. Can be either path to the executable or command to be executed (e.g. java -jar ...).",
        metavar = "<path>|<exec>"
    ).multiple()

    private val genOut by option("--gen_output", help = GenerationOptions.GEN_OUTPUT.description).path().required()

    override suspend fun run() {
        val genOptions = GenerationOptions.create {
            pluginOptions.forEach { (definition, raw) ->
                val name = raw.names.first().substringAfter("--")

                val value = when (raw) {
                    is OptionWithValues<*, *, *> -> raw.value
                    else -> null
                }

                @Suppress("UNCHECKED_CAST")
                when (definition.isRepeatable) {
                    true -> (value as? List<String>)?.forEach {
                        rawAppend(name, it)
                    } ?: return@forEach

                    false -> rawSet(name, value?.toString() ?: return@forEach)
                }
            }

            sourceInput.forEach {
                append(GenerationOptions.SOURCE_INPUT, it.absolutePathString())
            }
            contextInput.forEach {
                append(GenerationOptions.CONTEXT_INPUT, it.absolutePathString())
            }
            set(GenerationOptions.PERMIT_PACKAGE_CYCLES, permitPackageCycles.toString())
            set(GenerationOptions.GEN_OUTPUT, genOut.absolutePathString())
        }

        CodeGenerator(
            fileSystem = FileSystem.SYSTEM,
            plugins = plugins,
        ).generateCode(
            options = genOptions,
            loggerFactory = { pluginName ->
                ConsoleRLogger(
                    header = pluginName,
                    isDebugEnabled = debug,
                    onError = {
                        if (ignoreErrors) {
                            echo(
                                message = "rrgcli: An error is occurred in $pluginName, finishing the generation.",
                                err = true
                            )
                            exitProcess(1)
                        } else {
                            echo("rrgcli: An error is occurred in $pluginName, ignoring.", err = true)
                        }
                    },
                )
            }
        )
    }
}

private fun RawOption.applyTypeDescriptor(typeKind: OptionTypeKind, multiple: Boolean): Option {
    return when (typeKind) {
        OptionTypeKind.Boolean -> boolean().multiple(multiple)
        is OptionTypeKind.Choice -> choice(*typeKind.variants.toTypedArray()).multiple(multiple)
        OptionTypeKind.Number.Int -> int().multiple(multiple)
        OptionTypeKind.Number.Long -> long().multiple(multiple)
        OptionTypeKind.Number.Float -> float().multiple(multiple)
        OptionTypeKind.Number.Double -> double().multiple(multiple)
        OptionTypeKind.Path -> path().multiple(multiple)
        OptionTypeKind.Text -> this.multiple(multiple)
    }
}


private fun <T1, T2> NullableOption<T1, T2>.multiple(bool: Boolean): Option {
    return if (bool) multiple() else this
}