package org.example._4globalsymbols

import org.example._2skeleton.SkeletonType
import org.example._3midparse.mapToFullName
import org.example._3midparse.models.*
import org.example._3midparse.nameToMiniType
import org.example.common.*

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

fun GlobalSymbolTable.registerTypeByRef(typeName: MiniType, isData: Boolean) {
    this.symbols.add(SymbolInfo(typeName, isData, SkeletonType.CLASS, ""))
}

fun GlobalSymbolTable.registerType(typeName: String, isData: Boolean) {
    registerTypeByRef(typeName.nameToMiniType(), isData)
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

    shellSort(table.symbols, ::shouldSymbolsBeFlipped)

    return success(table)
}

typealias ShouldFlipOrderFunc = (SymbolInfo, SymbolInfo) -> Boolean

fun swapInList(list: MutableList<SymbolInfo>, index1: Int, index2: Int) {
    println("DEBUG: Swapped ${list[index1].name} and ${list[index2].name}");
    val temp = list[index1]
    list[index1] = list[index2]
    list[index2] = temp

}

fun shellSort(list: MutableList<SymbolInfo>, shouldFlipOrder: ShouldFlipOrderFunc) {

    var isRerored = true
    while (isRerored) {
        isRerored = false
        for (i in 0..<list.size) {
            for (j in i+1..<list.size)
            if (shouldFlipOrder(list[i], list[j])) {
                swapInList(list, i, j)
                isRerored = true
            }
        }
    }
}


fun shellSort2(list: MutableList<SymbolInfo>, sortyFunc: ShouldFlipOrderFunc) {
    var gap = list.size / 2
    while (gap > 0) {
        for (i in gap..<list.size) {
            val rightSideValue = list[i]
            var j = i - gap
            while (j >= 0) {
                var current = list[j]
                if (sortyFunc(current, rightSideValue)) {
                    swapInList(list, j, i)
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

fun getDependentTypesOfMiniClass(t: SymbolInfo): List<MiniType> {
    if (t.type == SkeletonType.CLASS) {
        val miniType = t.decl as MiniClass
        return getDependentTypesOfMiniClass(miniType)
    }
    return emptyList()
}

fun isTypeDependentOnOtherType(t1: SymbolInfo, typeToSearch: MiniType): Boolean {

    val dependentTypes = getDependentTypesOfMiniClass(t1)
    if (dependentTypes.isEmpty()) {
        return false
    }
    val view = dependentTypes.toListView()
    val indexOf = view.indexOfFirst { it.name == typeToSearch.name }
    return indexOf != -1

}

fun shouldSymbolsBeFlipped(t1: SymbolInfo, t2: SymbolInfo): Boolean {
    var areSameType = t1.type == t2.type
    val areNamesReordered = false;// t1.name.name > t2.name.name
    if (areSameType) {
        if (t1.type == SkeletonType.ENUM) {
            return areNamesReordered
        }
    }
    if (t1.type == SkeletonType.ENUM) {
        return false
    }
    if (t2.type == SkeletonType.ENUM) {
        return true
    }

    val getDependenciesOfT1 = getDependentTypesOfMiniClass(t1)
    val getDependenciesOfT2 = getDependentTypesOfMiniClass(t2)
    if (getDependenciesOfT1.isEmpty()) {
        return false
    }
    if (getDependenciesOfT2.isEmpty()) {
        return true
    }


    val isT1Dependent = isTypeDependentOnOtherType(t1, t2.name)
    val isT2Dependent = isTypeDependentOnOtherType(t2, t1.name)
    if (t1.type == SkeletonType.CLASS) {
        if (isT1Dependent) {
            return true
        }
    }
    if (t2.type == SkeletonType.CLASS) {
        if (isT2Dependent) {
            return false
        }
    }

    if (!isT1Dependent && !isT2Dependent) {
        return areNamesReordered
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
