package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.LocationPath

@Serializable
public data class RSElementLocation(
    @ProtoNumber(1)
    public val basePath: LocationPath,
    /** The path to this location relative to [base].  */
    @ProtoNumber(2)
    val relativePath: LocationPath,

    /** The line number of this location, or -1 for no specific line number.  */
    @ProtoNumber(3)
    val line: Int? = null,

    /** The column on the line of this location, or -1 for no specific column.  */
    @ProtoNumber(4)
    val column: Int? = null,
) {
    override fun toString(): String {
        return buildString {
            if (basePath.value.isNotBlank()) {
                append(basePath)
                append(":")
            }
            if (relativePath.value.isNotBlank()) {
                append(relativePath)
                append(":")
            }
            if (line != null) {
                append(line)
                append(":")
            }
            if (column != null) {
                append(column)
            }
        }
    }
}