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

        val localSb = StringBuilder()
        // 1. Re-generate the signature (must match the header exactly)
        if (decl.name.generics.isNotEmpty()) {
            localSb.append("template <")
            localSb.append(decl.name.generics.joinToString(",") { "typename $it" })
            localSb.append(">\n")
        }

        val cppReturn = semanticResolveType(decl.returnType, table)
        localSb.append(cppReturn).append(" ").append(decl.name.name).append("(")

        var first = true
        if (decl.receiverType != null) {
            localSb.append(semanticResolveType(decl.receiverType, table)).append(" self")
            first = false
        }

        for (p in decl.params) {
            if (!first) localSb.append(", ")
            localSb.append(semanticResolveTypeFull(p.type, table)).append(" ").append(p.name)
            first = false
        }
        localSb.append(")")

        // 2. Generate the logic!
        generateFunctionBody(body, localSb, decl.receiverType != null, table)
        sb.append(localSb.toString())
    }
}