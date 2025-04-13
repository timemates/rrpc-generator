package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Denotes that type might be of streaming type.
 */
@Serializable
public class StreamableRSTypeUrl(
    @ProtoNumber(1)
    public val isStreaming: Boolean = false,
    @ProtoNumber(2)
    public val type: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
)