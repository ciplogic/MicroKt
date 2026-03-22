package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.ListView
import lt.microkot.common.get
import lt.microkot.common.slice
import lt.microkot.common.toListView
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot.common.isSuccess

fun parseTypeAlias(view: ListView<SkeletonNode>, modifiers: ModifierList): TResult<TypeAliasDeclaration> {
    var aliasType = DeclaredType("Unknown")
    var paramsNode: SkeletonNode? = null
    var returnTypeNodeStart = -1

    var i = 0
    while (i < view.size && getAtomValueOfListView(view, i) == "") {
        i++
    }

    if (i < view.size) {
        val res = parseType(view.slice(i))
        if (res.isSuccess()) {
            val parsed = res.value!!
            aliasType = parsed.type
            i += parsed.consumed
        } else {
            i++ // fallback
        }
    }

    while (i < view.size && getAtomValueOfListView(view, i) != "=") {
        i++
    }
    i++ // Skip '='

    while (i < view.size) {
        val child = view.get(i)
        val type = child.type
        val value = getAtomValue(child)
        if (type == SkeletonType.Parentheses) {
            paramsNode = child
        } else if (value == "-") {
            if (i + 1 < view.size && getAtomValueOfListView(view, i + 1) == ">") {
                returnTypeNodeStart = i + 2
                break
            }
        } else if (value == "->") {
            returnTypeNodeStart = i + 1
            break
        }
        i++
    }

    val parameterTypes = mutableListOf<DeclaredType>()
    if (paramsNode != null) {
        val paramView = paramsNode.children.toListView()
        var j = 0
        while (j < paramView.size) {
            val v = getAtomValueOfListView(paramView, j)
            if (v == "" || v == ",") {
                j++
                continue
            }
            val parsedTypeRes = parseType(paramView.slice(j))
            if (parsedTypeRes.isSuccess()) {
                val parsedType = parsedTypeRes.value!!
                parameterTypes.add(parsedType.type)
                j += parsedType.consumed
            } else {
                j++
            }
        }
    }

    var returnType = DeclaredType("Unit")
    if (returnTypeNodeStart != -1) {
        var startIdx = returnTypeNodeStart
        while (startIdx < view.size && getAtomValueOfListView(view, startIdx) == "") {
            startIdx++
        }
        val res = parseType(view.slice(startIdx))
        if (res.isSuccess()) {
            returnType = res.value!!.type
        }
    }

    return success(TypeAliasDeclaration(aliasType, modifiers, parameterTypes, returnType))
}
