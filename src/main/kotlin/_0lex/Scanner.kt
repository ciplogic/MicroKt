package org.example._0lex

import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example.common.*

class Scanner(val source: StringView) {
    var pos: Int = 0

    override fun toString(): String {
        return source.slice(pos).toString()
    }
}

private fun Scanner.skipWhiteSpaces() {
    while (pos < source.length) {
        val currentView = source.slice(pos)

        // Check for standard whitespace
        if (isWhitespace(source.get(pos))) {
            pos++
            continue
        }

        // Check for comments using our existing rule
        val commentLen = getCommentLength(currentView)
        if (commentLen > 0) {
            pos += commentLen
            continue
        }

        // If neither, we've reached a meaningful token
        break
    }
}

fun Scanner.peek(): TResult<Token> {
    skipWhiteSpaces()

    if (pos >= source.length) {
        return success(Token(TokenType.EndOfFile, ""))
    }

    val currentView = source.slice(pos)

    val match = findMatch(currentView)

    if (match != null) {
        // Note: We don't advance 'pos' here, just view the token
        return success(Token(match.type, source.slice(pos, pos + match.length).toString()))
    } else {
        val location = errorAt(currentView.start)
        return error("Lexer error at $location: Unknown character '${source.get(pos)}'")
    }
}

fun Scanner.errorAt(pos: Int): String {
    var line = 1
    var col = 1

    // We iterate from 0 to the current position in the underlying string
    for (i in 0 until pos) {
        if (source.value[i] == '\n') {
            line++
            col = 1
        } else {
            col++
        }
    }
    return "($line,$col)"
}

fun Scanner.advance(): Token {
    val res = peek()
    if (res.isError()) {
        pos++
        return Token(TokenType.None, source.slice(pos - 1, 1).toString())
    }

    val token = res.value!!
    pos += token.value.length
    return token
}


fun Scanner.isAtEnd(): Boolean {
    skipWhiteSpaces()
    return pos >= source.length
}

fun Scanner.peekText(): String {
    return peek().value?.value ?: ""
}

fun Scanner.skipIf(text: String): Boolean {
    if (peekText() == text) {
        advance()
        return true
    }
    return false
}

fun Scanner.expect(text: String, nodeType: SkeletonType): TResult<SkeletonNode> {
    val tokenRes = peek()
    if (tokenRes.isError()) return tokenRes.asError()

    val token = tokenRes.value!!
    if (token.value != text) {
        return error("Expected '$text' but found '${token.value}' at ${errorAt(pos)}")
    }

    advance()
    return success(SkeletonNode(nodeType, token))
}

fun Scanner.match(text: String): TResult<Token> {
    val res = this.peek()
    if (res.isError()) return res.asError()

    val token = res.value!!
    if (token.value == text) {
        this.advance()
        return success(token)
    }

    return error("Expected '$text' but found '${token.value}' at ${this.errorAt(this.pos)}")
}
