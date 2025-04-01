package org.timemates.rrpc.codegen.plugin.data

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class SignalId(public val id: String) {
    public companion object {
        /**
         * Empty signal ID to use when it's not important to have an ID. Mostly for internal use.
         */
        public val EMPTY: SignalId = SignalId("")
    }
}