package org.timemates.rrpc.codegen

import org.timemates.rrpc.codegen.schema.RSEnumConstant
import org.timemates.rrpc.codegen.schema.RSExtend
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.RSFile
import org.timemates.rrpc.codegen.schema.RSRpc
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.codegen.schema.RSType

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
    public fun visitConstant(constant: RSEnumConstant, data: D): R
    public fun visitRpc(rpc: RSRpc, data: D): R
}