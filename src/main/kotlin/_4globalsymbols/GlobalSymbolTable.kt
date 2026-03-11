package org.example._4globalsymbols

import org.example._2skeleton.SkeletonType
import org.example._3midparse.CompilationUnit
import org.example._3midparse.MiniClass
import org.example._3midparse.MiniEnum
import org.example._3midparse.MiniFunction
import org.example._3midparse.MiniType
import org.example._3midparse.mapToFullName
import org.example._3midparse.nameToMiniType
import org.example.common.TResult
import org.example.common.success

class SymbolInfo(
    val name: MiniType,
    val isData: Boolean,
    val type: SkeletonType
)

class GlobalSymbolTable(
    val symbols: MutableList<SymbolInfo> = mutableListOf(),
    val functions: MutableList<SymbolInfo> = mutableListOf()
)
fun semanticCollectSymbols(units: List<CompilationUnit>): TResult<GlobalSymbolTable> {
    val table = GlobalSymbolTable()

    // 1. Process Classes (Reference and Data)
    val classes: List<MiniClass> = units.getDeclarations<MiniClass>()
    for (decl in classes) {
        if (semanticFindSymbol(table.symbols, decl.name) != null) {
            return error("Duplicate class: ${decl.name}")
        }
        table.symbols.add(SymbolInfo(decl.name, decl.isData, SkeletonType.CLASS))
    }

    // 2. Process Enums
    val enums: List<MiniEnum> = units.getDeclarations<MiniEnum>()
    for (decl in enums) {
        if (semanticFindSymbol(table.symbols, decl.name.nameToMiniType()) != null) {
            return error("Duplicate enum: ${decl.name}")
        }
        table.symbols.add(SymbolInfo(decl.name.nameToMiniType(), true, SkeletonType.ENUM))
    }

    // 3. Process Functions (Signatures)
    val functions: List<MiniFunction> = units.getDeclarations<MiniFunction>()
    for (decl in functions) {
        table.functions.add(SymbolInfo(decl.name, false, SkeletonType.FUNCTION))
    }

    return success(table)
}


fun semanticFindSymbol(list: List<SymbolInfo>, name: MiniType): SymbolInfo? {
    for (s in list) {
        if (s.name.mapToFullName() == name.mapToFullName()) {
            return s
        }
    }
    return null
}


inline fun <reified T> List<CompilationUnit>.getDeclarations(): List<T> {
    val result = mutableListOf<T>()
    for (unit in this) {
        var declarations = unit.getDeclarations<T>()
        result.addAll(declarations)
    }
    return result
}

inline fun <reified T> CompilationUnit.getDeclarations(): List<T> {
    val result = mutableListOf<T>()

    for (decl in declarations) {
        if (decl is T) {
            result.add(decl)
        }
    }

    return result
}
