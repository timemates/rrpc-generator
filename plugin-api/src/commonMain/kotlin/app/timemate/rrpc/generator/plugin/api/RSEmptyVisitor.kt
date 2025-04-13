package app.timemate.rrpc.generator.plugin.api

import app.timemate.rrpc.proto.schema.RSEnumConstant
import app.timemate.rrpc.proto.schema.RSExtend
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.proto.schema.RSNode
import app.timemate.rrpc.proto.schema.RSOneOf
import app.timemate.rrpc.proto.schema.RSRpc
import app.timemate.rrpc.proto.schema.RSService
import app.timemate.rrpc.proto.schema.RSType

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