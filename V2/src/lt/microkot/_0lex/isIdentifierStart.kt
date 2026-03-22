package lt.microkot._0lex

fun isIdentifierStart(c: Char): Boolean {
    return (c in 'a'..'z') || (c in 'A'..'Z') || c == '_'
}
