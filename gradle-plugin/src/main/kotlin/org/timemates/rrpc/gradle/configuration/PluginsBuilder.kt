package org.timemates.rrpc.gradle.configuration

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.provider.MapProperty
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.create
import org.timemates.rrpc.gradle.configuration.type.PluginDependencyType

/**
 * A builder class for configuring plugins and their options for code generation.
 *
 * This class allows adding both built-in and external plugins, providing specific configuration options for each.
 * The configuration options are stored as key-value pairs and are specific to the selected plugins.
 *
 * @property list A property that holds a list of plugins to be used during code generation.
 * @property options A map of configuration options to customize the behavior of the plugins.
 */
public class PluginsBuilder(
    private val project: Project,
    private val configuration: Configuration,
    private val options: MapProperty<ModuleIdentifier, Map<String, Any>>,
) {
    /**
     * Configures an external plugin for code generation.
     *
     * This method adds an external plugin, identified by its dependency coordinates, to the list of plugins
     * and provides a generic DSL scope for setting its configuration options.
     *
     * @param coordinates The dependency coordinates of the external plugin (e.g., `group:artifact:version`).
     * @param block A configuration block for specifying plugin-specific options.
     * Example usage:
     * ```kotlin
     * plugins {
     *     jar("com.example:custom-plugin:1.0.0")
     * }
     * ```
     */
    public fun add(
        notation: String,
        type: PluginDependencyType = PluginDependencyType.JAR,
        optionsBuilder: PluginOptionsBuilder.() -> Unit = {},
    ) {
        val dependency = when (type) {
            PluginDependencyType.BINARY -> binary(notation)
            else -> project.dependencies.create(
                dependencyNotation = notation,
            ) {
                artifact {
                    extension = "jar"
                }
            }
        }

        options.put(
            dependency.module,
            mutableMapOf<String, Any>().apply {
                PluginOptionsBuilder(this).apply(optionsBuilder)
            }.toMap(),
        )

        project.dependencies.add(
            configuration.name,
            dependency,
        )
    }

    private fun binary(notation: String): ExternalModuleDependency {
        var osClassifier: String
        var fileExtension: String

        when {
            OperatingSystem.current().isLinux -> {
                osClassifier = "linux-x86_64"
                fileExtension = "" // No extension for Linux
            }

            OperatingSystem.current().isWindows -> {
                osClassifier = "windows-x86_64"
                fileExtension = ".exe" // Windows needs .exe
            }

            OperatingSystem.current().isMacOsX -> {
                osClassifier = "macos-aarch64"
                fileExtension = "" // No extension for macOS
            }

            else -> throw GradleException("Unsupported OS: ${OperatingSystem.current()}")
        }

        return project.dependencies.create(
            notation,
        ) {
            artifact {
                extension = fileExtension
                classifier = osClassifier
            }
        }
    }
}