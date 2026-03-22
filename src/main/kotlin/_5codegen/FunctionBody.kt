package org.example._5codegen

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniBody
import org.example._3midparse.semanticGetText
import org.example._4globalsymbols.GlobalSymbolTable

fun generateFunctionBody(body: MiniBody, sb: StringBuilder, isExtension: Boolean, table: GlobalSymbolTable) {
    // 1. A function body in Kotlin is usually a BRACE node
    val node = body.node!!
    generateFunctionBodyNode(node, sb, isExtension, table)
}

fun generateFunctionBodyNode(node: SkeletonNode, sb: StringBuilder, isExtension: Boolean, table: GlobalSymbolTable) {
    if (node.type == SkeletonType.CURLY) {
        sb.append(" {\n")
        for (child in node.children) {
            generateStatement(child, sb, isExtension, table)
        }
        sb.append("}\n")
    } else {
        // Handle expression bodies: = [expression]
        sb.append(" { return ")
        generateExpression(node, sb, isExtension, table)
        sb.append("; }\n")
    }
}

fun generateStatement(node: SkeletonNode, sb: StringBuilder, isExtension: Boolean, table: GlobalSymbolTable) {
    val type = node.type

    val localSb = StringBuilder()
    if (type == SkeletonType.PROPERTY) {
        generateLocalVar(node, localSb, isExtension, table)
    }
    else if (type == SkeletonType.ATOM) {
        // Handle loose atoms like return, break, or EOLN
        generateExpression(node, localSb, isExtension, table)
        if (node.token?.value == "\n") sb.append(";\n")
    } else {
        // Fallback for expression statements (function calls, etc.)
        generateExpression(node, localSb, isExtension, table)
        localSb.append(";\n")
    }
    val outText = localSb.toString()
    sb.append(outText)
}

fun generateLocalVar(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    // node is a PROPERTY: [val/var] [name] [:] [Type] [=] [Expression...]
    var isVar = false
    var name = ""
    var equalsIndex = -1

    // 1. Find 'var' vs 'val' and the identifier name
    for (i in 0 until node.children.size) {
        val child = node.children[i]
        val text = semanticGetText(child)

        if (text == "var") {
            isVar = true
        } else if (child.token?.type == TokenType.IDENTIFIER && name == "") {
            name = text
        } else if (text == "=") {
            equalsIndex = i
            break // The rest of the nodes are the expression
        }
    }

    // 2. C++ Mapping: const auto for val, auto for var
    if (!isVar) {
        sb.append("const ")
    }
    sb.append("auto ").append(name)

    // 3. Handle Initializer
    if (equalsIndex != -1) {
        sb.append(" = ")
        // Generate everything after the '=' as a C++ expression
        for (j in (equalsIndex + 1) until node.children.size) {
            val exprNode = node.children[j]
            // Skip trailing EOLN inside the property node
            if (exprNode.token?.type == TokenType.EOLN) continue

            generateExpression(exprNode, sb, isExt, table)
        }
    }

    sb.append(";\n")
}

fun generateExpression(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    sb.append(" ")
    if (node.type == SkeletonType.ATOM) {
        val t = node.token!!
        val text = t.value

        if (text == "this") {
            if (isExt) sb.append("self") else sb.append("this")
        } else if (text == "." || text == "!!." || text == "?.") {
            // In MiniKt, we default to '->' because most things are Ref types
            sb.append("->")
        } else if (text == "null") {
            sb.append("nullptr")
        } else if (text == "val") {
            sb.append("auto")
        } else if (text == "var") {
            sb.append("auto")
        } else if (text == "true") {
            sb.append("true")
        } else if (text == "false") {
            sb.append("false")
        } else {
            sb.append(text)
        }
    } else if (node.type == SkeletonType.PAREN) {
        sb.append("(")
        for (i in 0 until node.children.size) {
            generateExpression(node.children[i], sb, isExt, table)
        }
        sb.append(")")
    } else if (node.type == SkeletonType.CHEVRON) {
        sb.append("<")
        for (i in 0 until node.children.size) {
            generateExpression(node.children[i], sb, isExt, table)
        }
        sb.append(">")
    } else {
        // Fallback for other nodes like BRACE or PROPERTY
        for (child in node.children) {
            generateExpression(child, sb, isExt, table)
        }
    }
}

