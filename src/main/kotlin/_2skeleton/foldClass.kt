package org.example._2skeleton

import org.example._0lex.*
import org.example.common.*

fun modifiersToAtoms(modifiers: List<Token>): List<SkeletonNode> {
    var result = mutableListOf<SkeletonNode>()
    for (m in modifiers) {
        result.add(SkeletonNode(SkeletonType.ATOM, m))
    }
    return result
}

fun foldClassHeader(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.CLASS)
    val modifierNodes = modifiersToAtoms(modifiers)
    node.children.addAll(modifierNodes)
    val keyword = scanner.advance() // Consume 'class'
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))
    // 1. Parse Header: Name, <Generics>, (Constructor Params)
    // Stop at '{' or 'EOLN' (for empty classes)
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) {
            return peekRes.asError()
        }

        val t = peekRes.value!!
        if (t.value == "{" || t.type == TokenType.EOLN) break

        val child = parseHeaderNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }
    return success(node)
}

fun foldClass(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val nodeResult = foldClassHeader(scanner, modifiers)
    if (nodeResult.isError()) return nodeResult
    val node = nodeResult.value!!

    // 2. Parse Body: Can only contain Properties (val/var) or EOLNs
    if (!scanner.isAtEnd() && scanner.peek().value?.value == "{") {
        val bodyResult = foldClassBody(scanner)
        if (bodyResult.isError()) return bodyResult
        node.children.add(bodyResult.value!!)
    }

    return success(node)
}

// In Folders.kt
fun foldClassProperty(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val keyword = scanner.advance() // 'val' or 'var'
    val node = SkeletonNode(SkeletonType.PROPERTY)
    node.children.addAll(modifiersToAtoms(modifiers))

    // 1. Attach modifiers and keyword
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))

    // 2. Parse Identifier (Name)
    val nameRes = scanner.peek()
    if (nameRes.isError() || nameRes.value!!.type != TokenType.IDENTIFIER) {
        return error("Expected property name at ${scanner.errorAt(scanner.pos)}")
    }
    node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))

    // 3. Parse Optional Type (: StringView)
    if (scanner.peek().value?.value == ":") {
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance())) // ':'
        // Use foldExpressionGroup to handle potential Generics in the type
        val typeRes = foldExpressionGroup(scanner)
        if (typeRes.isError()) return typeRes
        node.children.add(typeRes.value!!)
    }

    // 4. Parse Optional Initializer (= 0)
    if (scanner.peek().value?.value == "=") {
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance())) // '='
        // Everything until EOLN or '}' is the value
        while (!scanner.isAtEnd()) {
            val p = scanner.peek().value!!
            if (p.type == TokenType.EOLN || p.value == "}") break

            val expr = foldExpressionGroup(scanner)
            if (expr.isError()) return expr
            node.children.add(expr.value!!)
        }
    }

    // 5. Consume Terminator
    if (scanner.peek().value?.type == TokenType.EOLN) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
    }

    return success(node)
}

private fun foldClassBody(scanner: Scanner): TResult<SkeletonNode> {
    scanner.advance() // '{'
    val bodyNode = SkeletonNode(SkeletonType.BRACE)

    while (!scanner.isAtEnd()) {
        val peekResult = scanner.peek()
        if (peekResult.isError()) return peekResult.asError()
        val peek = peekResult.value!!

        val text = peek.value
        if (text == "}") {
            scanner.advance()
            break
        }

        if (peek.type == TokenType.EOLN) {
            scanner.advance()
            continue
        }

        // 1. Handle Modifiers (private, open, etc.)
        val mods = scanner.accumulateModifiers()

        // 2. Peek at the actual trigger after modifiers
        val nextResult = scanner.peek()
        if (nextResult.isError()) return nextResult.asError()
        val next = nextResult.value!!
        val nextText = next.value

        if (nextText == "val" || nextText == "var") {
            // Handle Properties
            val prop = foldClassProperty(scanner, mods)
            if (prop.isError()) return prop.asError()
            bodyNode.children.add(prop.value!!)
        } else if (nextText == "fun") {
            // ALLOW FUNCTIONS: Fold them and add to the body
            val func = foldClassFunction(scanner, mods)
            if (func.isError()) {
                return func.asError()
            }
            bodyNode.children.add(func.value!!)
        } else if (next.type == TokenType.EOLN) {
            bodyNode.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
        } else {
            return error("Unexpected token '$nextText' in class body at ${scanner.errorAt(scanner.pos)}")
        }
    }
    return success(bodyNode)
}

fun foldClassFunction(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
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
            val bodyResult = parseHeaderNext(scanner)
            if (bodyResult.isError()) return bodyResult
            node.children.add(bodyResult.value!!)
        } else if (text == "=") {
            // in class we support only curly braces for function bodies
            return error("Expected '{' after function name at ${scanner.errorAt(scanner.pos)}")
        }
    }

    // 4. Optional: Consume a trailing EOLN to keep the root level clean
    if (!scanner.isAtEnd() && scanner.peek().value?.type == TokenType.EOLN) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
    }

    return success(node)
}
