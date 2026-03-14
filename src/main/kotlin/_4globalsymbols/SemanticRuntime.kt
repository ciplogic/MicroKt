package org.example._4globalsymbols

import org.example._3midparse.models.*
import org.example._3midparse.nameToMiniType

fun MutableList<Any>.addBuiltInType(type: MiniType) {
    this.add(
        MiniClass(
            type,
            isData = true,
            properties = emptyList(),
            isBuiltIn = true
        )
    )
}

fun MutableList<Any>.addBuiltInTypeName(typeName: String) {
    this.addBuiltInType(typeName.nameToMiniType())
}

fun createRuntimeUnit(): CompilationUnit {
    val declaration = mutableListOf<Any>()
    // print(text: String)
    declaration.add(
        MiniFunction(
            name = MiniType("print"),
            receiverType = null,
            params = listOf(
                MiniProperty(
                    isVar = false,
                    name = "text",
                    type = MiniType("String"),
                    defaultInitialValue = null
                )
            ),
            returnType = MiniType("Unit"),
            body = null // No Kotlin body; it's a C++ primitive
        )
    )

    // File.readText(): String
    declaration.add(
        MiniFunction(
            name = MiniType("readText"),
            receiverType = MiniType("File"),
            params = emptyList(),
            returnType = MiniType("String"),
            body = null
        )
    )
    declaration.addBuiltInTypeName("File")
    declaration.addBuiltInTypeName("Int")
    declaration.addBuiltInTypeName("Boolean")
    declaration.addBuiltInTypeName("String")

    val listOfT = MiniType("List", listOf(MiniType("T")))

    declaration.addBuiltInType(listOfT)
    return CompilationUnit(pkg = "rtl", imports = mutableListOf(), declarations = declaration)
}
