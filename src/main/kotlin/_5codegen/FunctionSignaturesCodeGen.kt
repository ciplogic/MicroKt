package org.example._5codegen

import org.example._3midparse.models.MiniFunction
import org.example._3midparse.semanticResolveType
import org.example._3midparse.semanticResolveTypeFull
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.getTableFunctions

fun generateFunctionSignatures(sb: StringBuilder, table: GlobalSymbolTable) {

    sb.append("// --- Function Signatures ---\n\n")
    // 1. Use the generic extractor for a clean, typed list
    val functions: List<MiniFunction> = table.getTableFunctions()

    for (decl in functions) {
        generatePerFunctionHeader(decl, sb, table)
    }
}

private fun generatePerFunctionHeader(
    decl: MiniFunction,
    sb: StringBuilder,
    table: GlobalSymbolTable
) {
    // 1. Template Header: template <typename T>
    generateTemplateHeaderForFunction(decl, sb)

    // 2. Return Type & Name
    val cppReturn = semanticResolveType(decl.returnType, table)
    sb.append(cppReturn).append(" ").append(decl.name.name).append("(")

    // 3. Parameters
    var firstParam = true

    // Handle Extension Receiver
    if (decl.receiverType != null) {
        val receiverCppType = semanticResolveType(decl.receiverType, table)
        sb.append(receiverCppType).append(" self")
        firstParam = false
    }

    // Regular Parameters: Type Name
    for (prop in decl.params) {
        if (!firstParam) sb.append(", ")
        val paramCppType = semanticResolveTypeFull(prop.type, table)
        sb.append(paramCppType).append(" ").append(prop.name)
        firstParam = false
    }

    sb.append(");\n\n")
}

private fun generateTemplateHeaderForFunction(decl: MiniFunction, sb: StringBuilder) {
    if (decl.name.generics.isEmpty()) {
        return
    }
    sb.append("template <")
    var first = true
    for (param in decl.name.generics) {
        if (!first) sb.append(", ")
        sb.append("typename ").append(param)
        first = false
    }
    sb.append(">\n")
}