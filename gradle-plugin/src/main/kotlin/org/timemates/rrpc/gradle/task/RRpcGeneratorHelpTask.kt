package org.timemates.rrpc.gradle.task

import kotlinx.coroutines.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService

public abstract class RRpcGeneratorHelpTask : DefaultTask() {

    init {
        group = "rrpc"
    }

    private val logger = Logging.getLogger(RRpcGeneratorHelpTask::class.java)

    @get:InputFiles
    public abstract val rrpcPluginsDeps: ConfigurableFileCollection

    @TaskAction
    public fun showHelp() {
        logger.lifecycle("Running rrpcGeneratorHelp... 222")

        rrpcPluginsDeps.asFileTree.files.forEach { it.setExecutable(true) }

        runBlocking {
            val plugins = rrpcPluginsDeps.loadAsPlugins()

            logger.lifecycle("====================[ rrpc ]====================")
            logger.lifecycle("                Generator Overview               ")
            logger.lifecycle("================================================")
            logger.lifecycle("")

            logger.lifecycle("⚙ Generator Options:")
            CodeGenerator.BASE_OPTIONS.map { it.toOptionDescriptor() }.forEach { option ->
                logger.lifecycle("  - ${option.name}")
                logger.lifecycle("    Description : ${option.description}")
                logger.lifecycle("    Type        : ${option.kind.readableName}")
                logger.lifecycle("    Repeatable  : ${option.isRepeatable}")
                logger.lifecycle("")
            }

            logger.lifecycle("------------------------------------------------")
            logger.lifecycle("         Available Plugins and Their Options    ")
            logger.lifecycle("------------------------------------------------")

            plugins.forEach { plugin ->
                logger.lifecycle("")
                logger.lifecycle("▶ Plugin: ${plugin.name}")
                logger.lifecycle("  ${plugin.description}")
                logger.lifecycle("")

                if (plugin.options.isNotEmpty()) {
                    logger.lifecycle("  ▸ Options:")
                    plugin.options.forEach { option ->
                        logger.lifecycle("    - ${option.name}")
                        logger.lifecycle("      Description : ${option.description}")
                        logger.lifecycle("      Type        : ${option.kind.readableName}")
                        logger.lifecycle("      Repeatable  : ${option.isRepeatable}")
                        logger.lifecycle("")
                    }
                } else {
                    logger.lifecycle("  (No options available)")
                }

                logger.lifecycle("------------------------------------------------")
            }

            logger.lifecycle("================================================")
            logger.lifecycle("                End of Overview                 ")
            logger.lifecycle("================================================")

            plugins.forEach { it.finish() }
        }
    }

    private suspend fun ConfigurableFileCollection.loadAsPlugins(): List<ProcessPluginService> = coroutineScope {
        files.map { artifact ->
            async(Dispatchers.IO) {
                ProcessPluginService.load(
                    listOf(artifact.absolutePath)
                )
            }
        }.awaitAll()
    }
}