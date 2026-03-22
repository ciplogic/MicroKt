package lt.microkot._0lex

import lt.microkot.common.*
import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType

fun Scanner.expect(text: String, nodeType: SkeletonType): TResult<SkeletonNode> {
    val tokenRes = peek()
    if (tokenRes.isError()) {
        return tokenRes.asError()
    }

    val token = tokenRes.value!!
    if (token.value != text) {
        return error("Expected '$text' but found '${token.value}' at ${errorAt(pos)}")
    }

    advance()
    return success(SkeletonNode(nodeType, token))
}
