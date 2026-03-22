package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.isError
import lt.microkot.common.asError


fun parseTopLevelStatement(
    kotlinParsedFile: KotlinParsedFile,
    statement: SkeletonNode
): TResult<Unit> {
    val keyword = getAtomValueOfList(statement.children, 0)
    if (keyword == "") {
        return success(Unit)
    }

    if (keyword == "package") {
        // Warning: pkgName here is a local copy.
        // In a self-compiler, we'd usually return it or use a State object.
        kotlinParsedFile.packageName = parsePackage(statement)
    } else if (keyword == "import") {
        kotlinParsedFile.imports.add(parseImport(statement))
    } else if (keyword == "enum") {
        kotlinParsedFile.declarations.add(parseEnum(statement))
    } else {
        // This is the "Look for Modifiers" branch
        val res = parseDeclaration(statement)
        if (res.isError()) {
            return res.asError()
        }
        val declaration = res.value
        if (declaration != null) {
            kotlinParsedFile.declarations.add(declaration)
        }
    }
    return success(Unit)
}
