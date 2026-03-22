package lt.microkot._0lex

import lt.microkot.common.*

fun Scanner.skipWhiteSpaces() {
    while (pos < source.length) {
        val currentView = source.slice(pos)

        // Check for standard whitespace
        if (isWhitespace(source.get(pos))) {
            pos++
            continue
        }

        // Check for comments using our existing rule
        val commentLen = getCommentLength(currentView)
        if (commentLen > 0) {
            pos += commentLen
            continue
        }

        // If neither, we've reached a meaningful token
        break
    }
}
