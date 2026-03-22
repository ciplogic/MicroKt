package lt.microkot._2parser
import lt.microkot._3deepparser.DeepExpression as Expression

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot.common.toListView
import lt.microkot._0lex.errorPosToText
import lt.microkot._3deepparser.getParserSource


fun parseIf(statementNode: SkeletonNode): TResult<IfStatement> {
    val children = statementNode.children
    var i = 1 // skip "if"
    
    var conditionExpr: Expression? = null
    var thenBlock: Block? = null
    
    while (i < children.size) {
        val child = children.get(i)
        if (child.type == SkeletonType.Parentheses && conditionExpr == null) {
            val exprRes = parseExpression(child.children.toListView())
            if (exprRes.isError()) return exprRes.asError()
            conditionExpr = exprRes.value
        } else if (child.type == SkeletonType.CurlyBraces && thenBlock == null) {
            val res = parseBlock(child)
            if (res.isError()) {
                return res.asError()
            }
            thenBlock = res.value!!
        }
        i++
    }
    
    if (conditionExpr != null && thenBlock != null) {
        return success(IfStatement(conditionExpr, thenBlock))
    }
    
    val location = errorPosToText(getParserSource(), statementNode.position)
    return error("Error $location: Could not parse if statement (missing condition or then-block).")
}
