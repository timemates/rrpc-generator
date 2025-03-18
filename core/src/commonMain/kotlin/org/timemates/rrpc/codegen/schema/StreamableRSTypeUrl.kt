package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

/**
 * Denotes that type might be of streaming type.
 */
@Serializable
public class StreamableRSTypeUrl(
    @ProtoNumber(1)
    public val isStreaming: Boolean,
    @ProtoNumber(2)
    public val type: RSDeclarationUrl,
)