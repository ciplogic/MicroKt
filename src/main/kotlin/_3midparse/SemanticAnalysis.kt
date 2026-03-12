package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.CompilationUnit
import org.example._3midparse.models.MiniFunction
import org.example._3midparse.models.MiniProperty
import org.example._3midparse.models.MiniType
import org.example._4globalsymbols.GlobalSymbolTable
import org.example._4globalsymbols.semanticFindSymbol
import org.example.common.*

typealias SemanticAnalysisHandler = (skeletonNode: SkeletonNode, compilationUnit: CompilationUnit) -> Unit

data class CompilationUnitHandler(val skeletonType: SkeletonType, val handler: SemanticAnalysisHandler)

fun semanticAnalyze(nodes: List<SkeletonNode>): CompilationUnit {
    val unit = CompilationUnit("", mutableListOf<String>(), mutableListOf<Any>())

    for (node in nodes) {
        for (handler in rulesOfSemanticAnalysis) {
            if (node.type == handler.skeletonType) {
                handler.handler(node, unit)
                break
            }
        }
    }
    return unit
}


private val rulesOfSemanticAnalysis = arrayOf(
    CompilationUnitHandler(SkeletonType.PACKAGE, ::analyzePackage),
    CompilationUnitHandler(SkeletonType.IMPORT, ::analyzeImport),
    CompilationUnitHandler(SkeletonType.ENUM, ::analyzeEnum),
    CompilationUnitHandler(SkeletonType.TYPEALIAS, ::analyzeTypeAlias),
    CompilationUnitHandler(SkeletonType.CLASS, ::analyzeClass),
    CompilationUnitHandler(SkeletonType.FUNCTION, ::analyzeFunction),
)

fun analyzePackage(node: SkeletonNode, unit: CompilationUnit) {
    unit.pkg = semanticJoinAtoms(node)
}

fun analyzeImport(node: SkeletonNode, unit: CompilationUnit) {
    unit.imports.add(semanticJoinAtoms(node))
}

fun analyzeEnum(node: SkeletonNode, unit: CompilationUnit) {
    unit.declarations.add(semanticLowerEnum(node))
}

fun analyzeTypeAlias(node: SkeletonNode, unit: CompilationUnit) {
    unit.declarations.add(semanticTypeAlias(node))
}

fun analyzeClass(node: SkeletonNode, unit: CompilationUnit) {
    unit.declarations.add(semanticLowerClass(node))
}

fun analyzeFunction(node: SkeletonNode, unit: CompilationUnit) {
    unit.declarations.add(semanticLowerFunction(node))
}

fun isAtomIdentifier(node: SkeletonNode): Boolean {
    if (node.type != SkeletonType.ATOM) {
        return false
    }
    val t = node.token!!
    return t.type != TokenType.KEYWORD
}

fun semanticJoinAtoms(node: SkeletonNode): String {
    var result = StringBuilder()
    val atoms = node.children.toListView().where(::isAtomIdentifier)

    for (child in atoms) {
        val t = child.token!!
        result.append(t.value)

    }
    return result.toString()
}


fun semanticGetText(node: SkeletonNode): String {
    if (node.type == SkeletonType.ATOM) {
        return node.token?.value ?: ""
    }
    return "[${node.type}]"
}

fun extractFromFoldedProperty(node: SkeletonNode): MiniProperty {
    var name = ""
    var isVar = false

    var childView = node.children.toListView()
    val varToken = childView.get(0).token!!
    if (varToken.value == "var") {
        isVar = true
        childView = childView.slice(1)
    }
    if (varToken.value == "val") {
        isVar = false
        childView = childView.slice(1)
    }
    val nameToken = childView.get(0).token!!
    name = nameToken.value
    childView = childView.slice(2)

    val indexOfAssign = childView.indexOfFirst { it.token?.value == "=" }
    var assignString: String? = null
    if (indexOfAssign != -1) {
        val assignView = childView.slice(indexOfAssign + 1)
        assignString = assignView.last()?.token!!.value
        childView = childView.slice(0, indexOfAssign)
    }

    val typeList = childView.toList()

    val propertyType = semanticExtractType(typeList)

    return MiniProperty(name, propertyType, isVar, assignString)
}

fun foldLocalPropertiesFromAtoms(children: List<SkeletonNode>): List<SkeletonNode> {
    if (children.isEmpty()) return emptyList()

    val result = mutableListOf<SkeletonNode>()
    var pos = 0
    // Start with a clean container for the first property
    var currentProperty = SkeletonNode(SkeletonType.PROPERTY)

    while (pos < children.size) {
        val node = children[pos]
        val token = node.token

        if (token?.value == ",") {
            // Finish current and start new property
            result.add(processPropertyMetadata(currentProperty))
            currentProperty = SkeletonNode(SkeletonType.PROPERTY)
            pos++
            continue
        }
        if (token?.type == TokenType.EOLN) {
            pos++
            continue
        }

        currentProperty.children.add(node)
        pos++
    }

    result.add(processPropertyMetadata(currentProperty))
    return result
}

