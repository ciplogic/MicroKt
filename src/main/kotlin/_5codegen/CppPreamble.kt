package org.example._5codegen

import org.example._2skeleton.SkeletonType
import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniTypeAlias
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
                    if (i < symbol.name.generics.size - 1) sb.append(", ")
                }
                sb.append(">\n")
            }

            // 2. Declare the struct
            sb.append("struct ").append(symbol.name).append(";\n")
        }
    }

    sb.append("\n// --- Type Aliases ---\n")
    return sb.toString()
}

fun generateCPlusPlusTypeAliases(unit: CompilationUnit, sb: StringBuilder, table: GlobalSymbolTable) {
    val miniTypes = unit.getDeclarations<MiniTypeAlias>()

    for (decl in miniTypes) {
        // We only care about typealias declarations here
        // 1. Handle Template Header: template <typename T>
        if (decl.name.generics.isNotEmpty()) {
            sb.append("template <")
            var first = true
            for (param in decl.name.generics) {
                if (!first) sb.append(", ")
                sb.append("typename ").append(param)
                first = false
            }
            sb.append(">\n")
        }

        // 2. Generate the C++ using statement
        // using Name = TargetType;
        sb.append("using ").append(decl.name)
            .append(" = ").append(decl.targetType)
            .append(";\n")
    }
}
