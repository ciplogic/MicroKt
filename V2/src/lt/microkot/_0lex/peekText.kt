package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.peekText(): String {
    return peek().value?.value ?: ""
}
