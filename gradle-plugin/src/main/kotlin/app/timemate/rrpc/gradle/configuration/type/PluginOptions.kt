package app.timemate.rrpc.gradle.configuration.type

import java.io.Serializable

internal data class PluginOptions(val value: Map<String, OptionValue>) : Serializable {
    operator fun get(key: String): OptionValue? {
        return value[key]
    }

    sealed interface OptionValue : Serializable {
        data class Single(val value: String) : OptionValue
        data class Multiple(val value: List<String>) : OptionValue
    }
}