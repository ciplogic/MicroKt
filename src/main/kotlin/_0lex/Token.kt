package org.example._0lex

data class Token(val type: TokenType, val value: String) {
    override fun toString(): String {
        if (this.type == TokenType.EOLN) {
            return "\\n"
        }
        return value
    }
}

