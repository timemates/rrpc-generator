package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSRpc(
    /**
     * The name of RPC.
     *
     * Marked with [NonPlatformSpecificAccess] because code-generation should
     * adapt the name to the language's naming convention. For example, for Java
     * and Kotlin, it should start with a lowercase letter instead of uppercase as in the
     * ProtoBuf naming convention. If we generate some kind of metadata to the service, it's
     * absolutely legal and required to keep name as in original `.proto` definition, otherwise
     * it may break cross-language support.
     *
     * @see languageSpecificName
     */
    @ProtoNumber(1)
    public val name: String = "",

    /**
     * Denotes the input of RPC that comes from client to server. The type
     * might be a stream, make sure you made a check.
     */
    @ProtoNumber(2)
    public val requestType: StreamableRSTypeUrl = StreamableRSTypeUrl(),

    /**
     * Denotes the out of RPC that comes from server to client. The type
     * might be a stream, make sure you made a check.
     */
    @ProtoNumber(3)
    public val responseType: StreamableRSTypeUrl = StreamableRSTypeUrl(),

    /**
     * The options that are specified for given RPC.
     */
    @ProtoNumber(4)
    public val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(5)
    override val documentation: String = "",
    @ProtoNumber(6)
    public val location: RSElementLocation = RSElementLocation.UNKNOWN,
) : RSNode, Documentable {
    public fun languageSpecificName(language: Language): String {

        return when (language) {
            Language.JAVA, Language.KOTLIN, Language.PYTHON, Language.GO ->
                name.replaceFirstChar { it.lowercase() } // camelCase

            Language.PHP, Language.RUBY ->
                name.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase() // snake_case

            Language.C_SHARP ->
                name.replaceFirstChar { it.uppercase() }
        }
    }
}

public val RSRpc.javaName get() = languageSpecificName(Language.JAVA)
public val RSRpc.kotlinName get() = languageSpecificName(Language.KOTLIN)
public val RSRpc.pythonName get() = languageSpecificName(Language.PYTHON)
public val RSRpc.goName get() = languageSpecificName(Language.GO)
public val RSRpc.phpName get() = languageSpecificName(Language.PHP)
public val RSRpc.rubyName get() = languageSpecificName(Language.RUBY)
public val RSRpc.csharpName get() = languageSpecificName(Language.C_SHARP)

public val RSRpc.isRequestResponse: Boolean get() = !requestType.isStreaming && !responseType.isStreaming
public val RSRpc.isRequestStream: Boolean get() = !requestType.isStreaming && responseType.isStreaming
public val RSRpc.isRequestChannel: Boolean get() = requestType.isStreaming && responseType.isStreaming
public val RSRpc.isFireAndForget: Boolean
    get() = requestType.type != RSDeclarationUrl.Companion.ACK && responseType.type == RSDeclarationUrl.Companion.ACK
public val RSRpc.isMetadataPush: Boolean
    get() = requestType.type == RSDeclarationUrl.Companion.ACK && responseType.type == RSDeclarationUrl.Companion.ACK