package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot.common.ListView
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.isError
import lt.microkot.common.asError

fun parseVarStatement(view: ListView<SkeletonNode>, isVar: Boolean): TResult<VarStatement> {
    val res = parsePropertyParts(view)
    if (res.isError()) {
        return res.asError()
    }
    val parts = res.value!!
    return success(VarStatement(parts.name, isVar, parts.type, parts.initializer))
}

