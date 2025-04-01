package org.timemates.rrpc.codegen.logging

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.plugin.PluginCommunication
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import java.io.PrintStream

/**
 * Creates logger that simply prints everything in stdout/stderr.
 *
 * Should be used only in rrgcli. Don't use it in plugins.
 */
public fun LocalRLogger(
    out: PrintStream = System.out,
    err: PrintStream = System.err,
    header: String = "",
    includeDebug: Boolean,
    onError: () -> Unit,
): RLogger = ConsoleRLogger(
    out = out,
    err = err,
    header = header,
    includeDebug = includeDebug,
    onError = onError,
)

/**
 * Creates logger that sends log messages via IPC.
 *
 * Used for plugins.
 */
public fun PluginRLogger(
    communication: PluginCommunication,
    includeDebug: Boolean = false,
): RLogger = IPCRLogger(
    pluginCommunication = communication,
    includeDebug = includeDebug,
)

/**
 * A simple logging utility for code generators and processors.
 *
 * `GenLogger` collects log messages during code generation or processing tasks. It supports common logging
 * levels similar to the Gradle logging system: `WARN`, `LIFECYCLE`, `ERROR`, and `DEBUG`. However, this logger
 * is independent and not connected to Gradle's logging framework.
 *
 * All messages are accumulated in-memory and can be retrieved via the `output` list.
 */
public interface RLogger {
    public suspend fun log(level: Level, message: String)
    /**
     * Logs a warning message.
     *
     * Use this for non-critical issues that may require attention but do not prevent execution.
     *
     * @param message The warning message to be logged.
     */
    public suspend fun warning(message: String)

    /**
     * Logs a lifecycle message.
     *
     * Use this for general informational messages related to the high-level progress or important lifecycle events.
     *
     * @param message The lifecycle message to be logged.
     */
    public suspend fun lifecycle(message: String)

    /**
     * Logs an error message.
     *
     * Use this for critical issues or failures that typically require halting execution or indicating something went wrong.
     *
     * If any message of this type is sent, the result of the plugin is marked as 'failed'.
     *
     * @param message The error message to be logged.
     */
    public suspend fun error(message: String)

    /**
     * Logs a debug message.
     *
     * Use this for verbose output that is typically useful for debugging or in-depth analysis.
     *
     * Messages are not shown unless `--debug` flag is enabled for CLI. For Gradle Plugin
     * applies the same logic.
     *
     * @param message The debug message to be logged.
     */
    public suspend fun debug(message: String)

    @Serializable
    public enum class Level {
        @ProtoNumber(0)
        DEBUG,
        @ProtoNumber(1)
        LIFECYCLE,
        @ProtoNumber(2)
        WARN,
        @ProtoNumber(3)
        ERROR
    }
}