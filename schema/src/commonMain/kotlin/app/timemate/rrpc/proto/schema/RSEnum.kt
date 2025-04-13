package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@ConsistentCopyVisibility
@Serializable
public data class RSEnum private constructor(
    @ProtoNumber(1)
    override val name: String = "",
    @ProtoNumber(2)
    val constants: List<RSEnumConstant> = emptyList(),
    @ProtoNumber(3)
    override val documentation: String = "",
    @ProtoNumber(4)
    override val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(5)
    override val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
    @ProtoNumber(6)
    override val nestedExtends: List<RSExtend> = emptyList(),
    @ProtoNumber(7)
    override val location: RSElementLocation = RSElementLocation.UNKNOWN,
    @ProtoNumber(8)
    private val nestedMessages: List<RSMessage> = emptyList(),
    @ProtoNumber(9)
    private val nestedEnums: List<RSEnum> = emptyList(),
    @ProtoNumber(10)
    private val nestedEnclosingTypes: List<RSEnclosingType> = emptyList(),
) : RSType {

    public constructor(
        name: String,
        constants: List<RSEnumConstant> = emptyList(),
        documentation: String,
        options: RSOptions,
        typeUrl: RSDeclarationUrl,
        nestedExtends: List<RSExtend> = emptyList(),
        nestedTypes: List<RSType>,
        location: RSElementLocation,
    ) : this(
        name = name,
        constants = constants,
        documentation = documentation,
        options = options,
        typeUrl = typeUrl,
        nestedExtends = nestedExtends,
        nestedMessages = nestedTypes.filterIsInstance<RSMessage>(),
        nestedEnums = nestedTypes.filterIsInstance<RSEnum>(),
        nestedEnclosingTypes = nestedTypes.filterIsInstance<RSEnclosingType>(),
        location = location,
    )

    override val nestedTypes: List<RSType> by lazy {
        nestedMessages + nestedEnums + nestedEnclosingTypes
    }

    override fun copy(
        newNestedTypes: List<RSType>,
        newNestedExtends: List<RSExtend>,
        newOptions: RSOptions,
    ): RSType = RSEnum(
        name = name,
        constants = constants,
        documentation = documentation,
        options = newOptions,
        typeUrl = typeUrl,
        nestedExtends = newNestedExtends,
        nestedTypes = newNestedTypes,
        location = location,
    )

    public fun copy(
        name: String = this.name,
        constants: List<RSEnumConstant> = this.constants,
        documentation: String = this.documentation,
        options: RSOptions = this.options,
        typeUrl: RSDeclarationUrl = this.typeUrl,
        nestedExtends: List<RSExtend> = this.nestedExtends,
        nestedTypes: List<RSType> = this.nestedMessages + this.nestedEnums + this.nestedEnclosingTypes,
        location: RSElementLocation = this.location,
    ): RSEnum = RSEnum(
        name = name,
        constants = constants,
        documentation = documentation,
        options = options,
        typeUrl = typeUrl,
        nestedExtends = nestedExtends,
        nestedTypes = nestedTypes,
        location = location,
    )

}
