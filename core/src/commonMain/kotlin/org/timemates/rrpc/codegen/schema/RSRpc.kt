package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

@Serializable
public class RSRpc(
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
    @NonPlatformSpecificAccess
    public val name: String,

    /**
     * Denotes the input of RPC that comes from client to server. The type
     * might be a stream, make sure you made a check.
     */
    @ProtoNumber(2)
    public val requestType: StreamableRSTypeUrl,

    /**
     * Denotes the out of RPC that comes from server to client. The type
     * might be a stream, make sure you made a check.
     */
    @ProtoNumber(3)
    public val responseType: StreamableRSTypeUrl,

    /**
     * The options that are specified for given RPC.
     */
    @ProtoNumber(4)
    public val options: RSOptions = RSOptions.EMPTY,
    @ProtoNumber(5)
    override val documentation: String? = null,
) : RSNode, Documentable {
    public fun languageSpecificName(language: Language): String {
        @OptIn(NonPlatformSpecificAccess::class)
        return when (language) {
            Language.JAVA, Language.KOTLIN, Language.PHP, Language.PYTHON ->
                name.replaceFirstChar { it.lowercase() }
            else -> TODO()
        }
    }
}

public fun RSRpc.javaName(): String = languageSpecificName(Language.JAVA)
public fun RSRpc.kotlinName(): String = languageSpecificName(Language.KOTLIN)

public val RSRpc.isRequestResponse: Boolean get() = !requestType.isStreaming && !responseType.isStreaming
public val RSRpc.isRequestStream: Boolean get() = !requestType.isStreaming && responseType.isStreaming
public val RSRpc.isRequestChannel: Boolean get() = requestType.isStreaming && responseType.isStreaming
public val RSRpc.isFireAndForget: Boolean
    get() = requestType.type != RSDeclarationUrl.ACK && responseType.type == RSDeclarationUrl.ACK
public val RSRpc.isMetadataPush: Boolean
    get() = requestType.type == RSDeclarationUrl.ACK && responseType.type == RSDeclarationUrl.ACK