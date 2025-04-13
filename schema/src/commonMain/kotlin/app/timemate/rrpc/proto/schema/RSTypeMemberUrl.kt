package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSTypeMemberUrl(
    @ProtoNumber(1)
    public val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
    @ProtoNumber(2)
    public val memberName: String = "",
) {
    override fun toString(): String {
        return "$typeUrl#$memberName"
    }
}