package lt.microkot._0lex

fun isIdentifierPart(c: Char): Boolean {
    return isIdentifierStart(c) || (c in '0'..'9')
}
