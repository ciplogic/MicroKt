package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get
import lt.microkot.common.startsWith

fun getCommentLength(view: StringView): Int {
    if (view.length < 2) {
        return 0
    }
    if (view.startsWith("//")) {
        var len = 2
        while (len < view.length && view.get(len) != '\n') {
            len++
        }
        return len
    }
    if (view.startsWith("/*")) {
        var len = 2
        while (len < view.length - 1) {
            if (view.get(len) == '*' && view.get(len + 1) == '/') {
                return len + 2
            }
            len++
        }
    }
    return 0
}
