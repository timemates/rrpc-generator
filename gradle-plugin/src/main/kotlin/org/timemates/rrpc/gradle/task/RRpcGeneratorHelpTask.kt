package org.timemates.rrpc.gradle.task

import kotlinx.coroutines.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.gradle.GradleRLogger
import org.timemates.rrpc.gradle.loadAsPlugins
import javax.inject.Inject

public abstract class RRpcGeneratorHelpTask : DefaultTask() {

    init {
        group = "rrpc"
    }

    private val logger = Logging.getLogger(RRpcGeneratorHelpTask::class.java)

    @get:InputFiles
    public abstract val rrpcPluginsDeps: ConfigurableFileCollection

    @get:Nested
    public abstract val launcher: Property<JavaLauncher>

    @get:Inject
    protected abstract val javaToolchainService: JavaToolchainService

    @get:Input
    internal abstract val options: MapProperty<ModuleIdentifier, Map<String, Any>>

    init {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @TaskAction
    public fun showHelp() {
        val launcher = launcher.get()
        val rlogger = GradleRLogger(logger, "rrpc-generator")
        rrpcPluginsDeps.asFileTree.files.forEach { it.setExecutable(true) }

        runBlocking {
            val pluginsConfiguration = project.configurations.getByName("rrpcPluginsDependencies")

            val plugins = pluginsConfiguration.resolvedConfiguration.resolvedArtifacts.filter {
                it.moduleVersion.id.module in options.get().keys
            }.loadAsPlugins(rlogger, launcher, options.get())

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
}