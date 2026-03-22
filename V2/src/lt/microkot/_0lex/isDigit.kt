package lt.microkot._0lex

fun isDigit(c: Char): Boolean {
    var result = c in '0'..'9'
    return result
}
