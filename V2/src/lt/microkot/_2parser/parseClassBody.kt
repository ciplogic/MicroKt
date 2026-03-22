package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.isError
import lt.microkot.common.asError


fun parseClassBody(
    curlyNode: SkeletonNode,
    properties: MutableList<PropertyDeclaration>,
    functions: MutableList<FunctionDeclaration>
): TResult<Unit> {
    val children = curlyNode.children
    var i = 0
    val size = children.size

    while (i < size) {
        val statement = children.get(i)

        // Inside the CurlyBraces, the Folder already grouped
        // tokens into Statement nodes.
        if (statement.type == SkeletonType.Statement) {
            val res = parseDeclaration(statement)
            if (res.isError()) {
                return res.asError()
            }
            val decl = res.value

            if (decl != null) {
                // Since we don't have 'is' or 'when',
                // we check the base type or a manual flag.
                if (decl is PropertyDeclaration) {
                    properties.add(decl)
                } else if (decl is FunctionDeclaration) {
                    functions.add(decl)
                }
            }
        }
        i = i + 1
    }
    return success(Unit)
}
