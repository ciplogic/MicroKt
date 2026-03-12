package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniType
import org.example.common.*

fun semanticExtractType(nodes: List<SkeletonNode>): MiniType {
    var childView = nodes.toListView()

    var lastNode = childView.last()
    var isNullable = false

    if (lastNode!!.type == SkeletonType.ATOM && lastNode.token?.value == "?") {
        isNullable = true
        childView = childView.slice(0, childView.len - 2)
    }
    var firstToken = childView.get(0).token
    if (firstToken!!.type != TokenType.IDENTIFIER) {
        firstToken = childView.get(1).token
    }
    val typeName = firstToken!!.value
    var generics = mutableListOf<MiniType>()
    if (childView.len > 1 && childView.get(1).type == SkeletonType.CHEVRON) {

        var genericChildView = childView.get(1).children.toListView()
        semanticExtractGenericParams(genericChildView, generics)
    }

    return MiniType(typeName, generics, isNullable)
}

fun semanticExtractGenericParams(
    genericChildView: ListView<SkeletonNode>,
    generics: MutableList<MiniType>
) {
    val indexOfComma = genericChildView.indexOfFirst { it.token?.value == "," }
    if (indexOfComma == -1) {
        generics.add(semanticExtractType(genericChildView.toList()))
        return
    }

    val leftSide = genericChildView.slice(0, indexOfComma)
    semanticExtractGenericParams(leftSide, generics)
    val rightSide = genericChildView.slice(indexOfComma + 1)
    semanticExtractGenericParams(rightSide, generics)
}

