package org.example._5codegen

import org.example._3midparse.semanticResolveType
import org.example._3midparse.semanticResolveTypeFull
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.getTableFunctions

fun generateAllFunctionBodies(sb: StringBuilder, table: GlobalSymbolTable) {
    sb.append("// --- Function Implementations ---\n\n")

    var functions = table.getTableFunctions()
    for (decl in functions) {
        // Skip if there is no body (abstract/interface methods)
        val body = decl.body ?: continue

        // 1. Re-generate the signature (must match the header exactly)
        if (decl.name.generics.isNotEmpty()) {
            sb.append("template <")
            sb.append(decl.name.generics.joinToString(",") { "typename $it" })
            sb.append(">\n")
        }

        val cppReturn = semanticResolveType(decl.returnType, table)
        sb.append(cppReturn).append(" ").append(decl.name).append("(")

        var first = true
        if (decl.receiverType != null) {
            sb.append(semanticResolveType(decl.receiverType, table)).append(" self")
            first = false
        }

        for (p in decl.params) {
            if (!first) sb.append(", ")
            sb.append(semanticResolveTypeFull(p.type, table)).append(" ").append(p.name)
            first = false
        }
        sb.append(")")

        // 2. Generate the logic!
        generateFunctionBody(body, sb, decl.receiverType != null, table)
    }
}