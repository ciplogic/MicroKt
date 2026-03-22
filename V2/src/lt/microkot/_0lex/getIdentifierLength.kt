package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get

fun getIdentifierLength(view: StringView): Int {
    if (!isIdentifierStart(view.get(0))) {
        return 0
    }
    var len = 1
    while ((len < view.length) && isIdentifierPart(view.get(len))) {
        len++
    }
    return len
}
