package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@Serializable
public data class RSField(
    @ProtoNumber(1)
    public val tag: Int,
    @ProtoNumber(2)
    public val name: String,
    @ProtoNumber(3)
    public val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(4)
    override val documentation: String?,
    @ProtoNumber(5)
    public val typeUrl: RSDeclarationUrl,
    @ProtoNumber(6)
    public val isRepeated: Boolean,
    @ProtoNumber(7)
    public val isInOneOf: Boolean = false,
    @ProtoNumber(8)
    public val isExtension: Boolean = false,
    @ProtoNumber(9)
    public val location: RSElementLocation,
) : Documentable, RSNode

