package org.example._5codegen

import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniClass
import org.example._4globalsymbols.GlobalSymbolTable

fun generateCPlusPlusPreamble(table: GlobalSymbolTable): String {
    val sb = StringBuilder()
    sb.append("#include \"minikt.hpp\"\n\n")
    sb.append("// --- Forward Declarations ---\n")

    for (symbol in table.symbols) {
        // Enums in C++11+ can be forward declared if we specify the underlying type
        if (symbol.type == SkeletonType.ENUM) {
            sb.append("enum class ${symbol.name} : Int;\n")
            continue
        }
    }

    for (symbol in table.symbols) {
        if (symbol.type == SkeletonType.CLASS) {
            val miniClass = symbol.decl as MiniClass
            if (miniClass.isBuiltIn) continue

            // 1. Handle Templates: template <typename T>
            if (symbol.name.generics.isNotEmpty()) {
                sb.append("template <")
                for (i in 0..< symbol.name.generics.size) {
                    val templateItem = "typename ${symbol.name.generics.get(i)}"
                    sb.append(templateItem)
                    if (i < symbol.name.generics.size - 1) sb.append(",")
                }
                sb.append(">\n")
            }

            // 2. Declare the struct
            sb.append("struct ${symbol.name.name} ;\n")
        }
    }

    sb.append("\n// --- Type Aliases ---\n")
    return sb.toString()
}