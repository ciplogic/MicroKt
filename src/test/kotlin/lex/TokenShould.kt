package lex

import org.example._0lex.Token
import org.example._0lex.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenShould {
    @Test
    fun handleToString() {
        val token = Token(TokenType.None, "test")
        assertEquals("test", token.toString())
    }

}