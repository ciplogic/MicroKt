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
import org.example._0lex.isAtEnd
import org.example._0lex.isModifier
import org.example._0lex.peek

// Shared rules array
private val structuralRules = arrayOf(
    SkeletonRule("enum", ::foldEnum),
    SkeletonRule("package", ::foldPackage),
    SkeletonRule("import", ::foldImport),
    SkeletonRule("typealias", ::foldTypeAlias),
    SkeletonRule("fun", ::foldFunction),
    SkeletonRule("class", ::foldClass),
    SkeletonRule("if", ::foldIf),
    SkeletonRule("return", ::foldReturn),
    SkeletonRule("var", ::foldVar),
    SkeletonRule("val", ::foldVar),
    SkeletonRule("while", ::foldWhile),
    SkeletonRule("for", ::foldFor),

    // Delimiters are now just rules!
    SkeletonRule("(", { s, m -> foldGroup(s, "(", ")", SkeletonType.PAREN) }),
    SkeletonRule("[", { s, m -> foldGroup(s, "[", "]", SkeletonType.BRACKET) }),
    SkeletonRule("{", { s, m -> foldGroup(s, "{", "}", SkeletonType.BRACE) }),
    SkeletonRule("<", { s, m -> foldGroup(s, "<", ">", SkeletonType.CHEVRON) })

)

fun parseNext(scanner: Scanner): TResult<SkeletonNode> {
    // 1. Grab any modifiers up front
    val modifiers = scanner.accumulateModifiers()

    val res = scanner.peek()
    if (res.isError()) {
        if (modifiers.isNotEmpty()) {
            return error("Expected construct after modifiers at ${scanner.errorAt(scanner.pos)}")
        }
        return res.asError()
    }

    val token = res.value!!
    val text = token.value
    //println("DEBUG: Dispatching ${token.value} at ${scanner.errorAt(token.value.start)}")
        
    // 2. Dispatch to the rule matching the "trigger" token
    for (rule in structuralRules) {
        if (text == rule.trigger) {
            // Pass the accumulated modifiers into the strategy
            val result = rule.folder(scanner, modifiers)
            if (result.isError()) {
                return result.asError()
            }
            return result
        }
    }

    // 3. Fallback: If we had modifiers but no rule matched
    if (modifiers.isNotEmpty()) {
        return error("Unsupported construct following modifiers '$text' at ${scanner.errorAt(scanner.pos)}")
    }
    // 3. Fallback for Identifiers, Operators, Numbers, EOLNs
    scanner.advance()
    return success(SkeletonNode(SkeletonType.ATOM, token))
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