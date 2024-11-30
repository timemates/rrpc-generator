package org.timemates.rrpc.gradle.type

/**
 * Represents a plugin that generates code or metadata during the build process.
 *
 * Plugins can either be built-in (bundled with the system) or external (resolved from external sources).
 */
public sealed interface GenerationPlugin {

    /**
     * Represents built-in plugins that are provided natively by the system and require no external resolution.
     */
    public sealed interface Builtin : GenerationPlugin {
        /**
         * The built-in plugin for generating Kotlin code.
         *
         * This plugin facilitates code generation specifically for Kotlin, enabling features
         * such as client stubs, server stubs, and type generation for rRPC.
         */
        public object Kotlin : Builtin
    }

    /**
     * Represents an external plugin resolved using dependency coordinates.
     *
     * @property coordinates The dependency coordinates of the external plugin (e.g., Maven coordinates
     * in the form `group:artifact:version`) used to fetch the plugin.
     */
    public data class External(public val coordinates: String) : GenerationPlugin
}