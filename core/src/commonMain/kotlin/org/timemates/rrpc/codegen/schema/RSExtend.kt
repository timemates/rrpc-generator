package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@Serializable
public data class RSExtend(
    @ProtoNumber(1)
    public val typeUrl: RSDeclarationUrl,
    @ProtoNumber(2)
    public val name: String,
    @ProtoNumber(3)
    public val fields: List<RSField> = emptyList(),
    @ProtoNumber(4)
    override val documentation: String? = null,
) : Documentable, RSNode
