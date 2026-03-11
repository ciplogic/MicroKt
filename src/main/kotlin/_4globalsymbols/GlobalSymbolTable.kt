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
import org.example.common.asError
import org.example.common.indexOfFirst
import org.example.common.isError
import org.example.common.success
import org.example.common.toListView

class SymbolInfo(
    val name: MiniType,
    val isData: Boolean,
    val type: SkeletonType,
    val decl: Any
) {
    override fun toString(): String {
        return name.toString()
    }
}

class GlobalSymbolTable(
    val symbols: MutableList<SymbolInfo> = mutableListOf(),
    val functions: MutableList<SymbolInfo> = mutableListOf()
)


inline fun <reified T> GlobalSymbolTable.getTableDeclarations(): List<T> {
    val result = mutableListOf<T>()

    for (decl in symbols) {
        if (decl.decl is T) {
            result.add(decl.decl as T)
        }
    }

    return result
}

fun GlobalSymbolTable.getTableFunctions(): List<MiniFunction> {
    val result = mutableListOf<MiniFunction>()

    for (decl in functions) {
        if (decl.decl is MiniFunction) {
            result.add(decl.decl)
        }
    }

    return result
}

fun semanticCollectSymbols(units: List<CompilationUnit>): TResult<GlobalSymbolTable> {
    val table = GlobalSymbolTable()

    val resultExtraction = extractTypeDeclarations(units, table)
    if (resultExtraction.isError()) {
        return resultExtraction.asError()
    }

    // 3. Process Functions (Signatures)
    val functions: List<MiniFunction> = units.getDeclarations<MiniFunction>()
    for (decl in functions) {
        table.functions.add(SymbolInfo(decl.name, false, SkeletonType.FUNCTION, decl))
    }

    return success(table)
}

private fun extractTypeDeclarations(
    units: List<CompilationUnit>,
    table: GlobalSymbolTable
): TResult<GlobalSymbolTable> {
    // 1. Process Classes (Reference and Data)
    val classes: List<MiniClass> = units.getDeclarations<MiniClass>()
    for (decl in classes) {
        if (semanticFindSymbol(table.symbols, decl.name) != null) {
            return error("Duplicate class: ${decl.name}")
        }
        table.symbols.add(SymbolInfo(decl.name, decl.isData, SkeletonType.CLASS, decl))
    }

    // 2. Process Enums
    val enums: List<MiniEnum> = units.getDeclarations<MiniEnum>()
    for (decl in enums) {
        if (semanticFindSymbol(table.symbols, decl.name.nameToMiniType()) != null) {
            return error("Duplicate enum: ${decl.name}")
        }
        table.symbols.add(SymbolInfo(decl.name.nameToMiniType(), true, SkeletonType.ENUM, decl))
    }

    shellSort(table.symbols, ::areTypesOrdered)

    return success(table)
}

typealias SortyFunc = (SymbolInfo, SymbolInfo) -> Boolean

fun shellSort(list: MutableList<SymbolInfo>, sortyFunc: SortyFunc) {
    var gap = list.size / 2
    while (gap > 0) {
        for (i in gap..<list.size) {
            val rightSideValue = list[i]
            var j = i - gap
            while (j >= 0) {
                var current = list[j]
                if (sortyFunc(current, rightSideValue)) {
                    list[i] = current
                    list[j] = rightSideValue
                }
                j -= gap
            }
        }
        gap /= 2
    }
}


fun getDependentTypesOfMiniClass(decl: MiniClass): List<MiniType> {
    return decl.properties.map { it.type }
}

fun isTypeDependentOnOtherType(t1: SymbolInfo, typeToSearch: MiniType): Boolean {
    if (t1.type == SkeletonType.CLASS) {
        val miniType = t1.decl as MiniClass
        val dependentTypes = getDependentTypesOfMiniClass(miniType)
        if (dependentTypes.isEmpty()) {
            return false
        }
        val view = dependentTypes.toListView()
        val indexOf = view.indexOfFirst { it.name == typeToSearch.name }
        if (indexOf == -1) {
            return false
        }
        return true
    }

    return false
}

fun areTypesOrdered(t1: SymbolInfo, t2: SymbolInfo): Boolean {
    if (t1.type == SkeletonType.ENUM) {
        return false
    }
    if (t2.type == SkeletonType.ENUM) {
        return true
    }
    if (t1.type == SkeletonType.CLASS) {
        if (isTypeDependentOnOtherType(t1, t2.name)) {
            return true
        }
    }
    if (t2.type == SkeletonType.CLASS) {
        if (isTypeDependentOnOtherType(t2, t1.name)) {
            return false
        }
    }

    return false
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
