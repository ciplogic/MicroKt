package org.example._3midparse

import org.example._3midparse.models.MiniType


fun MiniType.mapToFullName(): String {
    val sb = StringBuilder()
    sb.append(name)
    if (generics.isNotEmpty()) {
        sb.append("<")
        for (i in generics.indices) {
            sb.append(generics[i].mapToFullName())
            if (i < generics.size - 1) {
                sb.append(",")
            }
        }
        sb.append(">")
    }
    return sb.toString()
}

fun String.nameToMiniType(): MiniType {
    return MiniType(this)
}

