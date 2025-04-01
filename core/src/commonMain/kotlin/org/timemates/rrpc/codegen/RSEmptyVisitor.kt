package org.timemates.rrpc.codegen

import org.timemates.rrpc.codegen.schema.RSEnumConstant
import org.timemates.rrpc.codegen.schema.RSExtend
import org.timemates.rrpc.codegen.schema.RSField
import org.timemates.rrpc.codegen.schema.RSFile
import org.timemates.rrpc.codegen.schema.RSNode
import org.timemates.rrpc.codegen.schema.RSOneOf
import org.timemates.rrpc.codegen.schema.RSRpc
import org.timemates.rrpc.codegen.schema.RSService
import org.timemates.rrpc.codegen.schema.RSType

public abstract class RSEmptyVisitor<D, R> : RSVisitor<D, R> {
    public abstract fun defaultHandler(node: RSNode, data: D): R

    override fun visitOneOf(oneOf: RSOneOf, data: D): R {
        return defaultHandler(oneOf, data)
    }

    override fun visitFile(file: RSFile, data: D): R {
        return defaultHandler(file, data)
    }

    override fun visitService(service: RSService, data: D): R {
        return defaultHandler(service, data)
    }

    override fun visitType(type: RSType, data: D): R {
        return defaultHandler(type, data)
    }

    override fun visitExtend(extend: RSExtend, data: D): R {
        return defaultHandler(extend, data)
    }

    override fun visitRpc(rpc: RSRpc, data: D): R {
        return defaultHandler(rpc, data)
    }

    override fun visitField(field: RSField, data: D): R {
        return defaultHandler(field, data)
    }

    override fun visitConstant(constant: RSEnumConstant, data: D): R {
        return defaultHandler(constant, data)
    }
}