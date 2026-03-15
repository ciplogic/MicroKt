package org.example._2skeleton

import org.example._0lex.*
import org.example.common.*


fun foldFunction(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    var foldFunction = foldFunctionHeader(scanner, modifiers)
    if (foldFunction.isError()) return foldFunction.asError()
    val node = foldFunction.value!!

    // 3. Handle the Body
    val next = scanner.peek()
    if (next.isSuccess()) {
        val t = next.value!!
        val text = t.value

        if (text == "{") {
            // Block body: fold everything until the matching '}'
            //val bodyResult = parseNext(scanner)
            scanner.advance() // Consume '{'
            val bodyResult = parseBodyNext(scanner, SkeletonType.BRACE, "}")
            if (bodyResult.isError()) return bodyResult
            node.children.add(bodyResult.value!!)
        } else if (text == "=") {
            // Expression body: consume '=' and then everything until EOLN
            node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
            val exprResult = foldLineRemainder(scanner)
            if (exprResult.isError()) return exprResult.asError()
            node.children.addAll(exprResult.value!!)
        }
    }

    // 4. Optional: Consume a trailing EOLN to keep the root level clean
    if (!scanner.isAtEnd() && scanner.peek().value?.type == TokenType.EOLN) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
    }

    return success(node)
}


fun isOpeningToken(token: Token): Boolean {
    if (token.type != TokenType.OPERATOR) {
        return false
    }

    if (token.value == "{") {
        return true
    }

    return false
}

fun parseBodyNext(scanner: Scanner, skeletonType: SkeletonType, closingTokenText: String): TResult<SkeletonNode> {
    val rootNode = SkeletonNode(skeletonType)
    while (!scanner.isAtEnd()) {
        val lineTokens = scanner.linesTokens()
        if (lineTokens.isEmpty()) {
            continue
        }

        val lastToken = lineTokens.last()

        if (lastToken.value == closingTokenText) {
            return success(rootNode)
        }
        val skeleton = tokensToStatement(lineTokens)
        val isOpening = isOpeningToken(lastToken)
        if (isOpening) {
            lineTokens.removeLast()
            val bodyResult = parseBodyNext(scanner, skeletonType, "}")
            if (bodyResult.isError()) return bodyResult
            skeleton.children.add(bodyResult.value!!)

        }
        parenConservativeParenFolder(skeleton)
        rootNode.children.add(skeleton)
    }

    return success(rootNode)
}

fun previousOpenParenIndexOf(children: ListView<SkeletonNode>): Int {
    return children.toList().indexOfLast { it.type == SkeletonType.ATOM && it.token?.value == "(" }
}

fun parenConservativeParenFolder(skeleton: SkeletonNode) {
    val children = skeleton.children.toListView()
    val closeParenIndexOf = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == ")" }
    if (closeParenIndexOf == -1) {
        return
    }
    val smallView = children.slice(0, closeParenIndexOf)
    val openParenIndexOf = previousOpenParenIndexOf(smallView)
    if (openParenIndexOf == -1) {
        return
    }
    val startSection = smallView.slice(openParenIndexOf + 1)
    val openingSection = children.slice(0, openParenIndexOf)
    val closingSection = children.slice(closeParenIndexOf + 1)
    val newChildren = mutableListOf<SkeletonNode>()
    newChildren.addAll(openingSection.toList())
    newChildren.add(SkeletonNode(SkeletonType.PAREN, null, startSection.toList()))
    newChildren.addAll(closingSection.toList())
    skeleton.children.clear()
    skeleton.children.addAll(newChildren)
    parenConservativeParenFolder(skeleton)
}

fun Scanner.linesTokens(): MutableList<Token> {
    val scanner = this
    val lineTokens = mutableListOf<Token>()
    while (!scanner.isAtEnd()) {
        val peek = scanner.peek().value!!
        if (peek.type == TokenType.EOLN) break
        lineTokens.add(scanner.advance())
    }
    scanner.advance()
    return lineTokens
}

fun tokensToStatement(tokens: List<Token>): SkeletonNode {
    val node = SkeletonNode(SkeletonType.STATEMENT)
    for (t in tokens) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, t))
    }
    return node
}

fun foldFunctionHeader(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.FUNCTION)

    val modifierNodes = modifiersToAtoms(modifiers)
    node.children.addAll(modifierNodes)
    val keyword = scanner.advance() // Consume 'fun'
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))

    // 2. Parse Header: Use the restricted parser
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return peekRes.asError()

        val t = peekRes.value!!

        // IMPORTANT: In a restricted header parse, we only stop on real boundaries
        if (t.value == "{" || t.value == "=") break

        // If you want to keep the EOLN nodes for formatting, add them,
        // but DON'T 'break' the loop.
        if (t.type == TokenType.EOLN) {
            node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
            continue
        }


        // Use the restricted header parser here!
        val child = parseHeaderNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }
    return success(node)
}

/**
 * Helper to collect all nodes on the current line (used for expression bodies).
 */
private fun foldLineRemainder(scanner: Scanner): TResult<List<SkeletonNode>> {
    val list = mutableListOf<SkeletonNode>()
    while (!scanner.isAtEnd()) {
        val peek = scanner.peek().value!!
        if (peek.type == TokenType.EOLN) break

        val res = parseNext(scanner)
        if (res.isError()) {
            return error(res.errorMessage!!)
        }
        list.add(res.value!!)
    }
    return success(list)
}