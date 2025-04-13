package app.timemate.rrpc.gradle.task

import app.timemate.rrpc.generator.CodeGenerator
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.gradle.GradleRLogger
import app.timemate.rrpc.gradle.collectArtifactFiles
import app.timemate.rrpc.gradle.configuration.type.GenerationPlugin
import app.timemate.rrpc.gradle.loadAsPlugins
import app.timemate.rrpc.gradle.toStringValueRepresentative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

public abstract class GenerateRRpcCodeTask : DefaultTask() {

    init {
        group = "rrpc"
    }

    private val logger = Logging.getLogger(GenerateRRpcCodeTask::class.java)

    @get:Input
    internal abstract val permitPackageCycles: Property<Boolean>

    @get:Internal
    internal abstract val plugins: ListProperty<GenerationPlugin>

    @get:InputFiles
    internal abstract val contextProtoDeps: ConfigurableFileCollection

    @get:InputFiles
    internal abstract val sourceProtoDeps: ConfigurableFileCollection

    @get:InputFiles
    internal abstract val rrpcPluginsDeps: ConfigurableFileCollection

    @get:OutputDirectory
    internal abstract val outputDir: RegularFileProperty

    @get:Nested
    public abstract val launcher: Property<JavaLauncher>

    @get:Inject
    protected abstract val javaToolchainService: JavaToolchainService

    @get:OutputDirectory
    internal abstract val dependenciesProtoFolder: DirectoryProperty

    init {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @TaskAction
    public fun generate() {
        val srcOut = dependenciesProtoFolder.get().asFile.resolve("source")
        val ctxOut = dependenciesProtoFolder.get().asFile.resolve("context")

        val launcher = launcher.get()
        val plugins = plugins.get()

        srcOut.mkdirs()
        ctxOut.mkdirs()

        runBlocking {
            val rLogger = GradleRLogger(
                logger = logger,
                header = "rrpc-generator",
                isDebugEnabled = logger.isDebugEnabled
            )
            val outputDir = outputDir.asFile.get()

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

            val pluginsConfiguration = project.configurations.getByName("rrpcPlugin")

            val pluginToProcesses = plugins.associateWith { plugin ->
                async(Dispatchers.IO) {
                    when (plugin.dependency) {
                        is Provider<*> -> (plugin.dependency.get() as Dependency).collectArtifactFiles(
                            pluginsConfiguration
                        )

                        is Dependency -> plugin.dependency.collectArtifactFiles(pluginsConfiguration)
                        else -> error("Unknown dependency type: ${plugin.dependency}")
                    }.loadAsPlugins(rLogger, launcher)
                }
            }.mapValues { (_, deferred) -> deferred.await() }

            val generationOptions = GenerationOptions.create {
                append(GenerationOptions.SOURCE_INPUT, srcOut.absolutePath)
                append(GenerationOptions.CONTEXT_INPUT, ctxOut.absolutePath)

                pluginToProcesses.forEach { (module, processes) ->
                    processes.forEach { process ->
                        val prefix = process.name

                        module.options.forEach { (key, value) ->
                            if (value is List<*>) {
                                value.forEach {
                                    rawAppend("$prefix:$key", it.toStringValueRepresentative() ?: return@forEach)
                                }
                            } else {
                                rawSet("$prefix:$key", value.toStringValueRepresentative() ?: return@forEach)
                            }
                        }
                    }
                }

                sourceProtoDeps.files.forEach { file ->
                    if (file.extension == "jar" || file.extension == "zip") {
                        append(GenerationOptions.SOURCE_INPUT, file.absolutePath)
                    } else {
                        logger.warn("${file.path} is not a jar or zip file, skipping as a source dependency.")
                    }
                }

                contextProtoDeps.files.forEach { file ->
                    if (file.extension == "jar" || file.extension == "zip") {
                        append(GenerationOptions.CONTEXT_INPUT, file.absolutePath)
                    } else {
                        logger.warn("${file.path} is not a jar or zip file, skipping as a context dependency.")
                    }
                }

                set(GenerationOptions.PERMIT_PACKAGE_CYCLES, permitPackageCycles.get().toString())
                set(GenerationOptions.GEN_OUTPUT, outputDir.absolutePath)
            }

            val pluginProcesses = pluginToProcesses.values.flatten()

            logger.lifecycle("Plugin processes: $pluginProcesses")

            CodeGenerator(FileSystem.SYSTEM, pluginProcesses)
                .generateCode(
                    options = generationOptions,
                    loggerFactory = { pluginName ->
                        GradleRLogger(logger, pluginName, logger.isDebugEnabled)
                    }
                )

            logger.lifecycle("Finishing plugin processes..")

            pluginProcesses.forEach { it.finish() }
        }
    }
}