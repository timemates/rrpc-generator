package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@Serializable
public data class RSService(
    /**
     * Name of the service.
     */
    @ProtoNumber(1)
    public val name: String = "",

    /**
     * List of RPCs (Remote Procedure Calls) defined in this service.
     */
    @ProtoNumber(2)
    public val rpcs: List<RSRpc> = emptyList(),

    /**
     * Options on service-level.
     */
    @ProtoNumber(3)
    public val options: RSOptions = RSOptions.EMPTY,

    /**
     * String reference representation.
     */
    @ProtoNumber(4)
    public val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,

    /**
     * Specifies where is the service located in filesystem.
     */
    @ProtoNumber(5)
    public val location: RSElementLocation = RSElementLocation.UNKNOWN,
) : RSNode
