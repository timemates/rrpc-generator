package app.timemate.rrpc.generator.plugin.api

import app.timemate.rrpc.proto.schema.RSEnumConstant
import app.timemate.rrpc.proto.schema.RSExtend
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.proto.schema.RSOneOf
import app.timemate.rrpc.proto.schema.RSRpc
import app.timemate.rrpc.proto.schema.RSService
import app.timemate.rrpc.proto.schema.RSType

/**
 * Visitor interface for traversing RRpc schema objects.
 *
 * @param D The type of data passed during the traversal.
 * @param R The type of result returned from the visit methods.
 */
public interface RSVisitor<D, R> {
    public fun visitFile(file: RSFile, data: D): R
    public fun visitService(service: RSService, data: D): R
    public fun visitType(type: RSType, data: D): R
    public fun visitExtend(extend: RSExtend, data: D): R
    public fun visitField(field: RSField, data: D): R
    public fun visitOneOf(oneOf: RSOneOf, data: D): R
    public fun visitConstant(constant: RSEnumConstant, data: D): R
    public fun visitRpc(rpc: RSRpc, data: D): R
}