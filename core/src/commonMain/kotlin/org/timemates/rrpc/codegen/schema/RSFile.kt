package org.timemates.rrpc.codegen.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.codegen.schema.value.LocationPath
import org.timemates.rrpc.codegen.schema.value.RSPackageName

@Serializable
public class RSFile(
    /**
     * Name of a source file.
     */
    @ProtoNumber(1)
    public val name: String,

    /**
     * Package name specified in the proto file. It should not be accessed to generate
     * platform-specific code, but through `platformPackageName(language)`.
     */
    @ProtoNumber(2)
    @NonPlatformSpecificAccess
    public val packageName: RSPackageName? = null,

    /**
     * File-level options of the file.
     */
    @ProtoNumber(3)
    public val options: RSOptions  = RSOptions.EMPTY,

    /**
     * The services that are defined in the `.proto` file.
     */
    @ProtoNumber(4)
    public val services: List<RSService> = emptyList(),

    /**
     * Types that are defined in `.proto` file.
     */
    @ProtoNumber(5)
    public val types: List<RSType> = emptyList(),

    /**
     * Declared extends in file.
     */
    @ProtoNumber(6)
    public val extends: List<RSExtend> = emptyList(),

    /**
     * Where is a file located
     */
    @ProtoNumber(7)
    public val location: RSElementLocation,

    @ProtoNumber(8)
    public val imports: List<LocationPath> = emptyList(),
) : RSNode {
    public companion object {
        public val JAVA_PACKAGE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "java_package")
    }

    /**
     * Gets platform-specific package name based on the [language] that is provided by
     * accessing file-level options.
     */
    @OptIn(NonPlatformSpecificAccess::class)
    public fun platformPackageName(language: Language): RSPackageName? {
        return when (language) {
            Language.JAVA, Language.KOTLIN -> (options[JAVA_PACKAGE]?.value as? RSOption.Value.Raw)?.string
            Language.PHP -> TODO()
            Language.C_SHARP -> TODO()
            Language.PYTHON -> TODO()
        }?.let(::RSPackageName) ?: packageName
    }

    public val allTypes: List<RSType> by lazy {
        val result = mutableListOf<RSType>()
        var typesToProcess = types

        while (typesToProcess.isNotEmpty()) {
            val nextTypes = typesToProcess.flatMap { it.nestedTypes }
            result.addAll(typesToProcess)  // Add the current level of types
            typesToProcess = nextTypes
        }

        result
    }
}

public fun RSFile.javaPackage(): RSPackageName? = platformPackageName(Language.JAVA)
public fun RSFile.kotlinPackage(): RSPackageName? = platformPackageName(Language.KOTLIN)
