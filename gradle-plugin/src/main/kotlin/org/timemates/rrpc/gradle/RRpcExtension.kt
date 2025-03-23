package org.timemates.rrpc.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.timemates.rrpc.gradle.configuration.GenOptionsBuilder
import org.timemates.rrpc.gradle.configuration.PluginsBuilder
import org.timemates.rrpc.gradle.configuration.ProtoInputBuilder
import org.timemates.rrpc.gradle.configuration.type.ProtoDependencyType
import org.timemates.rrpc.gradle.configuration.type.ProtoInput
import kotlin.io.path.absolutePathString

/**
 * The main DSL extension for configuring the rRPC Gradle plugin.
 *
 * This extension provides access to configure input sources, plugins, and various generation options
 * for the protocol buffer-based code generation process.
 *
 * @property inputFolders A list of protocol buffer input sources, including directories, archives, or external dependencies.
 * @property plugins A list of generation plugins, which can include built-in plugins like Kotlin or external ones.
 * @property options A map of key-value pairs for configuring plugin-specific options.
 * @property permitPackageCycles A flag that controls whether package cycles are allowed during generation.
 *   Defaults to `false`, which raises an error for package cycles.
 */
@RRpcGradlePluginDsl
public open class RRpcExtension(
    private val project: Project,
    private val sourceDeps: Configuration,
    private val contextDeps: Configuration,
    private val pluginsDeps: Configuration,
) {
    private val objects = project.objects

    /**
     * A list of protocol buffer input sources, configured through the DSL.
     * Inputs can include directories, archives, or external dependencies.
     */
    internal val inputFolders: ConfigurableFileCollection = objects.fileCollection()
    internal val inputFoldersIsContext: MapProperty<String, Boolean> = objects.mapProperty<String, Boolean>()

    /**
     * A map of options used to configure plugin-specific behaviors.
     * Keys represent the option names, and values can be of any type, allowing for flexible configuration.
     */
    internal val options: MapProperty<String, Any> = objects.mapProperty(String::class.java, Any::class.java)

    internal val dependencyTypes: MapProperty<Dependency, ProtoDependencyType> =
        objects.mapProperty(Dependency::class.java, ProtoDependencyType::class.java)

    public val outputFolder: RegularFileProperty = objects.fileProperty()

    /**
     * Configures the protocol buffer inputs for code generation.
     *
     * @param action A configuration block using the `ProtoInputBuilder` DSL.
     * Example usage:
     * ```kotlin
     * inputs {
     *     directory(file("src/main/proto"))
     *     artifact(file("libs/protos.jar"))
     *     artifact("com.example:proto-dependency:1.0.0")
     * }
     * ```
     */
    @RRpcGradlePluginDsl
    public fun inputs(action: ProtoInputBuilder.() -> Unit) {
        ProtoInputBuilder(project.layout.buildDirectory.get().asFile) { protoInput, type ->
            if (protoInput is ProtoInput.Artifact) {
                val dependency = project.dependencies.create(
                    protoInput.notation
                )
                dependencyTypes.put(dependency, type)
                project.dependencies.add(
                    if (type == ProtoDependencyType.CONTEXT) contextDeps.name else sourceDeps.name,
                    dependency
                )
            } else if (protoInput is ProtoInput.Directory) {
                inputFolders.from(protoInput.directory)
                inputFoldersIsContext.put(
                    protoInput.directory.absolutePathString(),
                    type == ProtoDependencyType.CONTEXT
                )
            }
        }.action()
    }

    /**
     * Configures the plugins used for code generation.
     *
     * @param action A configuration block using the `PluginsBuilder` DSL.
     * Example usage:
     * ```kotlin
     * plugins {
     *     add("com.example:typescript-plugin:1.0.0")
     * }
     * ```
     */
    @RRpcGradlePluginDsl
    public fun plugins(action: PluginsBuilder.() -> Unit) {
        PluginsBuilder(project, pluginsDeps).action()
    }

    /**
     * Configures generation options.
     *
     * @param action A configuration block using the `GenOptionsBuilder` DSL. Example usage:
     * ```kotlin
     * options {
     *  /**
     *   * Determines whether package cycle checks are permitted during generation.
     *   *
     *   * By default, this is set to `false`, which raises an error if package cycles are detected.
     *   * Setting it to `true` will allow package cycles without error.
     *   */
     *  append("permit_package_cycles", true)
     * }
     * ```
     */
    @RRpcGradlePluginDsl
    public fun options(action: GenOptionsBuilder.() -> Unit) {
        GenOptionsBuilder(options).action()
    }
}