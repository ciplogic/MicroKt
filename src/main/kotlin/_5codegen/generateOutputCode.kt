package org.example._5codegen

import org.example._3midparse.CompilationUnit
import org.example._4globalsymbols.GlobalSymbolTable
import org.example.common.writeFileAsText


fun generateOutputCode(
    globalTable: GlobalSymbolTable,
    allUnits: MutableList<CompilationUnit>
) {
    // 2. Start building the amalgamated C++ file
    val finalCpp = StringBuilder()

    // Pass 1: Preamble & Forward Declares
    finalCpp.append(generateCPlusPlusPreamble(globalTable))

    // Pass 2: Type Aliases (Pre-resolved via table during lowering)
    for (unit in allUnits) {
        generateCPlusPlusTypeAliases(unit, finalCpp, globalTable)
    }

    // Pass 3: Enums
    generateCPlusPlusEnums(allUnits, finalCpp)

    // Pass 4: Struct Bodies (Now uses table to resolve field types)
    generateStructDefinitions(allUnits, finalCpp, globalTable)

    // Pass 5: Function Signatures (The "Header" part of functions)
    for (unit in allUnits) {
        generateFunctionSignatures(unit, finalCpp, globalTable)
    }

    // Pass 6: Function Bodies (The "Logic" part)
    generateAllFunctionBodies(allUnits, finalCpp, globalTable)

    // 3. Output to file
    println("--- Generation Complete ---")
    writeFileAsText("Output/main_amalgamated.cpp", finalCpp.toString())
}
