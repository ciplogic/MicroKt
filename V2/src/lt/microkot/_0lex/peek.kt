package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.peek(): TResult<Token> {
    skipWhiteSpaces()

    if (pos >= source.length) {
        return success(Token(TokenType.EndOfFile, "", pos))
    }

    val currentView = source.slice(pos)

    val match = findMatch(currentView)

    if (match != null) {
        // Capture the position before advancing or returning
        val startPos = pos
        return success(Token(match.type, source.slice(pos, pos + match.length).toString(), startPos))
    }
    
    val location = errorAt(pos)
    return error("Error $location: Unknown character '${source.get(pos)}'")
}
