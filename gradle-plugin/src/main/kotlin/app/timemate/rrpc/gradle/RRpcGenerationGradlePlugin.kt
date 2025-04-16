package app.timemate.rrpc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import app.timemate.rrpc.gradle.task.GenerateRRpcCodeTask
import app.timemate.rrpc.gradle.task.RRpcGeneratorHelpTask
import org.gradle.kotlin.dsl.register

public class RRpcGenerationGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val sourceProtoDeps: Configuration = target.configurations.create("rrpcSourceProto") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            }
        }

        val contextProtoDeps: Configuration = target.configurations.create("rrpcContextProto") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            }
        }

        val rrpcPluginsDeps: Configuration = target.configurations.create("rrpcPlugin") {
            isVisible = false
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        val extension =
            target.extensions.create<RRpcExtension>("rrpc", target, sourceProtoDeps, contextProtoDeps, rrpcPluginsDeps)
        target.tasks.register<GenerateRRpcCodeTask>("generateRRpcCode") {
            this.inputs.set(extension.inputs)
            this.inputFolders.from(extension.inputFolders)
            this.permitPackageCycles.set(extension.permitPackageCycles)
            this.plugins.addAll(extension.registeredPlugins)
            this.contextProtoDeps.from(contextProtoDeps.incoming.files)
            this.sourceProtoDeps.from(sourceProtoDeps.incoming.files)
            this.rrpcPluginsDeps.from(rrpcPluginsDeps.incoming.files)
            this.outputDir.set(extension.outputFolder)

            doFirst {
                rrpcPluginsDeps.resolve()
            }
        }

        target.tasks.register<RRpcGeneratorHelpTask>("rrpcGeneratorHelp") {
            this.rrpcPluginsDeps.from(rrpcPluginsDeps.incoming.files)
            this.plugins.addAll(extension.registeredPlugins)

            doFirst {
                rrpcPluginsDeps.resolve()
            }
        }
    }
}