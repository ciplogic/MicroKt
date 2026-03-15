package org.example._2skeleton

import org.example._0lex.*
import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success

// A FoldRule takes the scanner and returns a specific structured node
typealias FoldRule = (Scanner, List<Token>) -> TResult<SkeletonNode>
class SkeletonRule(val trigger: String, val folder: FoldRule)

// Shared rules array
private val structuralRules = arrayOf(
    SkeletonRule("enum", ::foldEnum),
    SkeletonRule("package", ::foldPackage),
    SkeletonRule("import", ::foldImport),
    SkeletonRule("typealias", ::foldTypeAlias),
    SkeletonRule("fun", ::foldFunction),
    SkeletonRule("var", ::foldVar),
    SkeletonRule("val", ::foldVar),
    SkeletonRule("class", ::foldClass)
)

fun parseReservedWordStatement(scanner: Scanner, modifiers: List<Token>, skeletonType: SkeletonType = SkeletonType.STATEMENT ): TResult<SkeletonNode> {
    val tokens = scanner.linesTokens()

    val node = SkeletonNode(skeletonType)
    node.children.addAll(modifiersToAtoms(modifiers))
    val lastToken = tokens.last()
    val isOpenParen = lastToken.value == "("
    if (isOpenParen) {
        tokens.removeLast()
    }
    for (token in tokens) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, token))
    }
    if (isOpenParen) {
        val bodyParse = parseBodyNext(scanner, SkeletonType.PAREN, ")")
        if (bodyParse.isError()) return bodyParse.asError()
        node.children.add(bodyParse.value!!)
    }

    return success(node)
}


fun parseNext(scanner: Scanner): TResult<SkeletonNode> {
    // 1. Grab any modifiers up front
    var modifiers = scanner.accumulateModifiers()

    var res = scanner.peek()
    if (res.isError()) {
        return res.asError()
    }
    if (res.value?.type == TokenType.EOLN) {
        scanner.advance()

        modifiers = scanner.accumulateModifiers()
        res = scanner.peek()
    }

    val token = res.value!!
    val text = token.value

    if (res.value.type != TokenType.KEYWORD) {
        scanner.advance()
        return success(SkeletonNode(SkeletonType.ATOM, token))
    }

    // 2. Dispatch to the rule matching the "trigger" token
    val indexOfRule = structuralRules.indexOfFirst { it.trigger == text }
    if (indexOfRule == -1) {
        scanner.advance()
        return success(SkeletonNode(SkeletonType.ATOM, token))
    }
    val rule = structuralRules.get(indexOfRule)

    // Pass the accumulated modifiers into the strategy
    val result = rule.folder(scanner, modifiers)
    if (result.isError()) {
        return result.asError()
    }
    return result
}

fun foldVar(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val resultNode = parseReservedWordStatement(scanner, modifiers, SkeletonType.VAR)
    return resultNode
}

fun Scanner.accumulateModifiers(): List<Token> {
    val modifiers = mutableListOf<Token>()
    val scanner = this

    while (!scanner.isAtEnd()) {
        val res = scanner.peek()
        if (res.isError()) break

        val token = res.value!!
        // Check if it's a keyword AND in our allowed modifier list
        val isGoodToken = isModifier(token.value) || token.type == TokenType.Annotation
        if (isGoodToken) {
            modifiers.add(scanner.advance())
        } else {
            break // Hit a non-modifier (like 'fun', 'class', or an identifier)
        }
    }
    return modifiers
}