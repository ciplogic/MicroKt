package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get

fun getNumberLength(view: StringView): Int {
    if (!isDigit(view.get(0))) {
        return 0
    }
    var len = 1
    while (len < view.length && isDigit(view.get(len))) {
        len++
    }
    return len
}
