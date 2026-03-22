package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot.common.get
import lt.microkot.common.slice
import lt.microkot.common.toListView

fun parseParameters(paramsNode: SkeletonNode): TResult<List<Parameter>> {
    val result = mutableListOf<Parameter>()
    val children = paramsNode.children.toListView()
    var i = 0
    while (i < children.size) {
        val child = children.get(i)
        var v = getAtomValue(child)
        if (child.type == SkeletonType.Atom && v != "," && v != ":") {
            if (v == "val" || v == "var") {
                i = i + 1
                if (i < children.size) {
                    v = getAtomValueOfListView(children, i)
                }
            }
            val paramName = v
            i = i + 1
            var typeVal = DeclaredType("Any")
            if (i < children.size && getAtomValueOfListView(children, i) == ":") {
                i = i + 1
                val parsedResult = parseType(children.slice(i))
                if (parsedResult.isError()) {
                    return parsedResult.asError()
                }
                val parsed = parsedResult.value!!
                typeVal = parsed.type
                i = i + parsed.consumed
            }
            if (i < children.size && getAtomValueOfListView(children, i) == "=") {
                while (i < children.size && getAtomValueOfListView(children, i) != ",") {
                    i = i + 1
                }
            }
            if (paramName != "") {
                result.add(Parameter(paramName, typeVal))
            }
        } else {
            i = i + 1
        }
    }
    return success(result)
}
