package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.startsWith

fun getKeywordLength(view: StringView): Int {
    val idLen = getIdentifierLength(view)
    if (idLen == 0) {
        return 0
    }

    // A keyword must match the full identifier length to prevent partial matches (e.g. "valiant")
    for (kw in keywords) {
        if (kw.length == idLen && view.startsWith(kw)) {
            return kw.length
        }
    }
    return 0
}
