package org.example._5codegen

import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniTypeAlias
import org.example._3midparse.mapToFullName
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.getDeclarations


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
        sb.append("using ").append(decl.name.name)
            .append(" = ");

        sb.append("Func<").append(decl.returnType.mapToFullName())
            .append("(")
            .append(decl.params.joinToString(", ") { it.type.mapToFullName() })
            .append(")")
            .append(">")

        //.append(decl.returnType.mapToFullName())
            sb.append(";\n")
    }
}
