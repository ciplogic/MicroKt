package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get

fun getStringLength(view: StringView): Int {
    val quote = view.get(0)
    // Support both "string" and 'char'
    if (quote != '"' && quote != '\'') {
        return 0
    }

    var len = 1
    while (len < view.length) {
        val c = view.get(len)

        // Handle escaped quotes: \" or \'
        if (c == '\\' && len + 1 < view.length) {
            len += 2
            continue
        }

        // If we find the matching closing quote
        if (c == quote) {
            return len + 1
        }
        len++
    }

    return 0 // Unterminated string or char literal
}
