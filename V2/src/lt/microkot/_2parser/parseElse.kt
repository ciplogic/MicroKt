package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError


fun parseElse(statementNode: SkeletonNode): TResult<ElseStatement> {
    val children = statementNode.children
    var i = 1 // skip "else"
    
    var block: Block? = null
    
    while (i < children.size) {
        val child = children.get(i)
        if (child.type == SkeletonType.CurlyBraces && block == null) {
            val res = parseBlock(child)
            if (res.isError()) {
                return res.asError()
            }
            block = res.value!!
        }
        i++
    }
    
    if (block != null) {
        return success(ElseStatement(block))
    }
    
    return error("Could not parse else statement (missing block).")
}
