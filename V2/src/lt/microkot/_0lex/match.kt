package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.match(text: String): TResult<Token> {
    val res = this.peek()
    if (res.isError()) {
        return res.asError()
    }

    val token = res.value!!
    if (token.value == text) {
        this.advance()
        return success(token)
    }

    return error("Expected '$text' but found '${token.value}' at ${this.errorAt(this.pos)}")
}
