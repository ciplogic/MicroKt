package org.example._3midparse

import org.example._0lex.TokenType
import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example.common.get
import org.example.common.toListView
import org.example.common.where

//
// typealias Predicate<T> = (T) -> Boolean

fun semanticTypeAlias(node: SkeletonNode): MiniTypeAlias {

    var childrenListView = node.children.toListView()
    // 1. Find the Name in the header
    // The header typically looks like: [ATOM(enum), ATOM(class), ATOM(Name)]
    val aliasName = childrenListView.get(1).token!!.value


    return MiniTypeAlias(aliasName.nameToMiniType(), "undefined")
}