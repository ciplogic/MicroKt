package org.example._2skeleton

import org.example._0lex.*
import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success

fun foldEnum(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    // 1. Consume 'enum'
    val enumKeyword = scanner.advance()

    // 2. Consume 'class' (Kotlin requires 'enum class')
    val nextResult = scanner.peek()
    if (nextResult.isError()) {
        return error(nextResult.errorMessage!!)
    }

    if (nextResult.value!!.value != "class") {
        return error("Expected 'class' after 'enum' at ${scanner.errorAt(scanner.pos)}")
    }
    val classKeyword = scanner.advance()

    val node = SkeletonNode(SkeletonType.ENUM)
    node.children.add(SkeletonNode(SkeletonType.ATOM, enumKeyword))
    node.children.add(SkeletonNode(SkeletonType.ATOM, classKeyword))

    // 3. Parse Header (Name, potentially Interfaces/Generics)
    // We stop when we hit '{'
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return error(peekRes.errorMessage!!)

        if (peekRes.value!!.value == "{") break

        // Use parseNext to handle nested parens/chevrons in the header
        val headerNode = parseHeaderNext(scanner)
        if (headerNode.isError()) return headerNode
        node.children.add(headerNode.value!!)
    }

    // 4. Parse Body
    if (!scanner.isAtEnd() && scanner.peek().value!!.value == "{") {
        val bodyResult = foldGroup(scanner, "{", "}", SkeletonType.BRACE)
        if (bodyResult.isError()) return bodyResult
        node.children.add(bodyResult.value!!)
    }

    return success(node)
}

fun foldPackage(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    return foldLineConstruct(scanner, SkeletonType.PACKAGE)
}

fun foldImport(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    return foldLineConstruct(scanner, SkeletonType.IMPORT)
}

fun foldTypeAlias(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val keyword = scanner.advance() // 'typealias'
    val node = SkeletonNode(SkeletonType.TYPEALIAS)
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))

    // Consume until EOLN (The "Name = Definition" part)
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return error(peekRes.errorMessage!!)

        val t = peekRes.value!!
        // Stop at the end of the line
        if (t.type == TokenType.EOLN) {
            scanner.advance()
            break
        }

        // Use parseNext so (StringView) -> Int gets folded into PAREN and ATOMS
        val child = parseHeaderNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }

    return success(node)
}

fun foldGroup(scanner: Scanner, open: String, close: String, type: SkeletonType): TResult<SkeletonNode> {
    scanner.advance() // Consume '(' or '['
    val node = SkeletonNode(type)

    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return peekRes.asError()
        val token = peekRes.value!!
        val text = token.value

        if (text == close) {
            scanner.advance() // Success!
            return success(node)
        }

        // DISAMBIGUATION: Inside a group, we use the expression folder
        // which knows how to "unroll" a < if it's an operator
        val childRes = foldExpressionGroup(scanner)
        if (childRes.isError()) return childRes
        node.children.add(childRes.value!!)
    }

    return error("Unclosed delimiter '$open' at ${scanner.errorAt(scanner.pos)}")
}

private fun foldLineConstruct(scanner: Scanner, type: SkeletonType): TResult<SkeletonNode> {
    val keyword = scanner.advance()
    val node = SkeletonNode(type)
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))

    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) {
            return error(peekRes.errorMessage!!)
        }

        val t = peekRes.value!!

        if (t.type == TokenType.EOLN) {
            scanner.advance()
            break
        }
        node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
    }

    return success(node)
}


