package org.example._0lex

import org.example.common.StringView
import org.example.common.get
import org.example.common.slice
import org.example.common.startsWith

// Define the functional interface for matching
typealias LexerRule = (StringView) -> Int

// Custom class to replace Pair for rule definitions
data class MatchPair(val type: TokenType, val rule: LexerRule){}

// Simple data class for the return value of findMatch
class MatchResult(val type: TokenType, val length: Int){}

fun getIdentifierLength(view: StringView): Int {
    if ( !isIdentifierStart(view.get(0))) {
        return 0
    }
    var len = 1
    while ((len < view.length) && isIdentifierPart(view.get(len))) {
        len++
    }
    return len
}
fun getAnnotationLength(view: StringView): Int {
    if ( view.get(0) != '@') {
        return 0
    }

    var identifierLength = getIdentifierLength(view.slice(1))
    if (identifierLength == 0) return 0
    return 1 + identifierLength
}

// --- Helper predicates for single characters ---
fun isIdentifierStart(c: Char): Boolean {
    return (c in 'a'..'z') || (c in 'A'..'Z') || c == '_'
}

fun isIdentifierPart(c: Char): Boolean {
    return isIdentifierStart(c) || (c in '0'..'9')
}

fun isDigit(c: Char): Boolean {
    var result = c in '0'..'9'
    return result
}

// --- Token Length Matchers ---

fun getEolnLength(view: StringView): Int {
    var firstChar = view.get(0)
    if (firstChar != '\n' && firstChar != '\r') return 0
    val textOfChars = "\r\n\t "
    var len = 0
    while (len < view.length){
        if (!textOfChars.contains(view.get(len))) {
            break
        }
        len++
    }

    return len
}

val keywords = arrayOf(
    "package", "import", "class", "interface", "fun", "val", "var", "for", "while", "do", "when",
    "as", "type", "is", "in", "this", "super",
    "break", "continue", "object",
    "try", "catch", "finally", "throw",
    "enum",
    "if", "else", "when", "return", "typealias", "this", "true", "false", "null",
    "public", "private", "protected", "internal",
    "override",  "final", "abstract", "reified",
    "sealed", "data", "inline", "noinline", "crossinline", "tailrec",
    "external", "annotation", "latex", "operator", "infix", "suspend"
)

fun getKeywordLength(view: StringView): Int {
    val idLen = getIdentifierLength(view)
    if (idLen == 0) return 0

    // A keyword must match the full identifier length to prevent partial matches (e.g. "valiant")
    for (kw in keywords) {
        if (kw.length == idLen && view.startsWith(kw)) {
            return kw.length
        }
    }
    return 0
}

fun isWhitespace(c: Char): Boolean {
    return c == ' ' || c == '\t'
}

fun getNumberLength(view: StringView): Int {
    if ( !isDigit(view.get(0))) return 0
    var len = 1
    while (len < view.length && isDigit(view.get(len))) {
        len++
    }
    return len
}

val multiCharOps = arrayOf("..", "==", "!=", "<=", ">=", "&&", "||", "->", "::", "++", "--")

fun getOperatorLength(view: StringView): Int {
    // Check multi-char operators first
    for (op in multiCharOps) {
        if (view.startsWith(op)) return op.length
    }

    // Added '?' and '=' and '!' to the allowed single characters
    val c = view.get(0)
    val singleOps = "+-*/%=<>!&|:.,()[]{}?;"
    if (singleOps.contains(c)) {
        return 1
    } else return 0
}

fun getStringLength(view: StringView): Int {
    val quote = view.get(0)
    // Support both "string" and 'char'
    if (quote != '"' && quote != '\'') return 0

    var len = 1
    while (len < view.length) {
        val c = view.get(len)

        // Handle escaped quotes: \" or \'
        if (c == '\\' && len + 1 < view.length) {
            len += 2
            continue
        }

        // If we find the matching closing quote
        if (c == quote) {
            return len + 1
        }
        len++
    }

    return 0 // Unterminated string or char literal
}

fun getCommentLength(view: StringView): Int {
    if (view.length < 2) return 0
    if (view.startsWith("//")) {
        var len = 2
        while (len < view.length && view.get(len) != '\n') len++
        return len
    }
    if (view.startsWith("/*")) {
        var len = 2
        while (len < view.length - 1) {
            if (view.get(len) == '*' && view.get(len + 1) == '/') return len + 2
            len++
        }
    }
    return 0
}


val rules: Array<MatchPair> = arrayOf(
    MatchPair(TokenType.Comment, ::getCommentLength),
    MatchPair(TokenType.EOLN, ::getEolnLength),
    MatchPair(TokenType.STRING, ::getStringLength),
    MatchPair(TokenType.KEYWORD, ::getKeywordLength),
    MatchPair(TokenType.IDENTIFIER, ::getIdentifierLength),
    MatchPair(TokenType.Annotation, ::getAnnotationLength),
    MatchPair(TokenType.NUMBER, ::getNumberLength),
    MatchPair(TokenType.OPERATOR, ::getOperatorLength)
)

/**
 * Iterates through custom MatchPairs to find a valid token boundary.
 */
fun findMatch(view: StringView): MatchResult? {
    for (mp in rules) {
        val length = mp.rule(view)
        if (length > 0) return MatchResult(mp.type, length)
    }
    return null
}

val modifiers = arrayOf(
    "public", "private", "protected", "internal",
    "override", "final", "abstract", "reified",
    "sealed", "data", "inline", "noinline", "crossinline", "tailrec",
    "external", "annotation", "latex", "operator", "infix", "suspend"
)

fun isModifier(text: String): Boolean {
    for (m in modifiers) {
        if (text == m) return true
    }
    return false
}
