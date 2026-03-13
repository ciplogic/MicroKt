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
    if (node.type == SkeletonType.BRACE) {
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
    if (type == SkeletonType.PROPERTY) generateLocalVar(node, localSb, isExtension, table)
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

fun generateIf(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    // node.children[0] is the 'if' keyword (ATOM)
    sb.append("if ")

    // 1. The Condition: Usually the first PAREN node
    val condNode = node.children.find { it.type == SkeletonType.PAREN }
    if (condNode != null) {
        generateExpression(condNode, sb, isExt, table)
    }

    // 2. The Then-Branch: The node immediately following the PAREN
    // It could be a BRACE (block) or an ATOM/CONSTRUCT (single statement)
    val thenIndex = node.children.indexOf(condNode) + 1
    if (thenIndex < node.children.size) {
        val thenNode = node.children[thenIndex]
        generateFunctionBodyNode(thenNode, sb, isExt, table)
    }

    // 3. Optional Else-Branch
    val elseIndex = findElseIndex(node)
    if (elseIndex != -1 && elseIndex + 1 < node.children.size) {
        sb.append(" else ")
        val elseNode = node.children[elseIndex + 1]
        generateFunctionBodyNode(elseNode, sb, isExt, table)
    }
}

private fun generateBranch(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    if (node.type == SkeletonType.BRACE) {
        // It's a block { ... }
        generateFunctionBodyNode(node, sb, isExt, table)
    } else {
        // It's a single statement/expression
        sb.append(" ")
        generateExpression(node, sb, isExt, table)
        // If we are in a statement context, we'd need a semicolon here
    }
}

private fun findElseIndex(node: SkeletonNode): Int {
    for (i in 0 until node.children.size) {
        if (node.children[i].token?.value == "else") return i
    }
    return -1
}

fun generateLocalProperty(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    // node is a PROPERTY construct: val name: Type = expression
    var isVar = false
    var name = ""
    "auto"
    var hasInitializer = false

    for (child in node.children) {
        val text = semanticGetText(child)
        if (text == "var") isVar = true
        else if (text == "val") isVar = false
        else if (child.token?.type == TokenType.IDENTIFIER && name == "") {
            name = text
        } else if (text == ":") {
            // Future: extract explicit type if needed
        } else if (text == "=") {
            hasInitializer = true
            break // Expression starts after '='
        }
    }

    // C++ Output: [const] auto name = ...
    if (!isVar) sb.append("const ")
    sb.append("auto ").append(name)

    if (hasInitializer) {
        sb.append(" = ")
        // Find the index of '=' and generate the rest as an expression
        val equalsIndex = node.children.indexOfFirst { it.token?.value == "=" }
        for (j in (equalsIndex + 1) until node.children.size) {
            generateExpression(node.children[j], sb, isExt, table)
        }
    }
    sb.append(";\n")
}

fun generateWhile(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    sb.append("while ")

    // 1. Condition: ( ... )
    val condNode = node.children.find { it.type == SkeletonType.PAREN }
    if (condNode != null) {
        generateExpression(condNode, sb, isExt, table)
    }

    // 2. Body: { block } or single statement
    val bodyIndex = node.children.indexOf(condNode) + 1
    if (bodyIndex < node.children.size) {
        val bodyNode = node.children[bodyIndex]
        generateBranch(bodyNode, sb, isExt, table)
    }
    sb.append("\n")
}

fun generateFor(node: SkeletonNode, sb: StringBuilder, isExt: Boolean, table: GlobalSymbolTable) {
    sb.append("for (")

    // The first child is usually the keyword 'for' (ATOM), skip it
    // The second child is the PAREN group (item in collection)
    val header = node.children.find { it.type == SkeletonType.PAREN }
    if (header != null) {
        // We want: auto item : collection
        var foundIn = false
        for (hChild in header.children) {
            val text = hChild.token?.value ?: ""
            if (text == "(" || text == ")") continue

            if (text == "in") {
                sb.append(" : ")
                foundIn = true
            } else if (!foundIn && hChild.token?.type == TokenType.IDENTIFIER) {
                sb.append("auto ").append(text)
            } else {
                generateExpression(hChild, sb, isExt, table)
            }
        }
    }
    sb.append(")")

    // The last child is the body
    generateFunctionBodyNode(node.children.last(), sb, isExt, table)
}
