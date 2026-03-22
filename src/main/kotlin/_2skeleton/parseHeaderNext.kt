package org.example._2skeleton

import org.example._0lex.*
import org.example.common.TResult
import org.example.common.asError
import org.example.common.isError
import org.example.common.success

data class PairRule(
    val open: String,
    val close: String,
    val type: SkeletonType
)

typealias HeaderParserMapper = (Scanner) -> TResult<SkeletonNode>

val headerPairs = arrayOf(
    PairRule("(", ")", SkeletonType.PAREN),
    PairRule("[", "]", SkeletonType.BRACKET),
    PairRule("<", ">", SkeletonType.CHEVRON),
    PairRule("{", "}", SkeletonType.CURLY)
)

fun foldGroupInHeader(
    scanner: Scanner,
    rule: PairRule,
    childParser: HeaderParserMapper
): TResult<SkeletonNode> {
    val opener = scanner.advance() // Consume the opener (e.g., '(')
    val node = SkeletonNode(rule.type, opener)

    while (!scanner.isAtEnd()) {
        val peekResult = scanner.peek()
        if (peekResult.isError()) return peekResult.asError()

        val peek = peekResult.value!!

        // If we hit the specific closer for this group, we are done
        if (peek.value == rule.close) {
            scanner.advance() // Consume the closer (e.g., ')')
            return success(node)
        }

        // Recursively parse the next element inside this group
        val child = childParser(scanner)
        if (child.isError()) return child
        node.children.add(child.value!!)
    }

    return error("Error ${scanner.errorAt(scanner.pos)}: Unclosed group: expected '${rule.close}' ")
}

fun parseHeaderNext(scanner: Scanner): TResult<SkeletonNode> {
    val res = scanner.peek()
    if (res.isError()) return res.asError()

    val token = res.value!!
    val text = token.value

    // Check if the current token is a known opener
    for (rule in headerPairs) {
        if (text == rule.open) {
            // Recurse using this same function for children
            return foldGroupInHeader(scanner, rule, ::parseHeaderNext)
        }
    }

    // Fallback: If not an opener, it's just an ATOM (identifier, keyword, etc.)
    scanner.advance()
    return success(SkeletonNode(SkeletonType.ATOM, token))
}
