package lt.microkot._2parser

import lt.microkot._3deepparser.DeepExpression as Expression

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.toListView
import lt.microkot.common.slice
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot._0lex.errorPosToText
import lt.microkot._3deepparser.getParserSource

fun parseFor(statementNode: SkeletonNode): TResult<ForStatement> {
    val children = statementNode.children

    var loopVar = ""
    var rangeExpr: Expression? = null
    var body: Block? = null

    var i = 1 // skip "for"
    while (i < children.size) {
        val child = children.get(i)
        if (child.type == SkeletonType.Parentheses && rangeExpr == null) {
            // Usually we have (val x in y) or (x in y)
            val parenChildren = child.children
            var j = 0
            while (j < parenChildren.size) {
                val v = getAtomValueOfList(parenChildren, j)
                if (v == "in") {
                    val exprNodes = parenChildren.subList(j + 1, parenChildren.size).toListView()
                    val exprRes = parseExpression(exprNodes)
                    if (exprRes.isError()) {
                        return exprRes.asError()
                    }
                    rangeExpr = exprRes.value
                    break
                }
                if (v != "" && v != "val" && v != "var") {
                    loopVar = v
                }
                j++
            }
        } else if (child.type == SkeletonType.CurlyBraces && body == null) {
            val res = parseBlock(child)
            if (res.isError()) {
                return res.asError()
            }
            body = res.value!!
        }
        i++
    }

    if (loopVar != "" && rangeExpr != null && body != null) {
        return success(ForStatement(loopVar, rangeExpr, body))
    }

    val location = errorPosToText(getParserSource(), statementNode.position)
    return error("Error $location: Could not parse for statement (missing loopVar, range, or body).")
}
