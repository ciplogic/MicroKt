package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType


fun semanticExtractTypeParams(node: SkeletonNode, list: MutableList<String>) {
    // node is a SkeletonType.CHEVRON node
    for (child in node.children) {
        if (child.type == SkeletonType.ATOM) {
            val t = child.token!!
            // In a simple generic list <T, K>, we only care about the identifiers
            if (t.type == TokenType.IDENTIFIER) {
                list.add(t.value)
            }
        } else if (child.type == SkeletonType.CHEVRON) {
            // Support nested generics if needed: <T, List<K>>
            // For now, we can just recursively extract or treat as a single type
            semanticExtractTypeParams(child, list)
        }
    }
}
