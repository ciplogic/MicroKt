package lt.microkot._0lex

import lt.microkot.common.StringView

// Define the functional interface for matching
typealias LexerRule = (StringView) -> Int

// Custom class to replace Pair for rule definitions
data class MatchPair(val type: TokenType, val rule: LexerRule)

// Simple data class for the return value of findMatch
class MatchResult(val type: TokenType, val length: Int)

val keywords = arrayOf(
    "package", "import", "class", "interface", "fun", "val", "var", "for", "while", "do", "when",
    "as", "is", "in", "this", "super",
    "break", "continue", "object",
    "try", "catch", "finally", "throw",
    "enum",
    "if", "else", "when", "return", "typealias", "this", "true", "false", "null",
    "public", "private", "protected", "internal",
    "override", "final", "abstract", "reified",
    "sealed", "data", "inline", "noinline", "crossinline", "tailrec",
    "external", "annotation", "latex", "operator", "infix", "suspend"
)

val lexingRules: Array<MatchPair> = arrayOf(
    MatchPair(TokenType.Comment, ::getCommentLength),
    MatchPair(TokenType.EOLN, ::getEolnLength),
    MatchPair(TokenType.STRING, ::getStringLength),
    MatchPair(TokenType.KEYWORD, ::getKeywordLength),
    MatchPair(TokenType.IDENTIFIER, ::getIdentifierLength),
    MatchPair(TokenType.Annotation, ::getAnnotationLength),
    MatchPair(TokenType.NUMBER, ::getNumberLength),
    MatchPair(TokenType.OPERATOR, ::getOperatorLength)
)

val modifiers = arrayOf(
    "public", "private", "protected", "internal",
    "override", "final", "abstract", "reified",
    "sealed", "data", "inline", "noinline", "crossinline", "tailrec",
    "external", "annotation", "latex", "operator", "infix", "suspend"
)
