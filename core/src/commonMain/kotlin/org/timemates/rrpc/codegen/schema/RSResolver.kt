package org.timemates.rrpc.codegen.schema

import org.timemates.rrpc.codegen.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.codegen.schema.value.LocationPath
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

public fun RSResolver(
    files: List<RSFile>,
): RSResolver = InMemoryRSResolver(files)

public fun RSResolver(
    vararg resolvers: RSResolver,
): RSResolver = InMemoryRSResolver(resolvers.flatMap { it.resolveAvailableFiles() })

/**
 * Interface for resolving various components (fields, types, files, services) in the RPC metadata model.
 * This provides a lookup mechanism for retrieving metadata elements such as types, services, fields,
 * extensions, and files based on unique identifiers like type URLs or package names.
 */
public interface RSResolver {
    /**
     * Resolves a field within a type by the given [typeMemberUrl].
     *
     * @param typeMemberUrl The URL that identifies the specific field within the type.
     * @return The corresponding [RSField] if found, or `null` if no matching field is found.
     */
    public fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField?

    /**
     * Resolves a type by the given [url].
     *
     * @param url The unique identifier for the type, typically used in protobuf definitions.
     * @return The corresponding [RSType] if found, or `null` if no matching type is found.
     */
    public fun resolveType(url: RSDeclarationUrl): RSType?

    /**
     * Resolves the file where a type is present.
     *
     * @param url The reference to the type.
     * @return The corresponding [RSFile] where the type is found, or `null` if no matching file is found.
     */
    public fun resolveFileOf(url: RSDeclarationUrl): RSFile?

    public fun resolveFileAt(path: LocationPath): RSFile?

    /**
     * Resolves file by specific path.
     */
    public fun resolveFileAt(location: RSElementLocation): RSFile?

    /**
     * Resolves a service by the given [url].
     *
     * @param url The unique identifier for the service, typically used in protobuf definitions.
     * @return The corresponding [RSService] if found, or `null` if no matching service is found.
     */
    public fun resolveService(url: RSDeclarationUrl): RSService?

    /**
     * Resolves all available files in the current [RSResolver].
     *
     * @return A sequence of all [RSFile]s available within this resolver.
     */
    public fun resolveAvailableFiles(): Sequence<RSFile>

    /**
     * Resolves all available services in the current [RSResolver].
     *
     * @return A sequence of all [RSService]s available within this resolver.
     */
    public fun resolveAllServices(): Sequence<RSService>

    /**
     * Resolves all available types in the current [RSResolver].
     *
     * @return A sequence of all [RSType]s available within this resolver.
     */
    public fun resolveAllTypes(): Sequence<RSType>
}

private class InMemoryRSResolver(
    private val files: List<RSFile>,
) : RSResolver {
    private val servicesIndex: Map<RSDeclarationUrl, RSService> by lazy {
        files.flatMap { it.services }.associateBy { it.typeUrl }
    }
    private val typesIndex: Map<RSDeclarationUrl, RSType> by lazy {
        files.flatMap { it.allTypes }.associateBy { it.typeUrl }
    }

    private val filesDeclarationUrlIndex: Map<RSDeclarationUrl, RSFile> by lazy {
        buildMap {
            files.forEach { file ->
                file.allTypes.forEach { type ->
                    put(type.typeUrl, file)
                }
            }
        }
    }

    private val filesLocationIndex: Map<String, RSFile> by lazy {
        buildMap {
            files.forEach { file ->
                put("${file.location.basePath}:${file.location.relativePath}", file)
            }
        }
    }

    @OptIn(NonPlatformSpecificAccess::class)
    private val fieldsIndex: Map<RSTypeMemberUrl, RSField> by lazy {
        buildMap {
            files.forEach { file ->
                file.extends.forEach { extend ->
                    extend.fields.forEach { field ->
                        val fieldName = file.packageName?.value?.plus(".${field.name}") ?: field.name
                        put(RSTypeMemberUrl(extend.typeUrl, fieldName), field)
                    }
                }
            }


            typesIndex.values
                .asSequence()
                .filterIsInstance<RSType.Message>()
                .flatMap { it.fields }
                .forEach { field ->
                    put(RSTypeMemberUrl(field.typeUrl, field.name), field)
                }
        }
    }

    override fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField? {
        return fieldsIndex[typeMemberUrl]
    }

    override fun resolveType(url: RSDeclarationUrl): RSType? {
        return typesIndex[url]
    }

    override fun resolveFileOf(url: RSDeclarationUrl): RSFile? {
        return filesDeclarationUrlIndex[url]
    }

    override fun resolveFileAt(path: LocationPath): RSFile? {
        return files.firstOrNull { it.location.relativePath == path }
    }

    override fun resolveFileAt(location: RSElementLocation): RSFile? {
        return filesLocationIndex["${location.basePath}:${location.relativePath}"]
    }

    override fun resolveService(url: RSDeclarationUrl): RSService? {
        return servicesIndex[url]
    }

    override fun resolveAvailableFiles(): Sequence<RSFile> {
        return files.asSequence()
    }

    override fun resolveAllServices(): Sequence<RSService> {
        return servicesIndex.values.asSequence()
    }

    override fun resolveAllTypes(): Sequence<RSType> {
        return typesIndex.values.asSequence()
    }
}
