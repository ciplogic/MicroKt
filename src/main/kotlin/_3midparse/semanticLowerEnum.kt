package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example.common.toListView
import org.example.common.where

fun semanticLowerEnum(node: SkeletonNode): MiniEnum {
    val constants = mutableListOf<String>()

    // 1. Find the Name in the header
    // The header typically looks like: [ATOM(enum), ATOM(class), ATOM(Name)]

    val enumName =
        node.children.toListView().where({ it.type == SkeletonType.ATOM && it.token?.type == TokenType.IDENTIFIER })
            .get(0).token!!.value


    // 2. Find the Body (the BRACE node)
    for (child in node.children) {
        if (child.type == SkeletonType.BRACE) {
            // 3. Extract constants from the body
            // In a simple enum, constants are just IDENTIFIER atoms
            for (bodyChild in child.children) {
                if (bodyChild.type == SkeletonType.ATOM) {
                    val t = bodyChild.token!!
                    if (t.type == TokenType.IDENTIFIER) {
                        constants.add(t.value)
                    }
                }
            }
            break
        }
    }

    return MiniEnum(enumName, constants)
}

fun extractType(nodes: List<SkeletonNode>): MiniType {
    if (nodes.size == 1 && nodes[0].type == SkeletonType.ATOM) {
        return nodes[0].token!!.value.nameToMiniType()
    }

    var lastNode = nodes.last()
    var isNullable = false

    if (lastNode.type == SkeletonType.ATOM && lastNode.token?.value == "?") {
        isNullable = true
    }

    if (nodes.size >= 2 && nodes[0].type == SkeletonType.ATOM && nodes[1].type == SkeletonType.CHEVRON) {
        val typeName = nodes[0].token!!.value
        val generics = nodes[1].children.map { it.token!!.value.nameToMiniType() }
        return MiniType(typeName, generics, isNullable)
    }
    if (nodes.size == 2 && nodes[0].type == SkeletonType.ATOM) {
        val typeName = nodes[0].token!!.value
        return MiniType(typeName, listOf(), isNullable)
    }
    var name = ""
    var generics = mutableListOf<MiniType>()

    for (node in nodes) {
        val token = node.token
        val text = token?.value ?: ""

        when {
            // 1. Handle Generics: Recursively extract types from the CHEVRON's children
            node.type == SkeletonType.CHEVRON -> {
                // Split the children by commas to get individual type lists
                val groupedGenerics = splitByComma(node.children)
                for (group in groupedGenerics) {
                    generics.add(extractType(group))
                }
            }

            // 3. Handle Base Name (The ATOM that isn't a separator)
            node.type == SkeletonType.ATOM && text != ":" && text != "," -> {
                if (name.isEmpty()) name = text
            }
        }
    }

    return MiniType(name.ifEmpty { "Any" }, generics, isNullable)
}

/**
 * Helper to split a list of nodes by commas (e.g., [T, ",", K] -> [[T], [K]])
 */
private fun splitByComma(nodes: List<SkeletonNode>): List<List<SkeletonNode>> {
    val result = mutableListOf<List<SkeletonNode>>()
    var current = mutableListOf<SkeletonNode>()

    for (node in nodes) {
        if (node.token?.value == ",") {
            if (current.isNotEmpty()) result.add(current)
            current = mutableListOf()
        } else {
            current.add(node)
        }
    }
    if (current.isNotEmpty()) result.add(current)
    return result
}
