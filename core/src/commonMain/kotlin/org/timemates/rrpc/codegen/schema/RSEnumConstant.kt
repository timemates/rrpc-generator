package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSEnumConstant(
    @ProtoNumber(1)
    val name: String,
    @ProtoNumber(2)
    val tag: Int,
    @ProtoNumber(3)
    public val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(4)
    override val documentation: String? = null,
    @ProtoNumber(5)
    public val location: RSElementLocation,
) : RSNode, Documentable