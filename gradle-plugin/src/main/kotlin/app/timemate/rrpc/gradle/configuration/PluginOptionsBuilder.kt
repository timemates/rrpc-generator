package app.timemate.rrpc.gradle.configuration

import app.timemate.rrpc.gradle.configuration.type.GenerationPlugin
import app.timemate.rrpc.gradle.configuration.type.PluginOptions
import app.timemate.rrpc.gradle.toStringValueRepresentative


/**
 * Configures generation options.
 *
 * Example usage:
 * ```kotlin
 * add(...) {
 *  /**
 *   * Determines whether package cycle checks are permitted during generation.
 *   *
 *   * By default, this is set to `false`, which raises an error if package cycles are detected.
 *   * Setting it to `true` will allow package cycles without error.
 *   */
 *  option("server_generation", true)
 * }
 * ```
 */
public class PluginOptionsBuilder internal constructor(
    private val options: MutableMap<String, PluginOptions.OptionValue>,
) {
    /**
     * Accepts either object list of permitted types or just permitted type. If
     * option is already occurred, it will create a list with previously assigned value and a new one.
     *
     * Recognizable types:
     * - [java.io.File]
     * - [java.nio.file.Path]
     * - [org.gradle.api.file.FileSystemLocation]
     * - [org.gradle.api.provider.Provider] with values of previously permitted types.
     * - [kotlin.collections.List] of previously permitted types.
     *
     * Any other types will be treated as Strings by calling [Any.toString] method. So, primitive
     * types work, as well as any other that implements meaningful [Any.toString] method with
     * the output that plugin accepts.
     */
    public fun option(key: String, value: Any) {
        if (options.containsKey(key)) {
            val newValue = if (options[key] is List<*>) {
                PluginOptions.OptionValue.Multiple(((options[key] as List<*>) + value).map { it.toStringValueRepresentative() })
            } else {
                PluginOptions.OptionValue.Multiple(listOf(options[key].toStringValueRepresentative(), value.toStringValueRepresentative()))
            }
            options.put(key, newValue)
        } else {
            options.put(key, PluginOptions.OptionValue.Single(value.toStringValueRepresentative()))
        }
    }
}