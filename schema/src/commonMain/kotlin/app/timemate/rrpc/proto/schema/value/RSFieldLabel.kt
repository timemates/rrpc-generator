package app.timemate.rrpc.proto.schema.value

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public enum class RSFieldLabel {
    @ProtoNumber(0)
    NONE,
    @ProtoNumber(1)
    ONE_OF,
    @ProtoNumber(2)
    REPEATED,
}