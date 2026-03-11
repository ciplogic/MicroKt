package org.example._2skeleton

import org.example.common.TResult
import org.example._0lex.Scanner
import org.example._0lex.Token

enum class SkeletonType {
    CONSTRUCT,   // A complex structure like: [Keyword] + [Header Nodes] + [Body Node]
    ATOM,       // A single Token (val, identifier, etc.)
    PAREN,      // (...)
    BRACE,      // {...}
    BRACKET,    // [...]
    CHEVRON,    // <...>
    ENUM,
    IMPORT,
    PACKAGE,
    TYPEALIAS,
    CLASS,
    RET,
    IF,
    WHILE,
    PROPERTY,
    FUNCTION,
    FOR
}


// A FoldRule takes the scanner and returns a specific structured node
typealias FoldRule = (Scanner, List<Token>) -> TResult<SkeletonNode>

class SkeletonRule(val trigger: String, val folder: FoldRule)
