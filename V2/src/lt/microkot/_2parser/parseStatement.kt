package lt.microkot._2parser
import lt.microkot._3deepparser.DeepExpression as Expression

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot.common.toListView
import lt.microkot.common.slice
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot._0lex.errorPosToText
import lt.microkot._3deepparser.getParserSource

fun parseStatement(statementNode: SkeletonNode): TResult<Statement> {
    if (statementNode.children.isEmpty()) {
        val location = errorPosToText(getParserSource(), statementNode.position)
        return error("Error $location: Empty statement node.")
    }
    
    val firstAtom = getAtomValueOfList(statementNode.children, 0)
    if (firstAtom == "val") {
        val res = parseVarStatement(statementNode.children.toListView().slice(1), false)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }
    if (firstAtom == "var") {
        val res = parseVarStatement(statementNode.children.toListView().slice(1), true)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }

    // We try to interpret it as a declaration first.
    val declRes = parseDeclaration(statementNode)
    if (declRes.isError()) {
        return declRes.asError()
    }
    val decl = declRes.value
    if (decl != null) {
        return success(DeclarationStatement(decl))
    }

    // Try a simple return check
    // If the very first atom is "return"
    if (firstAtom == "return") {
        if (statementNode.children.size > 1) {
            val exprNodes = statementNode.children.subList(1, statementNode.children.size)
            val exprRes = parseExpression(exprNodes.toListView())
            if (exprRes.isError()) return exprRes.asError()
            return success(ReturnStatement(exprRes.value))
        }
        return success(ReturnStatement(null))
    }

    if (firstAtom == "if") {
        val res = parseIf(statementNode)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }
    if (firstAtom == "while") {
        val res = parseWhile(statementNode)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }
    if (firstAtom == "else") {
        val res = parseElse(statementNode)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }
    if (firstAtom == "for") {
        val res = parseFor(statementNode)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value!!)
    }
    if (firstAtom == "break") {
        return success(BreakStatement())
    }
    if (firstAtom == "continue") {
        return success(ContinueStatement())
    }

    // Otherwise treat as an expression
    val exprRes = parseExpression(statementNode.children.toListView())
    if (exprRes.isError()) {
        return exprRes.asError()
    }
    return success(ExpressionStatement(exprRes.value!!))
}
