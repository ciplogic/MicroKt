package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.errorAt(pos: Int): String {
    return errorPosToText(source.value, pos)
}
