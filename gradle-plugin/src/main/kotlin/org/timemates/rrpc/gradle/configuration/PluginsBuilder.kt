package org.timemates.rrpc.gradle.configuration

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.timemates.rrpc.gradle.type.GenerationPlugin

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
    private val list: ListProperty<GenerationPlugin>,
    private val options: MapProperty<String, Any>
) {

    /**
     * Configures the built-in Kotlin plugin for code generation.
     *
     * This method adds the `Kotlin` plugin to the list of plugins and provides a DSL
     * scope for setting configuration options specific to Kotlin code generation.
     *
     * @param block A configuration block for setting Kotlin-specific options.
     * Example usage:
     * ```kotlin
     * plugins {
     *     kotlin {
     *         // Configure Kotlin-specific options here
     *         output = "build/generated/kotlin"
     *     }
     * }
     * ```
     */
    public fun kotlin(block: KotlinConfigurationOptionsBuilder.() -> Unit) {
        list.add(GenerationPlugin.Builtin.Kotlin)
        KotlinConfigurationOptionsBuilder(options).apply(block)
    }

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
     *     external("com.example:custom-plugin:1.0.0") {
     *         put("customOption", "value")
     *     }
     * }
     * ```
     */
    public fun external(coordinates: String, block: GenOptionsBuilder.() -> Unit) {
        list.add(GenerationPlugin.External(coordinates))
        GenOptionsBuilder(options).apply(block)
    }
}