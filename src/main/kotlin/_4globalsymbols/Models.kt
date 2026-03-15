package org.example._4globalsymbols

import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniType

enum class SymbolType {
    Unknown,
    Enum,
    Function,
    Variable,
    Class,

}

class SymbolInfo(
    val symbolType: SymbolType,
    val name: MiniType,
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
