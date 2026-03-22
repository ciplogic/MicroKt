package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.ListView
import lt.microkot.common.get
import lt.microkot.common.slice
import lt.microkot.common.toListView
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.isSuccess

data class ParsedType(val type: DeclaredType, val consumed: Int)

fun parseType(view: ListView<SkeletonNode>): TResult<ParsedType> {
    if (view.size == 0) {
        return error("Trying to parse empty stream as type.")
    }

    val first = view.get(0)
    if (first.type != SkeletonType.Atom) {
        return error("Expected atom in first type position.")
    }

    val typeName = first.token!!.value
    val generics = mutableListOf<DeclaredType>()
    var isNullable = false
    var nextIndex = 1

    if (nextIndex < view.size) {
        val next = view.get(nextIndex)
        if (next.type == SkeletonType.Chevron) {
            generics.addAll(parseGenericList(next.children.toListView()))
            nextIndex = nextIndex + 1
        }
    }

    if (nextIndex < view.size) {
        val next = view.get(nextIndex)
        if (getAtomValue(next) == "?") {
            isNullable = true
            nextIndex = nextIndex + 1
        }
    }

    return success(ParsedType(DeclaredType(typeName, generics, isNullable), nextIndex))
}

fun parseGenericList(nodes: ListView<SkeletonNode>): MutableList<DeclaredType> {
    val result = mutableListOf<DeclaredType>()
    var i = 0
    while (i < nodes.size) {
        val node = nodes.get(i)
        val v = getAtomValue(node)
        if (v == "" || v == ",") {
            i++
            continue
        }
        val t = parseType(nodes.slice(i))
        if (t.isSuccess()) {
            val typeValue = t.value!!
            result.add(typeValue.type)
            i += typeValue.consumed
        } else {
            i++
        }
    }
    return result
}