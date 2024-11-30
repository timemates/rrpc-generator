package org.timemates.rrpc.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.timemates.rrpc.gradle.configuration.PluginsBuilder
import org.timemates.rrpc.gradle.configuration.ProtoInputBuilder
import org.timemates.rrpc.gradle.type.GenerationPlugin
import org.timemates.rrpc.gradle.type.ProtoInput

/**
 * The main DSL extension for configuring the rRPC Gradle plugin.
 *
 * This extension provides access to configure input sources, plugins, and various generation options
 * for the protocol buffer-based code generation process.
 *
 * @property inputs A list of protocol buffer input sources, including directories, archives, or external dependencies.
 * @property plugins A list of generation plugins, which can include built-in plugins like Kotlin or external ones.
 * @property options A map of key-value pairs for configuring plugin-specific options.
 * @property permitPackageCycles A flag that controls whether package cycles are allowed during generation.
 *   Defaults to `false`, which raises an error for package cycles.
 */
@RRpcGradlePluginDsl
public open class RRpcExtension(objects: ObjectFactory) {

    /**
     * A list of protocol buffer input sources, configured through the DSL.
     * Inputs can include directories, archives, or external dependencies.
     */
    public val inputs: ListProperty<ProtoInput> = objects.listProperty(ProtoInput::class.java)

    /**
     * A list of generation plugins to be applied.
     * Plugins can include built-in options (e.g., Kotlin) or external plugins defined by dependency coordinates.
     */
    public val plugins: ListProperty<GenerationPlugin> = objects.listProperty(GenerationPlugin::class.java)

    /**
     * A map of options used to configure plugin-specific behaviors.
     * Keys represent the option names, and values can be of any type, allowing for flexible configuration.
     */
    public val options: MapProperty<String, Any> = objects.mapProperty(String::class.java, Any::class.java)

    /**
     * Configures the protocol buffer inputs for code generation.
     *
     * @param action A configuration block using the `ProtoInputBuilder` DSL.
     * Example usage:
     * ```kotlin
     * inputs {
     *     directory(file("src/main/proto"))
     *     artifact(file("libs/protos.jar"))
     *     external("com.example:proto-dependency:1.0.0")
     * }
     * ```
     */
    public fun inputs(action: ProtoInputBuilder.() -> Unit) {
        ProtoInputBuilder(inputs).action()
    }

    /**
     * Configures the plugins used for code generation.
     *
     * @param action A configuration block using the `PluginsBuilder` DSL.
     * Example usage:
     * ```kotlin
     * plugins {
     *     kotlin {
     *         output = "build/generated/kotlin"
     *         clientGeneration = true
     *     }
     *     external("com.example:typescript-plugin:1.0.0") {
     *         put("customOption", "value")
     *     }
     * }
     * ```
     */
    public fun plugins(action: PluginsBuilder.() -> Unit) {
        PluginsBuilder(plugins, options).action()
    }

    /**
     * Determines whether package cycle checks are permitted during generation.
     *
     * By default, this is set to `false`, which raises an error if package cycles are detected.
     * Setting it to `true` will allow package cycles without error.
     */
    public val permitPackageCycles: Property<Boolean> = objects.property<Boolean>()
        .convention(false)
}