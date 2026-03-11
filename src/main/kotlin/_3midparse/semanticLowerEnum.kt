package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example.common.get
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
