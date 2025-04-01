package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@SerialName("ONE_OF")
public data class RSOneOf(
    @ProtoNumber(1)
    public val name: String,
    @ProtoNumber(2)
    public val fields: List<RSField> = emptyList(),
    @ProtoNumber(3)
    val documentation: String? = null,
    @ProtoNumber(4)
    val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(5)
    val location: RSElementLocation,
) : RSNode