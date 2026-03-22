package org.example._1simpleparser

import org.example._0lex.Scanner
import org.example._0lex.Token
import org.example._0lex.TokenType
import org.example._0lex.advance
import org.example._0lex.isAtEnd
import org.example._0lex.peek
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._2skeleton.tokensToStatement
import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.readFileAsText
import org.example.common.success
import org.example.common.toView

fun simpleParse(fileName: String): TResult<SkeletonNode> {
    val content = readFileAsText(fileName)
    val scanner = Scanner(content.toView())
    return simpleParseBodyNext(scanner, SkeletonType.CONSTRUCT)
}


fun simpleParseBodyNext(scanner: Scanner, skeletonType: SkeletonType): TResult<SkeletonNode> {
    val rootNode = SkeletonNode(skeletonType)
    while (!scanner.isAtEnd()) {
        val lineTokensRes = scanner.lineTokensErr()
        if (lineTokensRes.isError()) {
            return lineTokensRes.asError()
        }
        val lineTokens = lineTokensRes.value!!
        if (lineTokens.isEmpty()) {
            continue
        }

        val lastToken = lineTokens.last()

        if (lastToken.value == "}" && lineTokens.size == 1) {
            return success(rootNode)
        }
        val skeleton = tokensToStatement(lineTokens)
        if (lastToken.value == "{") {
            lineTokens.removeLast()
            val bodyResult = simpleParseBodyNext(scanner, SkeletonType.CURLY)
            if (bodyResult.isError()) {
                return bodyResult
            }
            skeleton.children.add(bodyResult.value!!)

        }
        rootNode.children.add(skeleton)
    }

    return success(rootNode)
}

fun Scanner.lineTokensErr(): TResult<MutableList<Token>> {
    val scanner = this
    val lineTokens = mutableListOf<Token>()
    while (!scanner.isAtEnd()) {
        val scanPeek = scanner.peek()
        if (scanPeek.isError()) {
            return scanPeek.asError()
        }
        val peek = scanPeek.value!!
        val current = scanner.advance()
        if (peek.type == TokenType.EOLN) {
            break
        }
        lineTokens.add(current)
    }
    return success(lineTokens)
}
