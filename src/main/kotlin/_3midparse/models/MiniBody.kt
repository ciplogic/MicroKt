package org.example._3midparse.models

import org.example._2skeleton.SkeletonNode

data class MiniBody(val children: List<MiniBody>, val node: SkeletonNode?)
{
    override fun toString(): String {
        return node?.toString() ?: ""
    }
}

class MiniProperty(val name: String, val type: MiniType, val isVar: Boolean, val defaultInitialValue: String?) {
    override fun toString(): String {
        return "$name: $type"
    }
}

class MiniFunction(
    val name: MiniType,
    val receiverType: MiniType?,
    val params: List<MiniProperty>,
    val returnType: MiniType,
    val body: MiniBody?
)

class MiniClass(
    val name: MiniType,
    val isData: Boolean,
    val properties: List<MiniProperty>,
    val isBuiltIn: Boolean = false
) {
    override fun toString(): String {
        return name.toString()
    }
}

class MiniTypeAlias(
    val name: MiniType,
    val params: List<MiniProperty>,
    val returnType: MiniType
// e.g., "std::function<Int(Ref<StringView>)>"
)

data class MiniType(
    val name: String,
    val generics: List<MiniType> = emptyList(),
    val isNullable: Boolean = false
) {
    override fun toString(): String {
        val gen = if (generics.isEmpty()) "" else "<${generics.joinToString()}>"
        return "$name$gen${if (isNullable) "?" else ""}"
    }
}

class MiniEnum(val name: String, val constants: List<String>)

class CompilationUnit(
    var pkg: String,
    val imports: MutableList<String>,
    val declarations: MutableList<Any> // Using Any for now to hold Enum/Class/Function
)
