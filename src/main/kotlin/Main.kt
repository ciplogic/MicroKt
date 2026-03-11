package org.example

import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._2skeleton.parseNext
import org.example._3midparse.CompilationUnit
import org.example._3midparse.semanticAnalyze
import org.example._4globalsymbols.semanticCollectSymbols
import org.example._5codegen.generateOutputCode
import org.example.common.isError
import org.example.common.readFileAsText
import org.example.common.scanFolderByExtension
import org.example.common.toView
import org.example._0lex.Scanner
import org.example._0lex.Token
import org.example._0lex.TokenType
import org.example._0lex.isAtEnd
import java.io.File

fun Token.toDisplayString(): String {
    // If it's an EOLN, just show a single symbolic marker
    if (this.type == TokenType.EOLN) return "\\n"

    // Otherwise, return the actual text from the StringView
    return this.value
}

fun main(args: Array<String>) {
    parseFileToCompilationUnit(File("src/main/kotlin/_0lex/LexerRules.kt"))
    val kotlinFiles = scanFolderByExtension("src/main", "kt")
    val allUnits = parseFilesToUnits(kotlinFiles)
    // After lowering all files into a list 'allUnits'
    val tableResult = semanticCollectSymbols(allUnits)
    if (tableResult.isError()) {
        println("SYMBOL ERROR: ${tableResult.errorMessage}")
        return
    }
    val globalTable = tableResult.value!!

    // Now you have a list of all CompilationUnits for the C++ Generator
    println("\nTotal units ready for C++ generation: ${allUnits.size}")


    generateOutputCode(globalTable, allUnits)

}

private fun parseFilesToUnits(kotlinFiles: List<File>): MutableList<CompilationUnit> {
    val allUnits = mutableListOf<CompilationUnit>()

    for (file in kotlinFiles) {
        val unit = parseFileToCompilationUnit(file)
        allUnits.add(unit)
    }
    return allUnits
}

private fun parseFileToCompilationUnit(file: File): CompilationUnit {
    println("--- Lowering: ${file.name} ---")
    val content = readFileAsText(file.canonicalPath)
    val scanner = Scanner(content.toView())

    // 1. Structural Parse (Skeleton)
    val skeletonNodes = mutableListOf<SkeletonNode>()
    while (!scanner.isAtEnd()) {
        val res = parseNext(scanner)
        if (res.isError()) {
            println("SKIP: ${file.name} due to Lexer/Skeleton error: ${res.errorMessage}")
            break
        }
        skeletonNodes.add(res.value!!)
    }

    // 2. Semantic Lowering (The new logic)
    val unit = semanticAnalyze(skeletonNodes)
    return unit
}


fun printNode(node: SkeletonNode, indent: Int) {
    val prefix = "  ".repeat(indent)

    // Handle single tokens (Atoms)
    if (node.type == SkeletonType.ATOM) {
        val token = node.token!!
        println("$prefix${token.toDisplayString()}")
        return
    }

    var open = node.type.toString()
    var close = ""

    if (node.type == SkeletonType.PAREN) {
        open = "("
        close = ")"
    } else if (node.type == SkeletonType.BRACE) {
        open = "{"
        close = "}"
    } else if (node.type == SkeletonType.BRACKET) {
        open = "["
        close = "]"
    } else if (node.type == SkeletonType.CHEVRON) {
        open = "<"
        close = ">"
    }

    // Now use 'open' and 'close' for the logic below...
    if (close == "") {
        println(prefix + open + " {")
    } else {
        println(prefix + open)
    }


    // Print the Opening line
    if (close.isEmpty()) {
        println("$prefix$open {") // e.g., FUNCTION {
    } else {
        println("$prefix$open")    // e.g., (
    }

    // Print Children
    for (child in node.children) {
        printNode(child, indent + 1)
    }

    // Print the Closing line
    if (close.isEmpty()) {
        println("$prefix}")
    } else {
        println("$prefix$close")   // e.g., )
    }
}

