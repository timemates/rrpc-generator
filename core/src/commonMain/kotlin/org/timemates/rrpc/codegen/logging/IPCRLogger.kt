package org.timemates.rrpc.codegen.logging

import org.timemates.rrpc.codegen.logging.RLogger.Level
import org.timemates.rrpc.codegen.plugin.PluginCommunication
import org.timemates.rrpc.codegen.plugin.data.PluginMessage
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.SignalId

internal class IPCRLogger(
    private val pluginCommunication: PluginCommunication,
    private val includeDebug: Boolean,
) : RLogger {
    override suspend fun log(level: Level, msg: String) {
        pluginCommunication.send(
            PluginMessage.create {
                // it's okay to use it here
                id = SignalId.EMPTY
                signal = PluginSignal.LogMessage(msg, level)
            }
        )
    }

    override suspend fun warning(message: String) {
        log(Level.WARN, message)
    }

    override suspend fun lifecycle(message: String) {
        log(Level.LIFECYCLE, message)
    }

    override suspend fun error(message: String) {
        log(Level.ERROR, message)
    }

    override suspend fun debug(message: String) {
        if (!includeDebug)
            log(Level.DEBUG, message)
    }
}