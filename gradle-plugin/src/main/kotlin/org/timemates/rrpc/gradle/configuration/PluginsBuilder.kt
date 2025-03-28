package org.timemates.rrpc.gradle.configuration

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.create

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
     *     add("com.example:custom-plugin:1.0.0")
     * }
     * ```
     */
    public fun add(notation: String) {
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

        val dependency = project.dependencies.create(
            notation,
        ) {
            artifact {
                extension = fileExtension
                classifier = osClassifier
            }
        }

        project.dependencies.add(
            configuration.name,
            dependency,
        )
    }
}