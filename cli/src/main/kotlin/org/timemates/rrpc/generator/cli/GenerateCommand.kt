package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import kotlinx.coroutines.CoroutineScope
import okio.FileSystem
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.OptionTypeKind
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import kotlin.io.path.pathString

class GenerateCommand(
    private val plugins: List<Plugin>,
    options: List<OptionDescriptor>,
    private val scope: CoroutineScope,
) : SuspendingCliktCommand("rrgcli") {
    private val registeredOptions = options
        .distinctBy { it.name }
        .map { option ->
            option to option(
                names = arrayOf("--${option.name}"),
                envvar = option.name.uppercase(),
                help = option.description,
            ).applyTypeDescriptor(option.kind, option.isRepeatable).also { registerOption(it) }
        }

    init {
        // not used here, but to keep us away from errors;
        registerOption(option("--plugin").multiple())
    }

    val genOut by option("--gen_output", help = GenerationOptions.GEN_OUTPUT.description!!).path().required()

    override suspend fun run() {
        outputPath = genOut.pathString

        val genOptions = GenerationOptions.create {
            registeredOptions.forEach { (definition, raw) ->
                val value = when (raw) {
                    is OptionWithValues<*, *, *> -> raw.value
                    else -> null
                }

                @Suppress("UNCHECKED_CAST")
                when (definition.isRepeatable) {
                    true -> (value as? List<String>)?.forEach {
                        rawAppend(definition.name, it)
                    } ?: return@forEach

                    false -> rawSet(definition.name, value?.toString() ?: return@forEach)
                }
            }
        }

        println(
            CodeGenerator(FileSystem.SYSTEM, plugins = plugins.map { ProcessAsPluginService(it) })
                .generateCode(options = genOptions).message.trimIndent()
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