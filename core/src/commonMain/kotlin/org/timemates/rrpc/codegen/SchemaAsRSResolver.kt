package org.timemates.rrpc.codegen

import com.squareup.wire.schema.ProtoMember
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import org.timemates.rrpc.codegen.schema.RSElementLocation
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.RSFile
import org.timemates.rrpc.codegen.schema.RSResolver
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.codegen.schema.RSType
import org.timemates.rrpc.codegen.schema.RSTypeMemberUrl
import org.timemates.rrpc.codegen.schema.value.LocationPath
import org.timemates.rrpc.codegen.schema.value.RSDeclarationUrl

public fun Schema.asRSResolver(): RSResolver {
    return SchemaAsRSResolver(this)
}

private class SchemaAsRSResolver(
    private val schema: Schema,
) : RSResolver {
    override fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField? {
        return schema.getField(
            ProtoMember.get(
                ProtoType.get(typeMemberUrl.typeUrl.value.replace("type.googleapis.com/", "")),
                typeMemberUrl.memberName,
            )
        )?.asRSField()
    }

    override fun resolveType(url: RSDeclarationUrl): RSType? {
        return schema.getType(url.value)?.asRSType()
    }

    override fun resolveFileOf(url: RSDeclarationUrl): RSFile? {
        return schema.protoFile(ProtoType.get(url.value))?.asRSFile()
    }

    override fun resolveFileAt(path: LocationPath): RSFile? {
        return schema.protoFile(path.value)?.asRSFile()
    }

    override fun resolveFileAt(location: RSElementLocation): RSFile? {
        return schema.protoFile(location.relativePath.value)?.asRSFile()
    }

    override fun resolveService(url: RSDeclarationUrl): RSService? {
        return schema.getService(url.value)?.asRSService()
    }

    override fun resolveAvailableFiles(): Sequence<RSFile> {
        return schema.protoFiles.asSequence().map { it.asRSFile() }
    }

    override fun resolveAllServices(): Sequence<RSService> {
        return schema.protoFiles.asSequence().flatMap { it.services }.map { it.asRSService() }
    }

    override fun resolveAllTypes(): Sequence<RSType> {
        return schema.protoFiles.asSequence().flatMap { it.types }.map { it.asRSType() }
    }
}