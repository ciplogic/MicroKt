package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.isError
import lt.microkot.common.asError


fun parseFile(root: SkeletonNode, fileName: String): TResult<KotlinParsedFile> {
    val result = KotlinParsedFile(fileName, "", mutableListOf(), mutableListOf())

    val statements = root.children
    var i = 0
    val size = statements.size

    while (i < size) {
        val statement = statements.get(i)

        if (statement.type == SkeletonType.Statement) {
            if (statement.children.size > 0) {
                // We pass the source down to all parsers that might need to parse expressions
                val res = parseTopLevelStatement(result, statement)
                if (res.isError()) {
                    return res.asError()
                }
            }
        }
        i = i + 1
    }

    return success(result)
}