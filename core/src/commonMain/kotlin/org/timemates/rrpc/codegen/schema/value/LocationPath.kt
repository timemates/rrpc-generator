package org.timemates.rrpc.codegen.schema.value

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class LocationPath(public val value: String) {
    override fun toString(): String {
        return value
    }
}