package org.example._5codegen

import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniEnum
import org.example._4globalsymbols.getDeclarations

fun generateCPlusPlusEnums(unit: MutableList<CompilationUnit>, sb: StringBuilder) {
    val enums = unit.getDeclarations<MiniEnum>()
    for (decl in enums) {
        sb.append("// Enum: ").append(decl.name).append("\n")
        sb.append("enum class ").append(decl.name).append(" : Int {\n")

        for (i in 0 until decl.constants.size) {
            sb.append("    ").append(decl.constants[i])
            if (i < decl.constants.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }

        sb.append("};\n\n")
    }
}
