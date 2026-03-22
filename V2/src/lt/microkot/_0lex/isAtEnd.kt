package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.isAtEnd(): Boolean {
    skipWhiteSpaces()
    return pos >= source.length
}
