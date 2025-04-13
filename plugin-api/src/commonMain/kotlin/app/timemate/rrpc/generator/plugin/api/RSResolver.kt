package app.timemate.rrpc.generator.plugin.api

import app.timemate.rrpc.proto.schema.*
import app.timemate.rrpc.proto.schema.value.LocationPath
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl

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

    /**
     * Resolves all available [RSExtend]s in the current [RSResolver].
     */
    public fun resolveAllExtends(): Sequence<RSExtend>

    /**
     * Resolves all [RSExtend]s that are extending the given type.
     *
     * As in proto3, that is the only version we support, you can only extend
     * option-related messages – it can be used only to resolve all the options
     * for specific scope, where options can be used.
     */
    public fun resolveExtendsOfType(url: RSDeclarationUrl): Sequence<RSExtend>

    /**
     * Filters out the nodes that are not satisfies criteria returned by
     * [visitor].
     *
     * **Implementation note**: the filtering starts from top declarations to bottom,
     * meaning if top-level declaration like a message does not satisfy the criteria,
     * the further sub-declarations are not processed or included. Additionally,
     *  if the option field is removed, it removes any references in the schema. Works only
     * for options.
     */
    public fun filter(visitor: RSVisitor<Unit, Boolean>): RSResolver
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

    private val extendsIndex: Map<RSDeclarationUrl, MutableList<RSExtend>> by lazy {
        buildMap {
            files.forEach { file ->
                (file.extends + file.allTypes.flatMap { it.allExtends }).forEach { extend ->
                    if (containsKey(extend.typeUrl)) {
                        this[extend.typeUrl]!!.add(extend)
                    } else {
                        put(extend.typeUrl, mutableListOf(extend))
                    }
                }
            }
        }
    }


    private val fieldsIndex: Map<RSTypeMemberUrl, RSField> by lazy {
        buildMap {
            files.forEach { file ->
                file.allExtends.forEach { extend ->
                    extend.fields.forEach { field ->
                        put(RSTypeMemberUrl(extend.typeUrl, field.protoQualifiedName), field)
                    }
                }
            }


            typesIndex.values
                .asSequence()
                .filterIsInstance<RSMessage>()
                .flatMap {
                    it.fields
                }
                .forEach { field ->
                    put(RSTypeMemberUrl(field.typeUrl, field.name), field)
                }
        }
    }

    override fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField? {
        println(typeMemberUrl)
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

    override fun resolveAllExtends(): Sequence<RSExtend> {
        return extendsIndex.values.asSequence().flatten()
    }

    override fun resolveExtendsOfType(url: RSDeclarationUrl): Sequence<RSExtend> {
        return extendsIndex[url]?.asSequence() ?: emptySequence()
    }

    override fun filter(visitor: RSVisitor<Unit, Boolean>): RSResolver {
        val optionsToRemove: MutableSet<RSTypeMemberUrl> = mutableSetOf()

        val initResult = files.mapNotNull { file ->
            if (!visitor.visitFile(file, Unit)) {
                (file.extends + file.types.flatMap { it.allExtends }).forEach { extend ->
                    optionsToRemove += extend.fields.map { RSTypeMemberUrl(extend.typeUrl, it.name) }
                }
                return@mapNotNull null
            }

            file.copy(
                types = file.types.mapNotNull { type ->
                    filterType(optionsToRemove, type, visitor)
                },
                extends = file.extends.mapNotNull {
                    val result = filterExtend(it, visitor)
                    optionsToRemove.addAll(result.second)
                    result.first
                },
                services = file.services.mapNotNull { service ->
                    filterService(service, visitor)
                },
            )
        }

        if (optionsToRemove.isEmpty())
            return RSResolver(initResult)

        return RSResolver(
            initResult.map { file ->
                file.copy(
                    options = file.options.withFilteredFrom(optionsToRemove),
                    types = file.types.map { pruneOptionsInType(it, optionsToRemove) },
                    extends = file.extends.map { pruneOptionsInExtend(it, optionsToRemove) },
                    services = file.services.map { pruneOptionsInService(it, optionsToRemove) },
                )
            }
        )
    }

    private fun filterType(
        optionsToRemove: MutableSet<RSTypeMemberUrl>,
        type: RSType,
        visitor: RSVisitor<Unit, Boolean>,
    ): RSType? {
        if (!visitor.visitType(type, Unit)) {
            type.allExtends.forEach { extend ->
                optionsToRemove.addAll(extend.fields.map { RSTypeMemberUrl(extend.typeUrl, it.name) })
            }
            return null
        }

        return when (type) {
            is RSEnclosingType -> type
            is RSEnum -> {
                type.copy(
                    constants = type.constants.mapNotNull { constant ->
                        if (!visitor.visitConstant(constant, Unit)) {
                            null
                        } else {
                            constant
                        }
                    }
                )
            }

            is RSMessage -> {
                type.copy(
                    fields = type.fields.mapNotNull { field ->
                        if (!visitor.visitField(field, Unit)) {
                            null
                        } else {
                            field
                        }
                    },
                    oneOfs = type.oneOfs.mapNotNull { oneOf ->
                        if (!visitor.visitOneOf(oneOf, Unit)) {
                            null
                        } else {
                            oneOf
                        }
                    }
                )
            }
        }.let { type ->
            type.copy(
                newNestedTypes = type.nestedTypes.mapNotNull { filterType(optionsToRemove, it, visitor) },
                newNestedExtends = type.nestedExtends.mapNotNull {
                    val result = filterExtend(it, visitor)
                    optionsToRemove.addAll(result.second)
                    result.first
                }
            )
        }
    }

    private fun filterExtend(
        extend: RSExtend,
        visitor: RSVisitor<Unit, Boolean>,
    ): Pair<RSExtend?, List<RSTypeMemberUrl>> {
        val extend = if (!visitor.visitExtend(extend, Unit))
            return Pair(null, extend.fields.map { RSTypeMemberUrl(extend.typeUrl, it.name) })
        else extend

        val resulting = extend.fields.associateWith { field ->
            visitor.visitField(field, Unit)
        }

        return extend.copy(
            fields = resulting.mapNotNull { (field, bool) ->
                if (bool) field else null
            }
        ) to extend.fields.map {
            RSTypeMemberUrl(extend.typeUrl, it.name)
        }
    }

    private fun filterService(service: RSService, visitor: RSVisitor<Unit, Boolean>): RSService? {
        return (if (!visitor.visitService(service, Unit)) null else service)
            .let { service ->
                service?.copy(
                    rpcs = service.rpcs.mapNotNull { rpc ->
                        if (!visitor.visitRpc(rpc, Unit)) null else rpc
                    },
                )
            }
    }

    private fun pruneOptionsInService(service: RSService, optionsToRemove: Set<RSTypeMemberUrl>): RSService {
        return service.copy(
            options = service.options.withFilteredFrom(optionsToRemove),
            rpcs = service.rpcs.map { rpc -> rpc.copy(options = rpc.options.withFilteredFrom(optionsToRemove)) },
        )
    }

    private fun pruneOptionsInType(type: RSType, optionsToRemove: Set<RSTypeMemberUrl>): RSType {
        val options = type.options.withFilteredFrom(optionsToRemove)
        return when (type) {
            is RSEnclosingType -> {
                type.copy(
                    nestedTypes = type.nestedTypes.map { pruneOptionsInType(it, optionsToRemove) },
                    nestedExtends = type.nestedExtends.map { pruneOptionsInExtend(it, optionsToRemove) },
                    options = options,
                )
            }

            is RSEnum -> {
                type.copy(
                    constants = type.constants.map { it.copy(options = it.options.withFilteredFrom(optionsToRemove)) },
                    nestedTypes = type.nestedTypes.map { pruneOptionsInType(it, optionsToRemove) },
                    nestedExtends = type.nestedExtends.map { pruneOptionsInExtend(it, optionsToRemove) },
                    options = options,
                )
            }

            is RSMessage -> {
                type.copy(
                    fields = type.fields.map { it.copy(options = it.options.withFilteredFrom(optionsToRemove)) },
                    oneOfs = type.oneOfs.map { oneOf ->
                        oneOf.copy(
                            options = oneOf.options.withFilteredFrom(optionsToRemove),
                            fields = oneOf.fields.map { it.copy(options = it.options.withFilteredFrom(optionsToRemove)) },
                        )
                    },
                    options = options,
                    nestedTypes = type.nestedTypes.map { pruneOptionsInType(it, optionsToRemove) },
                    nestedExtends = type.nestedExtends.map { pruneOptionsInExtend(it, optionsToRemove) }
                )
            }
        }
    }

    private fun pruneOptionsInExtend(extend: RSExtend, optionsToRemove: Set<RSTypeMemberUrl>): RSExtend {
        return extend.copy(
            fields = extend.fields.map { it.copy(options = it.options.withFilteredFrom(optionsToRemove)) },
        )
    }
}
