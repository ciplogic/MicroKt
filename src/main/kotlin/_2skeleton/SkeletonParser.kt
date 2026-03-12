package org.example._2skeleton

import org.example._0lex.*
import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success

// Shared rules array
private val structuralRules = arrayOf(
    SkeletonRule("enum", ::foldEnum),
    SkeletonRule("package", ::foldPackage),
    SkeletonRule("import", ::foldImport),
    SkeletonRule("typealias", ::foldTypeAlias),
    SkeletonRule("fun", ::foldFunction),
    SkeletonRule("class", ::foldClass)
)

fun parseStatement(scanner: Scanner, modifiers: List<Token>, skeletonType: SkeletonType = SkeletonType.STATEMENT ): TResult<SkeletonNode> {
    val node = SkeletonNode(skeletonType)
    node.children.addAll(modifiersToAtoms(modifiers))
    while (!scanner.isAtEnd()) {
        val res = parseNext(scanner)
        if (res.isError()) return res.asError()
        node.children.add(res.value!!)
    }
    return success(node)
}

fun parseNext(scanner: Scanner): TResult<SkeletonNode> {
    // 1. Grab any modifiers up front
    val modifiers = scanner.accumulateModifiers()

    var res = scanner.peek()
    if (res.isError()) {
        return res.asError()
    }
    if (res.value?.type == TokenType.EOLN) {
        scanner.advance()
        res = scanner.peek()
    }

    val token = res.value!!
    val text = token.value
    //println("DEBUG: Dispatching ${token.value} at ${scanner.errorAt(token.value.start)}")

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
    val rule = structuralRules[indexOfRule]

    // Pass the accumulated modifiers into the strategy
    val result = rule.folder(scanner, modifiers)
    if (result.isError()) {
        return result.asError()
    }
    return result
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