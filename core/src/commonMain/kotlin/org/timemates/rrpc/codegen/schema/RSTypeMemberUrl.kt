package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@Serializable
public data class RSTypeMemberUrl(
    @ProtoNumber(1)
    public val typeUrl: RSDeclarationUrl,
    @ProtoNumber(2)
    public val memberName: String,
) {
    override fun toString(): String {
        return "$typeUrl#$memberName"
    }
}