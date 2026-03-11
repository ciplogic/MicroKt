package org.example._2skeleton

import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success
import org.example._0lex.Scanner
import org.example._0lex.TokenType
import org.example._0lex.advance
import org.example._0lex.isAtEnd
import org.example._0lex.peek
import org.example._0lex.peekText


fun foldExpressionGroup(scanner: Scanner): TResult<SkeletonNode> {
    val text = scanner.peekText()

    if (text == "(") {
        return foldGroup(scanner, "(", ")", SkeletonType.PAREN)
    }

    if (text == "[") {
        return foldGroup(scanner, "[", "]", SkeletonType.BRACKET)
    }

    if (text == "<") {
        return tryFoldChevron(scanner)
    }

    // Fallback: It's a plain atom (Identifier, Number, Operator, etc.)
    val res = scanner.peek()
    if (res.isError()) return res.asError()

    val token = res.value!!
    scanner.advance()
    return success(SkeletonNode(SkeletonType.ATOM, token))
}

private fun tryFoldChevron(scanner: Scanner): TResult<SkeletonNode> {
    val startPos = scanner.pos
    val openToken = scanner.advance() // '<'

    val children = mutableListOf<SkeletonNode>()
    var balance = 1

    while (!scanner.isAtEnd() && balance > 0) {
        val p = scanner.peek().value!!
        val txt = p.value

        if (txt == ">") {
            balance--
            scanner.advance()
            if (balance == 0) {
                val node = SkeletonNode(SkeletonType.CHEVRON)
                node.children.addAll(children)
                return success(node)
            }
        } else if (txt == "<") {
            balance++
            scanner.advance()
        } else if (p.type == TokenType.OPERATOR && !":,.".contains(txt)) {
            // Found a math/logical operator? This is NOT a generic type.
            break
        } else {
            val child = foldExpressionGroup(scanner)
            if (child.isError()) break
            children.add(child.value!!)
        }
    }

    // FAIL: Reset scanner to just after the '<' and treat as ATOM
    scanner.pos = startPos + 1
    return success(SkeletonNode(SkeletonType.ATOM, openToken))
}
