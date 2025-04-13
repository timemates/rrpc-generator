package app.timemate.rrpc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import app.timemate.rrpc.gradle.task.GenerateRRpcCodeTask
import app.timemate.rrpc.gradle.task.RRpcGeneratorHelpTask
import app.timemate.rrpc.gradle.task.UnpackProtoDependenciesTask
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

        val unpackTask = target.tasks.register<UnpackProtoDependenciesTask>("unpackProtoDependencies") {
            this.inputs.set(extension.inputs)
            this.inputFolders.from(extension.inputFolders)
            this.contextDependencies.from(contextProtoDeps.incoming.files)
            this.sourceDependencies.from(sourceProtoDeps.incoming.files)
            this.dependenciesOutputDirectory.set(project.layout.buildDirectory.dir("extracted/rrpc-deps"))

            doFirst {
                contextProtoDeps.resolve()
                sourceProtoDeps.resolve()
            }
        }

        target.tasks.register<GenerateRRpcCodeTask>("generateRRpcCode") {
            dependsOn(unpackTask)

            this.dependenciesProtoFolder.fileProvider(unpackTask.map { it.dependenciesOutputDirectory.asFile.get() })
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