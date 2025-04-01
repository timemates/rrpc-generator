package org.timemates.rrpc.gradle.task

import kotlinx.coroutines.*
import okio.FileSystem
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import org.timemates.rrpc.gradle.GradleRLogger
import org.timemates.rrpc.gradle.loadAsPlugins
import org.timemates.rrpc.gradle.toStringValueRepresentative
import javax.inject.Inject
import kotlin.io.extension

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
    internal abstract val permitPackageCycles: Property<Boolean>

    @get:Input
    internal abstract val options: MapProperty<ModuleIdentifier, Map<String, Any>>

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

    init {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val defaultLauncher = javaToolchainService.launcherFor(toolchain)
        launcher.convention(defaultLauncher)
    }

    @TaskAction
    public fun generate() {
        val launcher = launcher.get()

        runBlocking {
            val rlogger = GradleRLogger(logger, "rrpc-generator")
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

            val pluginsConfiguration = project.configurations.getByName("rrpcPluginsDependencies")

            val pluginToModuleId = pluginsConfiguration.resolvedConfiguration.resolvedArtifacts.mapNotNull { artifact ->
                if (artifact.moduleVersion.id.module !in options.get().keys) return@mapNotNull null

                artifact.moduleVersion.id.module to async(Dispatchers.IO) {
                    val commands = if (artifact.extension == "jar")
                        listOf(launcher.executablePath.asFile.absolutePath, "-jar", artifact.file.absolutePath)
                    else listOf(artifact.file.absolutePath)

                    ProcessPluginService.load(commands, rlogger)
                }.await()
            }.associate { it }

            val generationOptions = GenerationOptions.create {
                inputFolders.files.associateBy {
                    inputFoldersIsContext.get()[it.absolutePath] ?: false
                }.forEach { (isContext, folder) ->
                    val argument = if (isContext) "context_input" else "source_input"
                    rawSet(argument, folder.absolutePath)
                }

                options.get().forEach { (module, options) ->
                    val prefix = pluginToModuleId[module]!!.name

                    options.forEach { key, value ->
                        if (value is List<*>) {
                            value.forEach {
                                rawAppend("$prefix:$key", it.toStringValueRepresentative() ?: return@forEach)
                            }
                        } else {
                            rawSet("$prefix:$key", value.toStringValueRepresentative() ?: return@forEach)
                        }
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

                set(GenerationOptions.PERMIT_PACKAGE_CYCLES, permitPackageCycles.get().toString())
                set(GenerationOptions.GEN_OUTPUT, outputDir.absolutePath)
            }

            val plugins = pluginToModuleId.values.toList()

            CodeGenerator(FileSystem.SYSTEM, plugins)
                .generateCode(
                    options = generationOptions,
                    loggerFactory = { pluginName ->
                        GradleRLogger(logger, pluginName)
                    }
                )

            plugins.forEach { it.finish() }
        }
    }
}