package app.timemate.rrpc.gradle.task

import app.timemate.rrpc.generator.CodeGenerator
import kotlinx.coroutines.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import app.timemate.rrpc.generator.plugin.api.communication.toOptionDescriptor
import app.timemate.rrpc.gradle.GradleRLogger
import app.timemate.rrpc.gradle.collectArtifactFiles
import app.timemate.rrpc.gradle.configuration.type.GenerationPlugin
import app.timemate.rrpc.gradle.loadAsPlugins
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

    @get:Internal
    internal abstract val plugins: ListProperty<GenerationPlugin>

    init {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @TaskAction
    public fun showHelp() {
        val launcher = launcher.get()
        val rLogger = GradleRLogger(
            logger = logger,
            header = "rrpc-generator",
            isDebugEnabled = logger.isDebugEnabled
        )
        rrpcPluginsDeps.asFileTree.files.forEach { it.setExecutable(true) }

        val plugins = plugins.get()

        runBlocking {
            val pluginsConfiguration = project.configurations.getByName("rrpcPlugin")

            val loadedPlugins = plugins.flatMap { plugin ->
                when (plugin.dependency) {
                    is Provider<*> -> (plugin.dependency.get() as Dependency).collectArtifactFiles(pluginsConfiguration)
                    is Dependency -> plugin.dependency.collectArtifactFiles(pluginsConfiguration)
                    else -> error("Unknown dependency type: ${plugin.dependency}")
                }
            }.loadAsPlugins(rLogger, launcher)

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

            loadedPlugins.forEach { plugin ->
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

            loadedPlugins.forEach { it.finish() }
        }
    }
}