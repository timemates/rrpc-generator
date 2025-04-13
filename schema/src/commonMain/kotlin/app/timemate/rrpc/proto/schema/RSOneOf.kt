package app.timemate.rrpc.proto.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@SerialName("ONE_OF")
public data class RSOneOf(
    @ProtoNumber(1)
    public val name: String = "",
    @ProtoNumber(2)
    public val fields: List<RSField> = emptyList(),
    @ProtoNumber(3)
    val documentation: String = "",
    @ProtoNumber(4)
    val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(5)
    val location: RSElementLocation = RSElementLocation.UNKNOWN,
) : RSNode

public val RSOneOf.javaName: String
    get() = name.split("_")
        .mapIndexed { index, word ->
            if (index == 0) word.lowercase()
            else word.replaceFirstChar { it.uppercase() }
        }
        .joinToString("")


public val RSOneOf.kotlinName: String get() = javaName