package org.example._2skeleton

import org.example._0lex.Token

enum class SkeletonType {
    CONSTRUCT,   // A complex structure like: [Keyword] + [Header Nodes] + [Body Node]
    ATOM,       // A single Token (val, identifier, etc.)
    PAREN,      // (...)
    CURLY,      // {...}
    BRACKET,    // [...]
    CHEVRON,    // <...>
    ENUM,
    IMPORT,
    PACKAGE,
    TYPEALIAS,
    CLASS,
    BODY,
    STATEMENT,
    FUNCTION,
    PROPERTY,
    VAR,
}


class SkeletonNode(
    val type: SkeletonType,
    val token: Token? = null,
    val children: MutableList<SkeletonNode> = mutableListOf()
) {
    override fun toString(): String {
        val sb = StringBuilder()
        if (this.token != null) {
            sb.append(this.token.toString())
        }
        for (child in children) {
            sb.append(child.toString())
        }
        return sb.toString()
    }
}