private fun processPropertyMetadata(prop: SkeletonNode): SkeletonNode {
    // 1. The first ATOM is usually the name (e.g., 'val x' -> 'x' or just 'x')
    // 2. Look for the ":" to find the type segment
    var typeStartIndex = -1

    for (i in prop.children.indices) {
        if (prop.children[i].token?.value == ":") {
            typeStartIndex = i + 1
            break
        }
    }

    if (typeStartIndex != -1) {
        // Collect tokens for MiniType until we hit "=" or end
        val typeNodes = mutableListOf<SkeletonNode>()
        for (i in typeStartIndex until prop.children.size) {
            if (prop.children[i].token?.value == "=") break
            typeNodes.add(prop.children[i])
        }

        // Use your new MiniType logic here
        // val mini = extractMiniTypeFromNodes(typeNodes)
        // prop.meta["type"] = mini
    }

    return prop
}


fun semanticLowerFunction(node: SkeletonNode): MiniFunction {
    val params = mutableListOf<MiniProperty>() // This was empty!
    var body: SkeletonNode? = null

    var funcType = inferFunctionType(node)
    if (funcType.name == "main") {
        funcType = MiniType("kMain")
    }

    val receiverType = inferReceiverOfFunction(node)
    val returnParsedType = inferReturnFunctionType(node)

    for (child in node.children) {
        if (child.type == SkeletonType.CHEVRON) {
        } else if (child.type == SkeletonType.PAREN) {
            // FIX: Extract (fileName: String) into the params list
            semanticExtractProperties(child, params)
        } else if (child.type == SkeletonType.BRACE) {
            body = child
        }
    }

    var semBody = semanticBody(body)
    return MiniFunction(funcType, receiverType, params, returnParsedType, semBody)
}


fun inferReturnFunctionType(node: SkeletonNode): MiniType {
    val children = node.children.toListView()
    val indexOfColon = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == ":" }
    if (indexOfColon == -1) {
        return MiniType("void")
    }
    var nodesAfterColon = children.slice(indexOfColon + 1)
    val indexOfBrace = nodesAfterColon.indexOfFirst { it.type == SkeletonType.BRACE }
    nodesAfterColon = nodesAfterColon.slice(0, indexOfBrace)
    val parsedType = semanticExtractType(nodesAfterColon.toList())
    return parsedType
}

fun inferReceiverOfFunction(node: SkeletonNode): MiniType? {
    val children = node.children.toListView()
    var indexOfDot = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == "." }
    if (indexOfDot == -1) {
        return null
    }
    val indexOfFunc = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == "fun" }

    var receiverTypeNodes = children.slice(indexOfFunc + 1)
    val indexOfChevron = children.indexOfFirst { it.type == SkeletonType.CHEVRON }
    val hasGenerics = indexOfChevron == indexOfFunc + 1
    if (hasGenerics) {
        receiverTypeNodes = receiverTypeNodes.slice(1)
    }

    indexOfDot = receiverTypeNodes.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == "." }
    receiverTypeNodes = receiverTypeNodes.slice(0, indexOfDot)
    val parsedType = semanticExtractType(receiverTypeNodes.toList())
    return parsedType
}

private fun inferFunctionType(node: SkeletonNode): MiniType {
    val children = node.children.toListView()
    val indexOfFunc = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token?.value == "fun" }

    val indexOfChevron = children.indexOfFirst { it.type == SkeletonType.CHEVRON }
    val hasGenerics = indexOfChevron == indexOfFunc + 1
    val indexOfParen = children.indexOfFirst { it.type == SkeletonType.PAREN }

    val functionExtractedName = children.get(indexOfParen - 1).token?.value!!

    var funcType = functionExtractedName.nameToMiniType()
    if (hasGenerics) {
        val listOfNodes = listOf<SkeletonNode>(node.children.get(indexOfParen - 1), node.children.get(indexOfChevron))
        funcType = semanticExtractType(listOfNodes)
    }
    return funcType
}


fun semanticResolveType(name: MiniType, table: GlobalSymbolTable): String {
    // 1. Check if it's a known class/enum
    val symbol = semanticFindSymbol(table.symbols, name)

    if (symbol != null) {
        // If it's a regular class (not data, not enum), it must be Ref-counted
        if (!symbol.isData && symbol.type == SkeletonType.CLASS) {
            return "Ref<" + name.name.nameToMiniType() + ">"
        }
    }

    // 2. Otherwise it's a primitive (Int, Boolean) or a Value Type (data class)
    return name.mapToFullName()
}

fun semanticResolveTypeFull(name: MiniType, table: GlobalSymbolTable): String {
    return semanticResolveType(name, table)
}
