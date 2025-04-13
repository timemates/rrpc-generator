package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSExtend(
    @ProtoNumber(1)
    public val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
    @ProtoNumber(2)
    public val name: String = "",
    @ProtoNumber(3)
    public val fields: List<RSField> = emptyList(),
    @ProtoNumber(4)
    override val documentation: String = "",
    @ProtoNumber(5)
    public val location: RSElementLocation = RSElementLocation.UNKNOWN,
) : Documentable, RSNode
