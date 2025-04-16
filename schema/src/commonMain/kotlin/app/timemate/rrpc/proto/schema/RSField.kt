package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSFieldLabel
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.proto.schema.value.RSPackageName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSField(
    @ProtoNumber(1)
    public val tag: Int = 0,
    @ProtoNumber(2)
    public val name: String = "",
    @ProtoNumber(3)
    public val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(4)
    override val documentation: String = "",
    @ProtoNumber(5)
    public val typeUrl: RSDeclarationUrl = RSDeclarationUrl.EMPTY,
    @ProtoNumber(6)
    public val label: RSFieldLabel = RSFieldLabel.NONE,
    @ProtoNumber(7)
    public val location: RSElementLocation = RSElementLocation.UNKNOWN,
    @ProtoNumber(8)
    public val isExtension: Boolean = false,
    @ProtoNumber(9)
    private val namespacesList: List<String> = emptyList(),
) : Documentable, RSNode {
    public val isInOneOf: Boolean get() = label == RSFieldLabel.ONE_OF
    public val isRepeated: Boolean get() = label == RSFieldLabel.REPEATED

    /**
     * Applies only to `extends` within a message.
     */
    public val namespaces: Namespaces? by lazy {
        if (!isExtension) return@lazy null

        val packageName = namespacesList.firstOrNull()
            ?.takeIf { it.isEmpty() }
            ?.let { RSPackageName(it) }
            ?: RSPackageName.Companion.EMPTY

        Namespaces(packageName, namespacesList.drop(1))
    }

    public val protoQualifiedName: String get() {
        return buildString {
            if (namespaces?.packageName?.value?.isNotBlank() == true) {
                append(namespaces!!.packageName.value)

                if (namespaces?.simpleNames?.isNotEmpty() == true)
                    append(".")
            }
            if (namespaces?.simpleNames?.isNotEmpty() == true) {
                append(namespaces!!.simpleNames.joinToString("."))
                append(".")
            }
            append(name)
        }
    }

    public data class Namespaces(public val packageName: RSPackageName, public val simpleNames: List<String>)
}


public val RSField.javaName: String
    get() = name.split("_")
        .mapIndexed { index, word ->
            if (index == 0) word.lowercase()
            else word.replaceFirstChar { it.uppercase() }
        }
        .joinToString("")


public val RSField.kotlinName: String get() = javaName