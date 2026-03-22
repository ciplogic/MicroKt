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
import lt.microkot.common.asError
import lt.microkot.common.isSuccess

fun parseFunction(view: ListView<SkeletonNode>, modifiers: ModifierList): TResult<FunctionDeclaration> {
    var name = ""
    var typeParameters = mutableListOf<DeclaredType>()
    var receiver: DeclaredType? = null
    var paramsNode: SkeletonNode? = null
    var bodyNode: SkeletonNode? = null
    var returnTypeNodeStart = -1
    var i = 0

    while (i < view.size && getAtomValueOfListView(view, i) == "") {
        i++
    }

    if (i < view.size && view.get(i).type == SkeletonType.Chevron) {
        val chevron = view.get(i)
        typeParameters.addAll(parseGenericList(chevron.children.toListView()))
        i++
    }

    if (i < view.size) {
        val typeOrNameRes = parseType(view.slice(i))
        if (typeOrNameRes.isSuccess()) {
            val parsedTypeOrName = typeOrNameRes.value!!
            i += parsedTypeOrName.consumed
            
            while (i < view.size && getAtomValueOfListView(view, i) == "") {
                i++
            }
            
            if (i < view.size && getAtomValueOfListView(view, i) == ".") {
                receiver = parsedTypeOrName.type
                i++
                while (i < view.size && getAtomValueOfListView(view, i) == "") {
                    i++
                }
                if (i < view.size) {
                    name = getAtomValueOfListView(view, i)
                    i++
                }
            } else {
                name = parsedTypeOrName.type.name
            }
        }
    }

    while (i < view.size) {
        val child = view.get(i)
        val type = child.type

        if (type == SkeletonType.Parentheses) {
            if (paramsNode == null) {
                paramsNode = child
                returnTypeNodeStart = i + 1
            }
        } else if (type == SkeletonType.CurlyBraces) {
            bodyNode = child
            break
        }
        i++
    }

    val parameters = if (paramsNode != null) {
        val res = parseParameters(paramsNode)
        if (res.isError()) {
            return res.asError()
        }
        res.value!!
    } else {
        emptyList()
    }
    var returnType = DeclaredType("Unit")
    
    if (returnTypeNodeStart != -1) {
        var j = returnTypeNodeStart
        var end = view.size
        if (bodyNode != null) {
            end = i
        }
        while (j < end) {
            val child = view.get(j)
            if (getAtomValue(child) == ":") {
                val res = parseType(view.slice(j + 1))
                if (res.isSuccess()) {
                    returnType = res.value!!.type
                }
                break
            }
            j = j + 1
        }
    }

    // Parsing the 'bodyNode' into a Block
    val body = if (bodyNode != null) {
        val res = parseBlock(bodyNode)
        if (res.isError()) {
            return res.asError()
        }
        res.value!!
    } else {
        null
    }

    return success(FunctionDeclaration(name, modifiers, typeParameters, receiver, parameters, returnType, body))
}
