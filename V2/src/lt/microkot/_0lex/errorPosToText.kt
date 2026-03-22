package lt.microkot._0lex

/**
 * Converts a raw character position into a readable "(line, column)" string.
 */
fun errorPosToText(source: String, position: Int): String {
    var line = 1
    var col = 1

    val atMost = position.coerceAtMost(source.length)
    for (i in 0 ..< atMost) {
        if (source[i] == '\n') {
            line++
            col = 1
        } else {
            col++
        }
    }
    return "($line,$col)"
}
