package org.example._5codegen

import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniClass
import org.example._3midparse.semanticResolveTypeFull
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.getDeclarations

fun generateStructDefinitions(unit: MutableList<CompilationUnit>, sb: StringBuilder, table: GlobalSymbolTable) {
    val classes = unit.getDeclarations<MiniClass>()
    for (decl in classes) {
        val localSb = StringBuilder()
        localSb.append("// Class: ").append(decl.name).append("\n")

        // 1. Templates
        if (decl.name.generics.isNotEmpty()) {
            val genericsJoinedText = decl.name.generics.joinToString(",") { "typename $it" }
            localSb.append("template <").append(genericsJoinedText).append(">\n")
        }

        // 2. Struct Declaration - NO KtObject for data classes
        localSb.append("struct ").append(decl.name.name)
        if (!decl.isData) {
            localSb.append(" : public KtObject")
        }
        localSb.append(" {\n")

        // 3. Properties: Type Name;
        generateCppStructProperty(decl, table, localSb)

        localSb.append("\n    ").append(decl.name).append("() = default;\n")
        localSb.append("};\n\n")

        sb.append(localSb)
    }
}

fun generateCppStructProperty(
    decl: MiniClass,
    table: GlobalSymbolTable,
    sb: StringBuilder
) {
    if (decl.properties.isEmpty()) {
        return
    }
    for (prop in decl.properties) {
        val cppType = semanticResolveTypeFull(prop.type, table)
        // FIX: Type comes FIRST in C++
        sb.append("    ").append(cppType).append(" ").append(prop.name).append(";\n")
    }
}
