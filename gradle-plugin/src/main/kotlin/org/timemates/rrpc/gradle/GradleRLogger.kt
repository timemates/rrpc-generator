package org.timemates.rrpc.gradle

import org.gradle.api.logging.Logger
import org.timemates.rrpc.codegen.logging.RLogger

internal class GradleRLogger(
    private val logger: Logger,
    private val header: String,
) : RLogger {
    override suspend fun log(level: RLogger.Level, message: String) {
        when (level) {
            RLogger.Level.DEBUG -> debug(message)
            RLogger.Level.LIFECYCLE -> lifecycle(message)
            RLogger.Level.WARN -> warning(message)
            RLogger.Level.ERROR -> error(message)
        }
    }

    override suspend fun warning(message: String) {
        logger.warn("$header: $message")
    }

    override suspend fun lifecycle(message: String) {
        logger.lifecycle("$header: $message")
    }

    override suspend fun error(message: String) {
        logger.error("$header: $message")
    }

    override suspend fun debug(message: String) {
        logger.debug("$header: $message")
    }

}