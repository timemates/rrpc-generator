package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl

public sealed interface RSType : RSNode, Documentable {
    public val name: String
    public val typeUrl: RSDeclarationUrl
    public val nestedTypes: List<RSType>
    public val nestedExtends: List<RSExtend>
    override val documentation: String?
    public val options: RSOptions
    public val location: RSElementLocation

    public fun copy(
        newNestedTypes: List<RSType> = nestedTypes,
        newNestedExtends: List<RSExtend> = nestedExtends,
        newOptions: RSOptions = options,
    ): RSType
}

public val RSType.allExtends: List<RSExtend>
    get() = nestedExtends + nestedTypes.flatMap { it.nestedExtends }