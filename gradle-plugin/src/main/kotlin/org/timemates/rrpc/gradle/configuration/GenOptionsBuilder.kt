package org.timemates.rrpc.gradle.configuration

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.timemates.rrpc.gradle.RRpcGradlePluginDsl

public class GenOptionsBuilder(private val generationOptions: MapProperty<String, Any>) {
    public fun append(key: String, value: Any) {
        val options = generationOptions.get()
        if (options.containsKey(key)) {
            val newValue = if (options[key] is List<*>) {
                (options[key] as List<*>) + value
            } else {
                options[key] = listOf(options[key], value)
            }
            generationOptions.put(key, newValue)
        }
    }
}