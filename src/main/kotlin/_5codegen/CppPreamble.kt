package org.example._5codegen

import org.example._2skeleton.SkeletonType
import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniTypeAlias
import org.example._3midparse.mapToFullName
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.getDeclarations

fun generateCPlusPlusPreamble(table: GlobalSymbolTable): String {
    val sb = StringBuilder()
    sb.append("#include \"minikt.hpp\"\n\n")
    sb.append("// --- Forward Declarations ---\n")

    for (symbol in table.symbols) {
        // Enums in C++11+ can be forward declared if we specify the underlying type
        if (symbol.type == SkeletonType.ENUM) {
            sb.append("enum class ").append(symbol.name).append(" : Int;\n")
            continue
        }
    }

    for (symbol in table.symbols) {
        if (symbol.type == SkeletonType.CLASS) {
            // 1. Handle Templates: template <typename T>
            if (symbol.name.generics.isNotEmpty()) {
                sb.append("template <")
                for (i in 0 until symbol.name.generics.size) {
                    sb.append("typename ").append(symbol.name.generics[i])
                    if (i < symbol.name.generics.size - 1) sb.append(",")
                }
                sb.append(">\n")
            }

            // 2. Declare the struct
            sb.append("struct ").append(symbol.name.name).append(";\n")
        }
    }

    sb.append("\n// --- Type Aliases ---\n")
    return sb.toString()
}