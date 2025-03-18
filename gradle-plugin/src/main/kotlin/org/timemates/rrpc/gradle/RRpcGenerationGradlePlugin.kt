package org.timemates.rrpc.gradle

import okio.FileSystem
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.generator.cli.RRpcGeneratorMain
import org.timemates.rrpc.generator.kotlin.KotlinPluginService
import java.io.File

public class RRpcGenerationGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val sourceProtoDeps: Configuration = target.configurations.create("rrpcSourceProtoDependencies") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            }
        }

        val contextProtoDeps: Configuration = target.configurations.create("rrpcContextProtoDependencies") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            }
        }

        val rrpcPluginsDeps: Configuration = target.configurations.create("rrpcPluginsDependencies") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(
                    Usage.USAGE_ATTRIBUTE,
                    target.objects.named(Usage::class.java, "rrpc-binary")
                )

                attribute(
                    Category.CATEGORY_ATTRIBUTE,
                    target.objects.named(Category::class.java, Category.LIBRARY)
                )

                // Define the artifact type as 'rrpc-binary' or something unique
                attribute(
                    Attribute.of("artifactType", String::class.java),
                    "rrpc-plugins-binary"
                )
            }
        }

        val extension = target.extensions.create<RRpcExtension>("rrpc", target, sourceProtoDeps, contextProtoDeps)

        val help = target.tasks.register("rrpcGeneratorHelp") {
            doLast {
                RRpcGeneratorMain.main(
                    buildList {
                        rrpcPluginsDeps.artifacts.files.forEach { file ->
                            add("--plugin=\"${file.absolutePath}\"")
                        }
                    }.toTypedArray()
                )
            }
        }

        val generateCode = target.tasks.register("generateRRpcCode") {
            group = "rrpc"

            inputs.files(extension.inputFolders)
            outputs.dir(project.layout.buildDirectory.dir("rrpc-generated"))

            doLast {
                sourceProtoDeps.resolve()
                contextProtoDeps.resolve()

                try {
                    outputs.files.forEach(File::deleteRecursively)
                } catch (e: Exception) {
                    logger.error(e.stackTraceToString())
                }

                RRpcGeneratorMain.main(
                    buildList {
                        extension.options.get().forEach { (key, value) ->
                            val value = value.toString()
                            add("--$key" + if (value.contains(Regex("\\s"))) "\"$value\"" else value)
                        }

                        sourceProtoDeps.artifacts.files.forEach { file ->
                            if (file.extension == "jar" || file.extension == "zip") {
                                add("--${GenerationOptions.SOURCE_INPUT}=\"${file.absolutePath}\"")
                            } else {
                                logger.warn("${file.path} is not a jar or a zip file, skipping as a source dependency.")
                            }
                        }

                        contextProtoDeps.artifacts.files.forEach { file ->
                            if (file.extension == "jar" || file.extension == "zip") {
                                add("--${GenerationOptions.CONTEXT_INPUT}=\"${file.absolutePath}\"")
                            } else {
                                logger.warn("${file.path} is not a jar or a zip file, skipping as a context dependency.")
                            }
                        }

                        rrpcPluginsDeps.artifacts.files.forEach { file ->
                            add("--plugin=\"${file.absolutePath}\"")
                        }
                    }.toTypedArray()
                )
            }
        }

        target.afterEvaluate {
            val allSourceSets = target.extensions.findByType<KotlinMultiplatformExtension>()?.sourceSets
                ?: target.extensions.findByType<KotlinJvmProjectExtension>()?.sourceSets
                ?: target.extensions.findByType<KotlinAndroidProjectExtension>()?.sourceSets

            val commonSourceSet = allSourceSets
                ?.findByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
            val mainSourceSet = allSourceSets?.findByName("main")

            val sourceSet = commonSourceSet ?: mainSourceSet

            sourceSet
                ?.kotlin
                ?.srcDirs(generateCode.get().outputs.files)
                ?: error(SOURCE_SET_NOT_FOUND)
        }
    }
}

private const val SOURCE_SET_NOT_FOUND =
    "Unable to obtain source set: you should have commonMain/main or custom one that is set up in the [rrpc.targetSourceSet]"