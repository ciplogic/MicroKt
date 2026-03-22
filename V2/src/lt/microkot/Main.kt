package lt.microkot

import lt.microkot._0lex.Scanner
import lt.microkot._1skelparser.foldChevrons
import lt.microkot._1skelparser.parseFoldingNodes
import lt.microkot._1skelparser.splitNodeIntoStatements
import lt.microkot._2parser.KotlinParsedFile
import lt.microkot._2parser.parseFile
import lt.microkot.common.*
import lt.microkot._3deepparser.ExpressionParser
import lt.microkot._3deepparser.parseExpression
import lt.microkot._3deepparser.setParserSource
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    demoPratt()

    val kotlinFiles = scanFolderByExtension("src", "kt")
    val parsedFiles = mutableListOf<KotlinParsedFile>()
    for (file in kotlinFiles) {
        val res = parseSingleFile(file)
        if (res.isError()) {
            println("Error parsing ${file.name}: ${res.errorMessage}")
            continue
        }
        parsedFiles.add(res.value!!)
    }
    println("Low level parsing complete. Total files: ${parsedFiles.size}")
}

fun parseSingleFile(file: File): TResult<KotlinParsedFile> {
    println("Parsing file: ${file.name}")
    val fileContent = readFileAsText(file.canonicalPath)
    val fileView = fileContent.toView()

    val scanner = Scanner(fileView)
    val skeletonParsedRes = parseFoldingNodes(scanner)
    if (skeletonParsedRes.isError()) {
        return skeletonParsedRes.asError()
    }
    println("Skeleton parsing complete")

    val skeletonRoot = skeletonParsedRes.value!!
    splitNodeIntoStatements(skeletonRoot)
    foldChevrons(skeletonRoot.children)
    println("Statements splitting complete")

    // Set the context for the Pratt parser error reporting
    setParserSource(fileContent)

    return parseFile(skeletonRoot, file.name)
}

fun demoPratt() {
    println("--- Pratt Parser Demo ---")
    val expressions = listOf(
        "a + b * c",
        "a * b + c",
        "a.foo(b, c) + d",
        "if (a == b) c else d",
        "x = y = z",
        "1 + 2 * 3 / (4 - 5)"
    )

    for (exprText in expressions) {
        val view = exprText.toView()
        val scanner = Scanner(view)
        val skelRes = parseFoldingNodes(scanner)
        if (skelRes.isError()) {
            println("Skel Error for '$exprText': ${skelRes.errorMessage}")
            continue
        }

        // The children of the root are our tokens/groups
        val nodes = skelRes.value!!.children.toListView()

        // Set the context for the Pratt parser demo
        setParserSource(exprText)
        val parser = ExpressionParser(nodes)
        val result = parser.parseExpression()
        setParserSource("")

        if (result.isSuccess()) {
            println("Input:  $exprText")
            println("Parsed: ${result.value}")
            println()
        } else {
            println("Error parsing '$exprText': ${result.errorMessage}")
        }
    }
    println("--- End of Demo ---\n")
}