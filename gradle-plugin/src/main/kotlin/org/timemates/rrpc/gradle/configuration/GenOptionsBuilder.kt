package org.timemates.rrpc.gradle.configuration

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.timemates.rrpc.gradle.RRpcGradlePluginDsl

public open class GenOptionsBuilder(private val generationOptions: MapProperty<String, Any>) {
    public operator fun set(key: String, value: Any) {
        generationOptions.put(key, value)
    }

    public operator fun get(key: String): Provider<Any?> {
        return generationOptions.getting(key)
    }

    public fun kotlin(block: KotlinConfigurationOptionsBuilder.() -> Unit) {
        KotlinConfigurationOptionsBuilder(generationOptions).apply(block)
    }
}

@RRpcGradlePluginDsl
public class KotlinConfigurationOptionsBuilder(generationOptions: MapProperty<String, Any>) {
    internal companion object {
        const val OUTPUT: String = "kotlin_output"
        const val SERVER_GENERATION: String = "kotlin_server_generation"
        const val CLIENT_GENERATION: String = "kotlin_client_generation"
        const val TYPE_GENERATION: String = "kotlin_server_generation"
        const val METADATA_GENERATION: String = "kotlin_metadata_generation"
        const val METADATA_SCOPE_NAME: String = "kotlin_metadata_scope_name"
    }

    public var output: String
        get() = get(OUTPUT).get() as? String ?: ""
        set(value) = set(OUTPUT, value)

    public var serverGeneration: Boolean
        get() = get(SERVER_GENERATION).get() as? Boolean != false
        set(value) = set(SERVER_GENERATION, value)

    public var clientGeneration: Boolean
        get() = get(CLIENT_GENERATION).get() as? Boolean != false
        set(value) = set(CLIENT_GENERATION, value)

    public var typeGeneration: Boolean
        get() = get(TYPE_GENERATION).get() as? Boolean != false
        set(value) = set(TYPE_GENERATION, value)

    public var metadataEnabled: Boolean
        get() = get(METADATA_GENERATION).get() as? Boolean == true
        set(value) = set(key = METADATA_GENERATION, value = value)

    public var metadataScopeName: String
        get() = get(METADATA_SCOPE_NAME).get() as? String ?: ""
        set(value) = set(METADATA_SCOPE_NAME, value)
}