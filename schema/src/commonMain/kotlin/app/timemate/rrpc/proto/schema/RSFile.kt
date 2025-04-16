package app.timemate.rrpc.proto.schema

import app.timemate.rrpc.proto.schema.value.LocationPath
import app.timemate.rrpc.proto.schema.value.RSPackageName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@ConsistentCopyVisibility
@Serializable
public data class RSFile private constructor(
    /**
     * Name of a source file.
     */
    @ProtoNumber(1)
    public val name: String,

    /**
     * Package name specified in the proto file. It should not be accessed to generate
     * platform-specific code, but through `platformPackageName(language)`. Or you may use:
     * - [javaPackage]
     * - [kotlinPackage]
     * - [csharpNamespace]
     * - [rubyPackage]
     * - [phpNamespace]
     * - [goPackage]
     */
    @ProtoNumber(2)
    public val packageName: RSPackageName = RSPackageName.EMPTY,

    /**
     * File-level options of the file.
     */
    @ProtoNumber(3)
    public val options: RSOptions = RSOptions.EMPTY,

    /**
     * The services that are defined in the `.proto` file.
     */
    @ProtoNumber(4)
    public val services: List<RSService> = emptyList(),

    /**
     * Declared extends in file.
     */
    @ProtoNumber(5)
    public val extends: List<RSExtend> = emptyList(),

    /**
     * Where is a file located
     */
    @ProtoNumber(6)
    public val location: RSElementLocation,

    @ProtoNumber(7)
    public val imports: List<LocationPath> = emptyList(),
    @ProtoNumber(8)
    private val messages: List<RSMessage> = emptyList(),
    @ProtoNumber(9)
    private val enums: List<RSEnum> = emptyList(),
    @ProtoNumber(10)
    private val enclosingTypes: List<RSEnclosingType> = emptyList(),
) : RSNode {
    public constructor(
        name: String,
        packageName: RSPackageName = RSPackageName.EMPTY,
        options: RSOptions = RSOptions.EMPTY,
        services: List<RSService> = emptyList(),
        extends: List<RSExtend> = emptyList(),
        location: RSElementLocation,
        imports: List<LocationPath> = emptyList(),
        types: List<RSType> = emptyList(),
    ) : this(
        name = name,
        packageName = packageName,
        options = options,
        services = services,
        extends = extends,
        location = location,
        imports = imports,
        messages = types.filterIsInstance<RSMessage>(),
        enums = types.filterIsInstance<RSEnum>(),
        enclosingTypes = types.filterIsInstance<RSEnclosingType>(),
    )

    public val types: List<RSType> by lazy {
        messages + enums + enclosingTypes
    }

    public companion object {
        public val JAVA_PACKAGE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "java_package")
        public val GO_PACKAGE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "go_package")
        public val PHP_NAMESPACE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "php_namespace")
        public val RUBY_PACKAGE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "ruby_package")
        public val CSHARP_NAMESPACE: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.FILE_OPTIONS, "csharp_namespace")
    }

    /**
     * Gets platform-specific package name based on the [language] that is provided by
     * accessing file-level options.
     */

    public fun platformPackageName(language: Language): RSPackageName? {
        return when (language) {
            Language.JAVA, Language.KOTLIN -> (options[JAVA_PACKAGE]?.value as? RSOption.Value.Raw)?.string
            Language.PHP -> (options[PHP_NAMESPACE]?.value as? RSOption.Value.Raw)?.string
            Language.C_SHARP -> (options[CSHARP_NAMESPACE]?.value as? RSOption.Value.Raw)?.string
            Language.PYTHON -> null
            Language.GO -> (options[GO_PACKAGE]?.value as? RSOption.Value.Raw)?.string
            Language.RUBY -> (options[RUBY_PACKAGE]?.value as? RSOption.Value.Raw)?.string
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

    public val allEnums: List<RSEnum> by lazy {
        enums + types.flatMap { it.allEnums }
    }

    public val allExtends: List<RSExtend> by lazy {
        extends + allTypes.flatMap { it.allExtends }
    }

    public fun copy(
        name: String = this.name,
        packageName: RSPackageName = this.packageName,
        options: RSOptions = this.options,
        services: List<RSService> = this.services,
        extends: List<RSExtend> = this.extends,
        location: RSElementLocation = this.location,
        imports: List<LocationPath> = this.imports,
        types: List<RSType> = this.types, // Combined list (messages + enums + enclosingTypes)
    ): RSFile {
        return RSFile(
            name = name,
            packageName = packageName,
            options = options,
            services = services,
            extends = extends,
            location = location,
            imports = imports,
            types = types // Here, we pass the combined list to the secondary constructor
        )
    }
}

public val RSFile.javaPackage: RSPackageName? get() = platformPackageName(Language.JAVA)
public val RSFile.kotlinPackage: RSPackageName? get() = platformPackageName(Language.KOTLIN)
public val RSFile.csharpNamespace: RSPackageName? get() = platformPackageName(Language.C_SHARP)
public val RSFile.phpNamespace: RSPackageName? get() = platformPackageName(Language.PHP)
public val RSFile.goPackage: RSPackageName? get() = platformPackageName(Language.GO)
public val RSFile.rubyPackage: RSPackageName? get() = platformPackageName(Language.RUBY)


public fun RSFile.findServiceOrNull(name: String): RSService? = services.firstOrNull { service -> service.name == name }
public fun RSFile.findService(name: String): RSService =
    findServiceOrNull(name) ?: error("Can't find service named '$name'.")
