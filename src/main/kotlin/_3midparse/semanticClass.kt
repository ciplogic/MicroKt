package org.example._3midparse

import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._0lex.TokenType
import org.example._3midparse.models.MiniClass
import org.example._3midparse.models.MiniProperty
import org.example._3midparse.models.MiniType
import org.example.common.get
import org.example.common.indexOfFirst
import org.example.common.slice
import org.example.common.toList
import org.example.common.toListView

data class IntermediateSemanticParseHeader(
    val classType: MiniType,
    val isData: Boolean,
    val remainderNodes: List<SkeletonNode>
)

fun semanticLowerClass(node: SkeletonNode): MiniClass {

    val intermediate = intermediateSemanticParseHeader(node)

    val properties = semanticExtractClassProperties(intermediate.remainderNodes)
    return MiniClass(intermediate.classType, intermediate.isData, properties)
}

private fun intermediateSemanticParseHeader(node: SkeletonNode): IntermediateSemanticParseHeader {

    var childrenListView = node.children.toListView()
    val dataIndexOf = childrenListView.indexOfFirst({ it.type == SkeletonType.ATOM && it.token?.value == "data" })
    val isData = dataIndexOf != -1
    val classIndexOf = childrenListView.indexOfFirst({ it.type == SkeletonType.ATOM && it.token?.value == "class" })
    val className = childrenListView.get(classIndexOf + 1).token?.value ?: ""

    val chevronIndexOf = childrenListView.indexOfFirst({ it.type == SkeletonType.CHEVRON })
    val typeParams = mutableListOf<MiniType>()
    if (chevronIndexOf != -1) {
        semanticExtractTypeParams(childrenListView.get(chevronIndexOf), typeParams)
    }
    val classType = MiniType(className, typeParams, false)

    var indexToValidBody = classIndexOf + 2
    if (chevronIndexOf != -1) {
        indexToValidBody = chevronIndexOf + 1
    }

    val remainderNodes = childrenListView.slice(indexToValidBody)

    return IntermediateSemanticParseHeader(classType, isData, remainderNodes.toList())
}


private fun semanticExtractClassProperties(node: List<SkeletonNode>): MutableList<MiniProperty> {
    val childrenList = node.toList()
    val properties = mutableListOf<MiniProperty>()
    for (child in childrenList) {
        if (child.type == SkeletonType.PAREN) {
            semanticExtractProperties(child, properties)
        } else if (child.type == SkeletonType.BRACE) {
            semanticExtractProperties(child, properties)
        }
    }
    return properties
}

fun semanticExtractProperties(node: SkeletonNode, list: MutableList<MiniProperty>) {
    if (node.children.isEmpty()) {
        return
    }

    if (node.children.get(0).type == SkeletonType.ATOM) {
        val properties = foldLocalPropertiesFromAtoms(node.children)
        node.children.clear()
        node.children.addAll(properties)
    }
    var i = 0
    while (i < node.children.size) {
        val child = node.children[i]

        // 1. USE THE HELPER HERE for folded nodes (usually from class bodies)
        if (child.type == SkeletonType.PROPERTY) {
            val foldedProperty = extractFromFoldedProperty(child)
            list.add(foldedProperty)
            i++
            continue
        }

        // 2. Fallback to flat scan (usually from primary constructors)
        if (child.type == SkeletonType.ATOM) {
            val text = child.token!!.value
            if (text == "val" || text == "var") {
                // ... your flat scan logic ...
            }
        }
        i++
    }
}


fun semanticExtractTypeParams(node: SkeletonNode, list: MutableList<MiniType>) {
    // node is a SkeletonType.CHEVRON node
    for (child in node.children) {
        if (child.type == SkeletonType.ATOM) {
            val t = child.token!!
            // In a simple generic list <T, K>, we only care about the identifiers
            if (t.type == TokenType.IDENTIFIER) {
                list.add(t.value.nameToMiniType())
            }
        } else if (child.type == SkeletonType.CHEVRON) {
            // Support nested generics if needed: <T, List<K>>
            // For now, we can just recursively extract or treat as a single type
            semanticExtractTypeParams(child, list)
        }
    }
}
