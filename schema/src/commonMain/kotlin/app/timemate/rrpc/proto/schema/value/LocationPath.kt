package app.timemate.rrpc.proto.schema.value

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class LocationPath(public val value: String) {
    public companion object {
        public val EMPTY: LocationPath = LocationPath("")
    }

    override fun toString(): String {
        return value
    }
}