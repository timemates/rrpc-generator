package org.timemates.rrpc.codegen.schema.value

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class RSPackageName(
    public val value: String,
) {
    public companion object {
        public val EMPTY: RSPackageName = RSPackageName("")
    }
}