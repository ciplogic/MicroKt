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
import lt.microkot.common.toMutableList
import lt.microkot._2parser.entities.*


data class FunctionHeader(
    val name: DeclaredType,
    val receiver: DeclaredType?,
    val parameters: List<Parameter>,
    val returnType: DeclaredType,
    val bodyNode: SkeletonNode?
)

fun parseFunctionHeader(view: ListView<SkeletonNode>): TResult<FunctionHeader> {
    var paramsNode: SkeletonNode? = null
    var bodyNode: SkeletonNode? = null
    var paramsIdx = -1

    // 1. Locate components (Parameters part, Body part)
    var i = 0
    while (i < view.size) {
        val child = view.get(i)
        if (child.type == SkeletonType.Parentheses && paramsNode == null) {
            paramsNode = child
            paramsIdx = i
        } else if (child.type == SkeletonType.CurlyBraces && bodyNode == null) {
            bodyNode = child
            break
        }
        i++
    }

    if (paramsIdx == -1) {
        return error("Could not parse function: missing parameters (parentheses).")
    }

    val headerTokens = view.slice(0, paramsIdx)
    var receiverType: DeclaredType? = null
    var nameGenericsTokens: ListView<SkeletonNode> = headerTokens

    // 2. Identify Receiver Type (Dot separator search)
    var dotIdx = -1
    for (j in 0 until headerTokens.size) {
        if (getAtomValueOfListView(headerTokens, j) == ".") {
            dotIdx = j
            break
        }
    }

    if (dotIdx != -1) {
        val receiverTokens = headerTokens.slice(0, dotIdx)
        if (receiverTokens.size > 0) {
            val res = parseType(receiverTokens)
            if (res.isError()) {
                return res.asError()
            }
            receiverType = res.value!!.type
        }
        nameGenericsTokens = headerTokens.slice(dotIdx + 1)
    }

    // 3. Unify Name and Generics (moving <T> to after "foo")
    val unifiedNameList = mutableListOf<SkeletonNode>()
    var nameAtom: SkeletonNode? = null
    var chevronNode: SkeletonNode? = null

    for (j in 0 until nameGenericsTokens.size) {
        val node = nameGenericsTokens.get(j)
        val value = getAtomValue(node)
        if (node.type == SkeletonType.Chevron) {
            chevronNode = node
        } else if (node.type == SkeletonType.Atom && value != "") {
             nameAtom = node
        }
    }

    if (nameAtom == null) {
        return error("Could not parse function: missing name.")
    }
    unifiedNameList.add(nameAtom)
    if (chevronNode != null) {
        unifiedNameList.add(chevronNode)
    }

    // 4. Parse Name + Generics as a single type to reuse generic extraction
    val nameRes = parseType(unifiedNameList.toListView())
    if (nameRes.isError()) {
        return nameRes.asError()
    }
    val nameBlock = nameRes.value!!.type

    // 5. Parse Parameters
    val parametersRes = parseParameters(paramsNode!!)
    if (parametersRes.isError()) {
        return parametersRes.asError()
    }

    // 6. Handle Return Type (between params and body)
    var returnType = DeclaredType("Unit")
    var startOfReturnIdx = paramsIdx + 1
    var endOfReturnIdx = if (bodyNode != null) {
        // Find body index in original view
        var bodyIdx = -1
        for (j in paramsIdx + 1 until view.size) {
            if (view.get(j) == bodyNode) {
                bodyIdx = j
                break
            }
        }
        bodyIdx
    } else {
        view.size
    }

    if (startOfReturnIdx < endOfReturnIdx) {
        val returnView = view.slice(startOfReturnIdx, endOfReturnIdx)
        for (j in 0 until returnView.size) {
            if (getAtomValueOfListView(returnView, j) == ":") {
                val res = parseType(returnView.slice(j + 1))
                if (res.isSuccess()) {
                    returnType = res.value!!.type
                }
                break
            }
        }
    }

    return success(FunctionHeader(
        nameBlock,
        receiverType,
        parametersRes.value!!,
        returnType,
        bodyNode
    ))
}
