package app.timemate.rrpc.gradle.configuration.type

import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency

/**
 * Represents a plugin and its configuration used during code generation in the rRPC Gradle plugin.
 *
 * This internal class serves as a structured, intermediate representation of a codegen plugin before it is
 * resolved or loaded. It holds:
 *
 * - a Gradle [org.gradle.api.artifacts.Dependency] object used to identify the plugin artifact (either a JAR or native binary), and
 * - a map of plugin-specific configuration options.
 *
 * At this stage, the [dependency] is not yet resolved. It is primarily used as a unique identifier until the
 * plugin is loaded—at which point its actual name (used for namespacing options) becomes available.
 *
 * The options are scoped to the plugin and are later passed to the generator during execution (e.g., `generateCode`
 * or `help` tasks).
 *
 * @property dependency The Gradle [org.gradle.api.artifacts.Dependency] that identifies the plugin. Not resolved at this stage.
 * @property options A map of plugin-specific options defined via the DSL.
 */
internal data class GenerationPlugin(
    val dependency: Any,
    val options: Map<String, Any>,
)

internal val GenerationPlugin.isAsModule: Boolean
    get() = dependency is ModuleDependency

internal val GenerationPlugin.isInProject: Boolean
    get() = dependency is ProjectDependency

internal val GenerationPlugin.isOnFileSystem: Boolean
    get() = dependency is FileCollectionDependency