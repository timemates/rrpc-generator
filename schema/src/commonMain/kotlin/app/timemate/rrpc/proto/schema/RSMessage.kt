package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@ConsistentCopyVisibility
@Serializable
public data class RSMessage private constructor(
    @ProtoNumber(1)
    override val name: String = "",
    @ProtoNumber(2)
    override val documentation: String = "",
    @ProtoNumber(3)
    val fields: List<RSField> = emptyList(),
    @ProtoNumber(4)
    val oneOfs: List<RSOneOf> = emptyList(),
    @ProtoNumber(5)
    override val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(6)
    override val typeUrl: RSDeclarationUrl = RSDeclarationUrl.UNKNOWN,
    @ProtoNumber(7)
    override val nestedExtends: List<RSExtend> = emptyList(),
    @ProtoNumber(8)
    override val location: RSElementLocation = RSElementLocation.UNKNOWN,
    @ProtoNumber(9)
    private val nestedMessages: List<RSMessage> = emptyList(),
    @ProtoNumber(10)
    private val nestedEnums: List<RSEnum> = emptyList(),
    @ProtoNumber(11)
    private val nestedEnclosingTypes: List<RSEnclosingType> = emptyList(),
) : RSType {
    public constructor(
        name: String,
        documentation: String,
        fields: List<RSField> = emptyList(),
        oneOfs: List<RSOneOf> = emptyList(),
        options: RSOptions,
        typeUrl: RSDeclarationUrl,
        nestedExtends: List<RSExtend> = emptyList(),
        nestedTypes: List<RSType>,
        location: RSElementLocation,
    ) : this(
        name = name,
        documentation = documentation,
        fields = fields,
        oneOfs = oneOfs,
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

    val allFields: List<RSField> get() = fields + oneOfs.flatMap { it.fields }

    /**
     * Gets [RSField] in current [RSType.Message] by given [tag].
     * If a field with a tag persists in the oneof field – oneof field is returned,
     *  where the field occurs.
     */
    public fun field(tag: Int): RSField? {
        return fields.firstOrNull { field ->
            field.tag == tag
        }
    }

    /**
     * Gets [RSField] in current [RSType.Message] by given [name].
     * If a field with a tag persists in the oneof field – oneof field is returned,
     *  where the field occurs.
     */

    public fun field(name: String): RSField? {
        return fields.firstOrNull { field ->
            field.name == name
        }
    }

    public override fun equals(other: Any?): Boolean {
        return other is RSMessage && other.typeUrl == typeUrl
    }

    override fun hashCode(): Int {
        return typeUrl.hashCode()
    }

    override fun copy(
        newNestedTypes: List<RSType>,
        newNestedExtends: List<RSExtend>,
        newOptions: RSOptions,
    ): RSType {
        return copy(nestedExtends = newNestedExtends, nestedTypes = newNestedTypes, options = newOptions)
    }

    public fun copy(
        name: String = this.name,
        documentation: String = this.documentation,
        fields: List<RSField> = this.fields,
        oneOfs: List<RSOneOf> = this.oneOfs,
        options: RSOptions = this.options,
        typeUrl: RSDeclarationUrl = this.typeUrl,
        nestedExtends: List<RSExtend> = this.nestedExtends,
        nestedTypes: List<RSType> = this.nestedTypes,
        location: RSElementLocation = this.location,
    ): RSMessage = RSMessage(
        name = name,
        documentation = documentation,
        fields = fields,
        oneOfs = oneOfs,
        options = options,
        typeUrl = typeUrl,
        nestedExtends = nestedExtends,
        nestedTypes = nestedTypes,
        location = location,
    )
}