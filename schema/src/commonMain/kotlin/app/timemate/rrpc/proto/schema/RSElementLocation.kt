package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.LocationPath
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSElementLocation private constructor(
    @ProtoNumber(1)
    public val basePath: LocationPath = LocationPath.EMPTY,
    /** The path to this location relative to [base].  */
    @ProtoNumber(2)
    val relativePath: LocationPath = LocationPath.EMPTY,

    /** The line number of this location, or -1 for no specific line number.  */
    @ProtoNumber(3)
    private val _line: Int = -1,

    /** The column on the line of this location, or -1 for no specific column.  */
    @ProtoNumber(4)
    private val _column: Int = -1,
) {
    public companion object {
        public val UNKNOWN: RSElementLocation = RSElementLocation()
    }

    public constructor(
        basePath: LocationPath,
        relativePath: LocationPath,
        line: Int? = null,
        column: Int? = null,
    ) : this(basePath, relativePath, line ?: -1, column ?: -1)

    public val line: Int? get() = _line.takeUnless { it == -1 }
    public val column: Int? get() = _column.takeIf { it != -1 }

    public fun copy(
        basePath: LocationPath = this.basePath,
        relativePath: LocationPath = this.relativePath,
        line: Int? = this.line,
        column: Int? = this.column,
    ): RSElementLocation = RSElementLocation(basePath, relativePath, line, column)

    override fun toString(): String {
        if (basePath.value.isBlank() && relativePath.value.isBlank())
            return "unknown"

        return buildString {
            if (basePath.value.isNotBlank()) {
                append(basePath)
                append(":")
            }
            if (relativePath.value.isNotBlank()) {
                append(relativePath)
            }
            if (line != null) {
                append(":")
                append(line)
            }
            if (column != null) {
                append(":")
                append(column)
            }
        }
    }
}