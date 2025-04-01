package org.timemates.rrpc.gradle

import kotlinx.coroutines.*
import okio.FileSystem
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.plugin.data.toOptionDescriptor
import org.timemates.rrpc.generator.plugin.loader.ProcessPluginService
import org.timemates.rrpc.gradle.task.GenerateRRpcCodeTask
import org.timemates.rrpc.gradle.task.RRpcGeneratorHelpTask
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

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
        }

        val extension =
            target.extensions.create<RRpcExtension>("rrpc", target, sourceProtoDeps, contextProtoDeps, rrpcPluginsDeps)

        target.tasks.register<GenerateRRpcCodeTask>("generateRRpcCode") {
            inputFolders.from(extension.inputFolders)
            inputFoldersIsContext.set(extension.inputFoldersIsContext)
            this.permitPackageCycles.set(extension.permitPackageCycles)
            this.options.putAll(extension.pluginOptions)
            this.contextProtoDeps.from(contextProtoDeps.incoming.files)
            this.sourceProtoDeps.from(sourceProtoDeps.incoming.files)
            this.rrpcPluginsDeps.from(rrpcPluginsDeps.incoming.files)
            this.outputDir.set(extension.outputFolder)

            doFirst {
                rrpcPluginsDeps.resolve()
                contextProtoDeps.resolve()
                sourceProtoDeps.resolve()
            }
        }

        target.tasks.register<RRpcGeneratorHelpTask>("rrpcGeneratorHelp") {
            this.rrpcPluginsDeps.from(rrpcPluginsDeps.incoming.files)
            this.options.set(extension.pluginOptions)

            doFirst {
                rrpcPluginsDeps.resolve()
            }
        }
    }
}