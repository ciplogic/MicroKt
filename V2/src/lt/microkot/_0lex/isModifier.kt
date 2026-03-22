package lt.microkot._0lex

fun isModifier(text: String): Boolean {
    for (m in modifiers) {
        if (text == m) {
            return true
        }
    }
    return false
}
