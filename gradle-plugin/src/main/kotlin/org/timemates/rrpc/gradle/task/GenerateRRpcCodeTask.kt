package org.timemates.rrpc.gradle.task

import kotlinx.coroutines.*
import okio.FileSystem
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import org.timemates.rrpc.gradle.toStringValueRepresentative
import java.io.File

public abstract class GenerateRRpcCodeTask : DefaultTask() {

    init {
        group = "rrpc"
    }

    private val logger = Logging.getLogger(GenerateRRpcCodeTask::class.java)

    @get:InputFiles
    internal abstract val inputFolders: ConfigurableFileCollection

    @get:Input
    internal abstract val inputFoldersIsContext: MapProperty<String, Boolean>

    @get:Input
    internal abstract val options: MapProperty<String, Any?>

    @get:InputFiles
    internal abstract val contextProtoDeps: ConfigurableFileCollection

    @get:InputFiles
    internal abstract val sourceProtoDeps: ConfigurableFileCollection

    @get:InputFiles
    internal abstract val rrpcPluginsDeps: ConfigurableFileCollection

    @get:OutputDirectory
    internal abstract val outputDir: RegularFileProperty

    @TaskAction
    public fun generate() {
        val outputDir = outputDir.asFile.get()
        logger.lifecycle("Executing generateRRpcCode...")

        // Resolve configurations before accessing artifacts
        rrpcPluginsDeps.files
        sourceProtoDeps.files
        contextProtoDeps.files

        rrpcPluginsDeps.asFileTree.files.forEach { it.setExecutable(true) }

        // Cleanup previous output
        try {
            if (outputDir.exists()) {
                logger.lifecycle("Cleaning previous outputs in: ${outputDir.absolutePath}")
                outputDir.deleteRecursively()
            }
        } catch (e: Exception) {
            logger.error("Failed to clean output dir: ${e.stackTraceToString()}")
        }

        runBlocking {
            val plugins = rrpcPluginsDeps.loadAsPlugins()

            val generationOptions = GenerationOptions.create {
                inputFolders.files.associateBy {
                    inputFoldersIsContext.get()[it.absolutePath] ?: false
                }.forEach { (isContext, folder) ->
                    val argument = if (isContext) "context_input" else "source_input"
                    rawSet(argument, folder.absolutePath)
                }

                options.get().forEach { (key, value) ->
                    if (value is List<*>) {
                        value.forEach {
                            rawAppend(key, it.toStringValueRepresentative() ?: return@forEach)
                        }
                    } else {
                        rawSet(key, value.toStringValueRepresentative() ?: return@forEach)
                    }
                }

                // Source proto dependencies
                sourceProtoDeps.files.forEach { file ->
                    if (file.extension == "jar" || file.extension == "zip") {
                        append(GenerationOptions.SOURCE_INPUT, file.absolutePath)
                    } else {
                        logger.warn("${file.path} is not a jar or zip file, skipping as a source dependency.")
                    }
                }

                // Context proto dependencies
                contextProtoDeps.files.forEach { file ->
                    if (file.extension == "jar" || file.extension == "zip") {
                        append(GenerationOptions.CONTEXT_INPUT, file.absolutePath)
                    } else {
                        logger.warn("${file.path} is not a jar or zip file, skipping as a context dependency.")
                    }
                }
                set(GenerationOptions.GEN_OUTPUT, outputDir.absolutePath)
            }

            logger.lifecycle(
                CodeGenerator(FileSystem.SYSTEM, plugins)
                    .generateCode(generationOptions)
                    .message
            )

            plugins.forEach { it.finish() }
        }
    }

    private suspend fun ConfigurableFileCollection.loadAsPlugins(): List<ProcessPluginService> = coroutineScope {
        files.map { artifact ->
            async(Dispatchers.IO) {
                ProcessPluginService.load(listOf(artifact.absolutePath))
            }
        }.awaitAll()
    }
}