package org.example._3midparse

import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniProperty
import org.example._3midparse.models.MiniType
import org.example._3midparse.models.MiniTypeAlias
import org.example.common.ListView
import org.example.common.get
import org.example.common.indexOfFirst
import org.example.common.slice
import org.example.common.toList
import org.example.common.toListView

// We support type aliases just to map lambda functions. Parameters have default name: "it", "it2", and so on when not specified
// typealias Predicate<T> = (T) -> Boolean
//
fun semanticTypeAlias(node: SkeletonNode): MiniTypeAlias {
    var childrenListView = node.children.toListView()
    // 1. Find the Name in the header
    // The header typically looks like: [ATOM(enum), ATOM(class), ATOM(Name)]
    val aliasName = childrenListView.get(1).token!!.value
    val hasGenerics = childrenListView.get(2).type == SkeletonType.CHEVRON
    val genericsTypes = mutableListOf<MiniType>()

    var miniType = MiniType(aliasName, genericsTypes, false)

    if (hasGenerics) {
        val interestingList = childrenListView.slice(1, 2).toList()
        miniType = semanticExtractType(interestingList)
        childrenListView = childrenListView.slice(4)
    } else {
        childrenListView = childrenListView.slice(3)
    }

    var returnType = extractTypeAliasReturnType(childrenListView)
    val parameters = mutableListOf<MiniProperty>()
    val parenNodeIndexOf = childrenListView.indexOfFirst { it.type == SkeletonType.PAREN }
    if (parenNodeIndexOf != -1) {
        val parenNode = childrenListView.get(parenNodeIndexOf)
        extractParametersInTypeAlias(parenNode.children.toListView(), parameters)

    }


    return MiniTypeAlias(miniType,  parameters, returnType)
}

fun extractParametersInTypeAlias(
    children: ListView<SkeletonNode>,
    parameters: MutableList<MiniProperty>
) {
    if (children.len == 0) {
        return
    }

    val indexOfComma = children.indexOfFirst { it.type == SkeletonType.ATOM && it.token!!.value == "," }
    if (indexOfComma == -1) {
        parseIndividualParameter(children, parameters)
        return
    }

    var currentParameterView = children.slice(0, indexOfComma)
    parseIndividualParameter(currentParameterView, parameters)
    val mutableChildren = children.slice(indexOfComma + 1)
    extractParametersInTypeAlias(mutableChildren, parameters)
}

fun parseIndividualParameter(
    toList: ListView<SkeletonNode>,
    parameters: MutableList<MiniProperty>
) {
    val indexOfColon = toList.indexOfFirst { it.type == SkeletonType.ATOM && it.token!!.value == ":" }
    if (indexOfColon == -1) {
        val parsedType = semanticExtractType(toList.toList())
        var propertyName = "it"
        if (parameters.size > 0) {
            propertyName = "it${parameters.size}"
        }
        var property = MiniProperty(propertyName, parsedType, false, null)
        parameters.add(property)
        return
    }
    val typeNodes = toList.slice(indexOfColon + 1).toList()
    val propertyName = toList.get(0).token!!.value
    val parsedType = semanticExtractType(typeNodes)
    val property = MiniProperty(propertyName, parsedType, false, null)
    parameters.add(property)
}

private fun extractTypeAliasReturnType(childrenListView: ListView<SkeletonNode>): MiniType {
    var returnType = MiniType("void", emptyList(), false)

    val indexOfArrow = childrenListView.indexOfFirst { it.type == SkeletonType.ATOM && it.token!!.value == "->" }
    if (indexOfArrow != -1) {
        val arrowView = childrenListView.slice(indexOfArrow + 1)
        val returnTypeParsed = semanticExtractType(arrowView.toList())
        returnType = returnTypeParsed
    }
    return returnType
}