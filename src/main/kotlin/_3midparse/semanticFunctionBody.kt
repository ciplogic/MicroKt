package org.example._3midparse

import org.example._2skeleton.SkeletonNode
import org.example._2skeleton.SkeletonType
import org.example._3midparse.models.MiniBody


fun semanticBody(body: SkeletonNode?): MiniBody {
    val childrenEmpty = arrayListOf<MiniBody>()
    if (body == null) {
        return MiniBody(childrenEmpty, null)
    }
    if (body.type == SkeletonType.CURLY) {
        val children = body.children
        val semChildren = children.map { semanticBody(it) }
        return MiniBody(semChildren, body)
    }
    return MiniBody(childrenEmpty, body)
}