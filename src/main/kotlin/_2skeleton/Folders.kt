package org.example._2skeleton

import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success
import org.example._0lex.Scanner
import org.example._0lex.Token
import org.example._0lex.TokenType
import org.example._0lex.advance
import org.example._0lex.errorAt
import org.example._0lex.expect
import org.example._0lex.isAtEnd
import org.example._0lex.match
import org.example._0lex.peek
import org.example._0lex.peekText

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
        val headerNode = parseNext(scanner)
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

fun foldVar(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.RET)

    node.children.addAll(modifiersToAtoms(modifiers))

    scanner.advance() //var or val


    // 2. Parse Header: Use the restricted parser
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return peekRes.asError()

        val t = peekRes.value!!

        if (t.type == TokenType.EOLN) {
            return success(node)
        }

        // Use the restricted header parser here!
        val child = parseHeaderNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }
    return success(node)
}
fun foldReturn(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.RET)

    scanner.advance() //ret


    // 2. Parse Header: Use the restricted parser
    while (!scanner.isAtEnd()) {
        val peekRes = scanner.peek()
        if (peekRes.isError()) return peekRes.asError()

        val t = peekRes.value!!

        if (t.type == TokenType.EOLN) {
            return success(node)
        }

        // Use the restricted header parser here!
        val child = parseHeaderNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }
    return success(node)
}

fun foldWhile(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.WHILE)

    // Expect 'while'
    val kwRes = scanner.expect("while", SkeletonType.ATOM)
    if (kwRes.isError()) return kwRes.asError()
    node.children.add(kwRes.value!!)

    // 2. The Condition: ( ... )
    if (scanner.peekText() == "(") {
        // We use foldGroup which internally uses foldExpressionGroup now
        val condRes = foldExpressionGroup(scanner)
        if (condRes.isError()) return condRes.asError()
        node.children.add(condRes.value!!)
    } else {
        return error("Expected '(' after 'while' at ${scanner.errorAt(scanner.pos)}")
    }

    // 3. The Body: { ... } or single statement
    // parseBranch uses the logic-pass aware dispatcher
    val bodyRes = parseBranch(scanner)
    if (bodyRes.isError()) return bodyRes.asError()
    node.children.add(bodyRes.value!!)

    return success(node)
}


fun foldFor(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val keyword = scanner.advance() // 'for'
    val node = SkeletonNode(SkeletonType.FOR)

    for (m in modifiers) node.children.add(SkeletonNode(SkeletonType.ATOM, m))
    node.children.add(SkeletonNode(SkeletonType.ATOM, keyword))

    // 1. The Header: (item in collection)
    if (scanner.peek().value?.value == "(") {
        // We use foldGroup but we need to ensure 'in' is preserved as an ATOM
        val header = foldGroup(scanner, "(", ")", SkeletonType.PAREN)
        if (header.isError()) return header
        node.children.add(header.value!!)
    } else {
        return error("Expected '(' after 'for'")
    }

    // 2. The Body: { ... } or single statement
    val body = parseBranch(scanner)
    if (body.isError()) return body
    node.children.add(body.value!!)

    return success(node)
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

fun foldIf(scanner: Scanner, modifiers: List<Token>): TResult<SkeletonNode> {
    val node = SkeletonNode(SkeletonType.IF)

    // Add accumulated modifiers
    for (m in modifiers) {
        node.children.add(SkeletonNode(SkeletonType.ATOM, m))
    }

    // 1. Match 'if' keyword
    val ifToken = scanner.match("if")
    if (ifToken.isError()) return ifToken.asError()
    node.children.add(SkeletonNode(SkeletonType.ATOM, ifToken.value!!))

    // 2. Condition: ( ... )
    if (scanner.peekText() == "(") {
        val cond = foldGroup(scanner, "(", ")", SkeletonType.PAREN)
        if (cond.isError()) return cond.asError()
        node.children.add(cond.value!!)
    } else {
        return error("Expected '(' after 'if' at ${scanner.errorAt(scanner.pos)}")
    }

    // 3. Then-Branch (Block or single statement)
    val thenBranch = parseBranch(scanner)
    if (thenBranch.isError()) return thenBranch.asError()
    node.children.add(thenBranch.value!!)

    // 4. Optional Else-Branch
    if (scanner.peekText() == "else") {
        // Consume 'else'
        val elseToken = scanner.match("else").value!!
        node.children.add(SkeletonNode(SkeletonType.ATOM, elseToken))

        // The else branch can be another 'if' (else-if) or a block
        val elseBranch = parseBranch(scanner)
        if (elseBranch.isError()) return elseBranch.asError()
        node.children.add(elseBranch.value!!)
    }

    return success(node)
}


/**
 * Parses either a { block } or a single atomic expression/construct
 */
private fun parseBranch(scanner: Scanner): TResult<SkeletonNode> {
    val peek = scanner.peek().value!!
    if (peek.value == "{") {
        return foldGroup(scanner, "{", "}", SkeletonType.BRACE)
    } else {
        // Single statement branch
        return foldExpressionGroup(scanner)
    }
}


fun foldGroup(scanner: Scanner, open: String, close: String, type: SkeletonType): TResult<SkeletonNode> {
    val startToken = scanner.advance() // Consume '(' or '['
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
        if (peekRes.isError()) return error(peekRes.errorMessage!!)

        val t = peekRes.value!!

        if (t.type == TokenType.EOLN) {
            // CONSUME the EOLN as part of this construct
            node.children.add(SkeletonNode(SkeletonType.ATOM, scanner.advance()))
            break
        }

        val child = parseNext(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }

    return success(node)
}


