package app.timemate.rrpc.generator.cli

import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import java.io.PrintStream

internal class ConsoleRLogger(
    private val out: PrintStream = System.out,
    private val err: PrintStream = System.err,
    override val isDebugEnabled: Boolean,
    private val onError: () -> Unit,
    header: String,
): RLogger {
    private val header = header.takeIf { it.isNotBlank() }?.plus(": ") ?: ""
    override suspend fun log(level: RLogger.Level, message: String) {
        when (level) {
            RLogger.Level.DEBUG -> debug(message)
            RLogger.Level.LIFECYCLE -> lifecycle(message)
            RLogger.Level.WARN -> warning(message)
            RLogger.Level.ERROR -> error(message)
        }
    }

    override suspend fun warning(message: String) {
        out.println("[WARN] $header$message")
    }

    override suspend fun lifecycle(message: String) {
        out.println("$header$message")
    }

    override suspend fun error(message: String) {
        err.println("[WARN] $header$message")
        onError()
    }

    override suspend fun debug(message: String) {
        if (isDebugEnabled) {
            out.println("[DEBUG] $header$message")
        }
    }
}