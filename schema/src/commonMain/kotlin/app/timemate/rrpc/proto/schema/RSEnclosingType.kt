package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@ConsistentCopyVisibility
@Serializable
public data class RSEnclosingType private constructor(
    @ProtoNumber(1)
    override val name: String = "",
    @ProtoNumber(2)
    override val documentation: String = "",
    @ProtoNumber(3)
    override val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
    @ProtoNumber(4)
    override val nestedExtends: List<RSExtend> = emptyList(),
    @ProtoNumber(5)
    override val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(6)
    override val location: RSElementLocation = RSElementLocation.UNKNOWN,
    @ProtoNumber(7)
    private val nestedMessages: List<RSMessage> = emptyList(),
    @ProtoNumber(8)
    private val nestedEnums: List<RSEnum> = emptyList(),
    @ProtoNumber(9)
    private val nestedEnclosingTypes: List<RSEnclosingType> = emptyList(),
) : RSType {
    public constructor(
        name: String,
        documentation: String,
        typeUrl: RSDeclarationUrl,
        nestedExtends: List<RSExtend> = emptyList(),
        options: RSOptions = RSOptions.EMPTY,
        location: RSElementLocation,
        nestedTypes: List<RSType> = emptyList(),
    ) : this(
        name = name,
        documentation = documentation,
        typeUrl = typeUrl,
        nestedExtends = nestedExtends,
        options = options,
        location = location,
        nestedMessages = nestedTypes.filterIsInstance<RSMessage>(),
        nestedEnums = nestedTypes.filterIsInstance<RSEnum>(),
        nestedEnclosingTypes = nestedTypes.filterIsInstance<RSEnclosingType>(),
    )

    override val nestedTypes: List<RSType> by lazy {
        nestedMessages + nestedEnums + nestedEnclosingTypes
    }

    override fun copy(
        newNestedTypes: List<RSType>,
        newNestedExtends: List<RSExtend>,
        newOptions: RSOptions,
    ): RSType = copy(
        nestedExtends = newNestedExtends,
        options = newOptions,
        nestedTypes = newNestedTypes,
    )

    public fun copy(
        name: String = this.name,
        documentation: String = this.documentation,
        typeUrl: RSDeclarationUrl = this.typeUrl,
        nestedExtends: List<RSExtend> = this.nestedExtends,
        options: RSOptions = this.options,
        location: RSElementLocation = this.location,
        nestedTypes: List<RSType> = this.nestedMessages + this.nestedEnums + this.nestedEnclosingTypes,
    ): RSEnclosingType = RSEnclosingType(
        name = name,
        documentation = documentation,
        typeUrl = typeUrl,
        nestedExtends = nestedExtends,
        options = options,
        location = location,
        nestedTypes = nestedTypes,
    )

}
