package app.timemate.rrpc.gradle.configuration


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
public class PluginOptionsBuilder(
    private val options: MutableMap<String, Any>,
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
                (options[key] as List<*>) + value
            } else {
                options[key] = listOf(options[key], value)
            }
            options.put(key, newValue)
        }
    }
}