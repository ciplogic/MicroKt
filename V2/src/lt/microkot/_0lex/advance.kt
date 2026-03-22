package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.advance(): Token {
    val res = peek()
    if (res.isError()) {
        pos++
        return Token(TokenType.None, source.slice(pos - 1, 1).toString(), pos - 1)
    }

    val token = res.value!!
    pos += token.value.length
    return token
}
