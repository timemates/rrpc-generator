package org.timemates.rrpc.gradle

import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import java.io.File

public class RRpcGenerationGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<RRpcExtension>("rrpc", target.objects)

        val generateCode = target.tasks.create("generateCode") {
            group = "rrpc"

            val outputsProvider =
                extension.pluginConfigurations.flatMap<List<String>> { configurations ->
                    val allOptions = configurations.flatMap<_, String> { configuration ->
                        configuration.options.map.filter { it.filter { entry -> entry.key.endsWith("output") } }
                            .map { map -> map.map { it.value } }
                    }
                    Providers.of(allOptions).map { listOfMaps ->
                        listOfMaps.flatten()
                    }
                }

            inputs.dir(extension.protosInput)
            // сюда загнать хочу
            outputs.dir(outputsProvider)

//            doLast {
//                val codeGenerator = CodeGenerator(FileSystem.SYSTEM).generate(
//                    options = GenerationOptions.create {
//
//                    },
//                    adapters = mapOf()
//                )
//
//                try {
//                    generationOutputPath.get()
//                        .asFile
//                        .listFiles()
//                        ?.forEach(File::deleteRecursively)
//                } catch (e: Exception) {
//                    if (logger.isDebugEnabled)
//                        logger.error(e.stackTraceToString())
//                }
//
//                codeGenerator.generate(
//                    configuration = RMGlobalConfiguration(
//                        inputs = target.file(extension.paths.protoSources.get()).toOkioPath(),
//                        output = target.file(generationOutputPath).toOkioPath(),
//                        clientGeneration = extension.profiles.client.get(),
//                        serverGeneration = extension.profiles.server.get(),
//                        builderTypes = extension.options.builderTypes.get(),
//                        permitPackageCycles = extension.options.permitPackageCycles.get(),
//                    )
//                )
//            }
//        }
//
//        target.afterEvaluate {
//            val allSourceSets = target.extensions.findByType<KotlinMultiplatformExtension>()?.sourceSets
//                ?: target.extensions.findByType<KotlinJvmProjectExtension>()?.sourceSets
//                ?: target.extensions.findByType<KotlinAndroidProjectExtension>()?.sourceSets
//                ?: error("Does Kotlin plugin apply to the buildscript?")
//
//            val commonSourceSet = allSourceSets
//                .findByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
//            val mainSourceSet = allSourceSets.findByName("main")
//
//            val sourceSet = if (extension.targetSourceSet.getOrNull() != null)
//                allSourceSets.getByName(extension.targetSourceSet.get())
//            else commonSourceSet ?: mainSourceSet
//
//            sourceSet
//                ?.kotlin
//                ?.srcDirs(generateCode.outputs)
//                ?: error(SOURCE_SET_NOT_FOUND)
        }
    }
}

private const val SOURCE_SET_NOT_FOUND =
    "Unable to obtain source set: you should have commonMain/main or custom one that is set up in the [rrpc.targetSourceSet]"