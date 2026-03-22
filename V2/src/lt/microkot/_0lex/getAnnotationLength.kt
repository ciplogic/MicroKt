package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get
import lt.microkot.common.slice

fun getAnnotationLength(view: StringView): Int {
    if (view.get(0) != '@') {
        return 0
    }

    var identifierLength = getIdentifierLength(view.slice(1))
    if (identifierLength == 0) {
        return 0
    }
    return 1 + identifierLength
}
