package org.example._2skeleton

import org.example._0lex.Scanner
import org.example._0lex.Token
import org.example.common.TResult

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
    BODY,
    STATEMENT,
    FUNCTION,
    PROPERTY,
    VAR,
}


// A FoldRule takes the scanner and returns a specific structured node
typealias FoldRule = (Scanner, List<Token>) -> TResult<SkeletonNode>

class SkeletonRule(val trigger: String, val folder: FoldRule)
