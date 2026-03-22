package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.skipIf(text: String): Boolean {
    if (peekText() == text) {
        advance()
        return true
    }
    return false
}
