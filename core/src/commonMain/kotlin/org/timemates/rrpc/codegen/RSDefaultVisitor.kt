package org.timemates.rrpc.codegen

import org.timemates.rrpc.codegen.schema.RSFile
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.codegen.schema.RSType

public abstract class RSDefaultVisitor<D, R> : RSEmptyVisitor<D, R>() {
    override fun visitFile(file: RSFile, data: D): R {
        file.services.forEach { service ->
            visitService(service, data)
        }
        file.types.forEach { type ->
            visitType(type, data)
        }
        file.extends.forEach { extend ->
            visitExtend(extend, data)
        }
        return super.visitFile(file, data)
    }

    override fun visitService(service: RSService, data: D): R {
        service.rpcs.forEach { rpc -> visitRpc(rpc, data) }
        return super.visitService(service, data)
    }

    override fun visitType(type: RSType, data: D): R {
        when (type) {
            is RSType.Enum -> type.constants.forEach { constant -> visitConstant(constant, data) }
            is RSType.Message -> type.fields.forEach { field -> visitField(field, data) }
            is RSType.Enclosing -> {}
        }

        type.nestedTypes.forEach { type -> visitType(type, data) }
        type.nestedExtends.forEach { extend -> visitExtend(extend, data) }

        return super.visitType(type, data)
    }
}