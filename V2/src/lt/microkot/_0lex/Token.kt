package lt.microkot._0lex

data class Token(
    val type: TokenType,
    val value: String,
    val position: Int = -1
)
