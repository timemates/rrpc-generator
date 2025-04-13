package app.timemate.rrpc.generator.plugin.api.logger

import app.timemate.rrpc.generator.plugin.api.communication.PluginCommunication
import app.timemate.rrpc.generator.plugin.api.communication.PluginMessage
import app.timemate.rrpc.generator.plugin.api.communication.PluginSignal
import app.timemate.rrpc.generator.plugin.api.communication.SignalId

internal class IPCRLogger(
    private val pluginCommunication: PluginCommunication,
    override val isDebugEnabled: Boolean,
) : RLogger {
    override suspend fun log(level: RLogger.Level, msg: String) {
        pluginCommunication.send(
            PluginMessage.create {
                // it's okay to use it here
                id = SignalId.EMPTY
                signal = PluginSignal.LogMessage(msg, level)
            }
        )
    }

    override suspend fun warning(message: String) {
        log(RLogger.Level.WARN, message)
    }

    override suspend fun lifecycle(message: String) {
        log(RLogger.Level.LIFECYCLE, message)
    }

    override suspend fun error(message: String) {
        log(RLogger.Level.ERROR, message)
    }

    override suspend fun debug(message: String) {
        if (!isDebugEnabled)
            log(RLogger.Level.DEBUG, message)
    }
}