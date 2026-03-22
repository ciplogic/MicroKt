package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError


fun parseBlock(curlyNode: SkeletonNode): TResult<Block> {
    val statements = mutableListOf<Statement>()
    var i = 0
    val size = curlyNode.children.size

    while (i < size) {
        val child = curlyNode.children.get(i)
        if (child.type == SkeletonType.Statement) {
            val res = parseStatement(child)
            if (res.isError()) {
                return res.asError()
            }
            val statement = res.value
            if (statement != null) {
                statements.add(statement)
            }
        }
        i = i + 1
    }
    return success(Block(statements))
}
