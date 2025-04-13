package app.timemate.rrpc.gradle.configuration

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.internal.os.OperatingSystem
import app.timemate.rrpc.gradle.configuration.type.GenerationPlugin
import app.timemate.rrpc.gradle.configuration.type.PluginDependencyType
import java.io.File

public class PluginsBuilder internal constructor(
    private val project: Project,
    private val configuration: Configuration,
    private val plugins: ListProperty<GenerationPlugin>,
) {
    /**
     * Configures an external plugin for code generation.
     * **Order is important**: The generation task plugin takes into account the order of plugins that are added,
     * meaning that, for example, if you have a plugin that modifies the input, it should come before the plugin that
     * should generate based on this modified input.
     *
     * This method adds an external plugin, identified by its dependency coordinates, to the list of plugins
     * and provides a generic DSL scope for setting its configuration options.
     *
     * The plugin is added to the Gradle dependencies and can be configured further using the `optionsBuilder`
     * block, which applies plugin-specific configuration options.
     * The `notation` parameter accepts a wide range
     * of dependency types and is resolved into a Gradle dependency using the [resolveDependency] function.
     *
     * ## Supported Dependency Types
     * The following types are supported for the `notation` parameter:
     * -
     * **String**:
     * A standard dependency notation string in the form of
     * `group:artifact:version` (e.g., `"com.example:custom-plugin:1.0.0"`).
     * - **Provider<MinimalExternalModuleDependency>**:
     * A Gradle `Provider` wrapping a `MinimalExternalModuleDependency`,
     * which typically comes from version catalogs.
     * - **ModuleDependency / ExternalModuleDependency**: These represent module dependencies, including external modules defined by their coordinates (`group:name:version`).
     * - **ProjectDependency**: A dependency pointing to a local Gradle project.
     * - **FileCollectionDependency**: A collection of files (e.g., JARs, binaries) that will be used as dependencies.
     *
     * The [resolveDependency] function resolves the provided `notation` into a proper Gradle dependency and applies
     * necessary adjustments for binary dependencies.
     * For binary dependencies (when `type` is `PluginDependencyType.BINARY`),
     * platform-specific classifiers (e.g., `linux-x86_64`, `windows-x86_64`)
     * are added to the dependency artifacts.
     *
     * ## Example Usage
     * ```kotlin
     * plugins {
     *     add("com.example:custom-plugin:1.0.0") {
     *      option("server_generation", false)
     *     }
     * }
     * ```
     *
     * In this example,
     * the `add` function is called with a String dependency notation `"com.example:custom-plugin:1.0.0"`,
     * which will be resolved into a Gradle dependency.
     * If the plugin is binary, the appropriate classifier and extension
     * (e.g., `linux-x86_64`) will be applied.
     *
     * @param notation The dependency notation of the external plugin. Accepted types are:
     * - String (e.g., "com.example:custom-plugin:1.0.0")
     * - Provider<MinimalExternalModuleDependency> (from version catalogs)
     * - ModuleDependency / ExternalModuleDependency
     * - ProjectDependency
     * - FileCollectionDependency
     * @param type The type of dependency: JAR or BINARY. BINARY dependencies will have the platform-specific
     * classifier (and extension for Windows) applied by the [resolveDependency] function.
     * @param optionsBuilder A configuration block for specifying plugin-specific options.
     *
     * @see resolveDependency
     */
    public fun add(
        notation: Any,
        type: PluginDependencyType = PluginDependencyType.JAR,
        optionsBuilder: PluginOptionsBuilder.() -> Unit = {},
    ) {
        val dependency = resolveDependency(notation, type)
        val options = mutableMapOf<String, Any>().apply {
            PluginOptionsBuilder(this).apply(optionsBuilder)
        }.toMap()
        val generationPlugin = GenerationPlugin(dependency, options)
        plugins.add(generationPlugin)
        project.dependencies.add(configuration.name, dependency)
    }

    /**
     * Resolves the provided notation into a Gradle Dependency, applying binary adjustments if needed.
     *
     * This function takes a dependency notation (e.g., group:name:version or a `FileCollectionDependency`) and
     * resolves it into a Gradle dependency, which is returned as a [Dependency] object. If the dependency is of
     * type BINARY, special adjustments are applied to ensure that the correct binary classifier (e.g., for OS
     * specific binaries like `linux-x86_64`, `windows-x86_64`, etc.) is added to the artifact.
     *
     * The function supports multiple forms of dependency notations and handles each according to its type. It
     * uses Gradle's [Project.dependencies.create] method to create dependencies from provided notations and applies
     * binary classifiers when necessary. Binary dependencies are resolved with proper OS-specific classifiers and file
     * extensions where applicable.
     *
     * ## Supported Notation Types
     * - **String**: A standard dependency notation string in the form of `group:artifact:version`.
     * - **Provider**: A Gradle [Provider] that wraps a dependency notation (can be a String or a custom dependency).
     * - **MinimalExternalModuleDependency**: Represents an external module dependency with a module identifier.
     * - **ModuleDependency**: A standard module dependency object containing coordinates (`group`, `name`, `version`).
     * - **ExternalModuleDependency**: Represents an external module with a dependency on the module coordinates.
     * - **ProjectDependency**: A dependency that points to a local project.
     * - **FileCollectionDependency**: A collection of files (e.g., JARs, binaries) that are included as dependencies.
     *
     * ## Binary Handling
     * When a dependency is of type [PluginDependencyType.BINARY], the function ensures that the appropriate
     * binary classifier is added to the artifact. The classifier is determined based on the operating system where
     * the Gradle build is running. Common classifiers include:
     * - `linux-x86_64` for Linux
     * - `windows-x86_64` for Windows (with `.exe` extension)
     * - `macos-aarch64` for macOS
     *
     * For non-binary dependencies (e.g., `.jar` files), no classifier is added, and the dependency is handled as usual.
     *
     * The `FileCollectionDependency` type is handled specially: when it's a binary (but not a JAR), the function
     * appends the classifier to the file name before the file extension or at the end of the name if no extension exists.
     *
     * ## Dependency Creation Workflow
     * - For **String**, **Provider**, **MinimalExternalModuleDependency**, **ModuleDependency**, and **ExternalModuleDependency**,
     *   Gradle dependencies are created using the [project.dependencies.create] method, which internally resolves the dependency.
     * - For **ProjectDependency**, no creation is needed, as it’s already a Gradle project dependency.
     * - For **FileCollectionDependency**, if it’s a binary, the files are iterated through, and the classifier is appended to the file names as necessary.
     *
     * The function also ensures that duplicate artifacts are avoided, especially for **ModuleDependency** types,
     * by clearing any existing artifacts and adding the necessary one with the classifier.
     *
     * ## Example Usage
     * ```kotlin
     * resolveDependency("com.example:plugin:1.0.0", PluginDependencyType.BINARY)
     * ```
     * This will resolve a dependency on the plugin `com.example:plugin:1.0.0`, adding the appropriate binary classifier
     * for the current operating system (e.g., `linux-x86_64`, `windows-x86_64`, etc.).
     *
     * ## Notes
     * - If the `notation` is a `Provider`, the value is first extracted from the provider and resolved into a dependency.
     * - For a `FileCollectionDependency`, the function will append the correct classifier to the files in the collection,
     *   ensuring that binary dependencies are properly handled with their OS-specific classifiers.
     * - The `ModuleDependency` and `ExternalModuleDependency` types are resolved similarly, but their classifier
     *   is only applied when the `PluginDependencyType.BINARY` is set.
     *
     * @param notation The dependency notation, which can be of various types (String, Provider, ModuleDependency, etc.).
     * @param type The type of dependency being resolved. It determines whether the binary classifier should be applied.
     * @return The resolved [Dependency] object, which is used by Gradle to manage the dependency in the build.
     */
    private fun resolveDependency(notation: Any, type: PluginDependencyType): Any {
        val (osClassifier, fileExtension) = if (type == PluginDependencyType.BINARY)
            getBinaryClassifierAndExtension()
        else
            null to null

        fun configureClassifierIfBinary(dep: Dependency): Dependency {
            if (type != PluginDependencyType.BINARY) return dep
            return when (dep) {
                is ModuleDependency -> {
                    dep.artifacts.clear() // Prevent duplicate artifacts
                    dep.addArtifact(
                        DefaultDependencyArtifact(
                            dep.name,
                            fileExtension ?: "",
                            null,
                            osClassifier,
                            null
                        )
                    )
                    dep
                }
                else -> dep
            }
        }

        return when (notation) {
            is Provider<*> -> {
                notation.map { notation ->
                    project.dependencies.create(resolveDependency(notation, type)).also {
                        configureClassifierIfBinary(it)
                    }
                }
            }

            is String -> {
                val dep = project.dependencies.create(notation)
                configureClassifierIfBinary(dep)
            }

            is MinimalExternalModuleDependency -> {
                val module = notation.module
                val coords = buildString {
                    append(module.group)
                    append(':')
                    append(module.name)
                    if (!notation.version.isNullOrBlank()) {
                        append(':')
                        append(notation.version)
                    }
                }
                val dep = project.dependencies.create(coords)
                configureClassifierIfBinary(dep)
            }

            is ProjectDependency -> notation

            is ExternalModuleDependency -> {
                val dep = project.dependencies.create(notation)
                configureClassifierIfBinary(dep)
            }

            is ModuleDependency -> {
                val dep = project.dependencies.create(notation)
                configureClassifierIfBinary(dep)
            }

            is FileCollectionDependency -> {
                if (type == PluginDependencyType.BINARY) {
                    val updatedFiles = notation.files.map { file ->
                        if (file.extension == "jar") return@map file

                        val nameWithoutExt = file.nameWithoutExtension
                        val parent = file.parentFile
                        val finalName = if (file.extension.isNotEmpty()) {
                            "$nameWithoutExt-$osClassifier.${file.extension}"
                        } else {
                            "$nameWithoutExt-$osClassifier${fileExtension.orEmpty()}"
                        }

                        File(parent, finalName)
                    }

                    project.dependencies.create(project.files(updatedFiles))
                } else {
                    notation
                }
            }

            else -> error("Unsupported dependency type: ${notation::class.simpleName}")
        }
    }
}


private fun getBinaryClassifierAndExtension(): Pair<String, String> {
    return when {
        OperatingSystem.current().isLinux -> "linux-x86_64" to ""
        OperatingSystem.current().isWindows -> "windows-x86_64" to ".exe"
        OperatingSystem.current().isMacOsX -> "macos-aarch64" to ""
        else -> throw GradleException("Unsupported OS: ${OperatingSystem.current().name}")
    }
}