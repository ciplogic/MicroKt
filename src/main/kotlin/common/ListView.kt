package org.example.common

typealias Predicate<T> = (T) -> Boolean

data class ListView<T>(val items: List<T>, val start: Int, val len: Int) {
    override fun toString(): String {
        val list = this.toList()
        if (list.isEmpty()) return ""
        val sb = StringBuilder()
        for (item in list) {
            sb.append(item)
        }
        return sb.toString()
    }

}

fun <T> ListView<T>.last(): T? {
    if (this.len == 0) {
        return null
    }
    return items[start + len - 1]
}

fun <T> ListView<T>.removeAllIf(function: Predicate<T>): ListView<T> {
    val resultItems = mutableListOf<T>()
    for (index in 0..<len) {
        val item = items.get(index)
        if (!function(item)) {
            resultItems.add(item)
        }
    }
    return ListView(resultItems, 0, resultItems.size)
}

fun <T> ListView<T>.indexOfFirst(function: Predicate<T>): Int {
    for (i in start..<start + len) {
        if (function(items[i])) {
            return i - start
        }
    }
    return -1
}

fun <T> ListView<T>.get(index: Int): T {
    return items[start + index]
}

fun <T> ListView<T>.toList(): MutableList<T> {
    val itemsList = items.subList(start, start + len)
    return itemsList.toMutableList()
}

fun <T> ListView<T>.where(predicate: Predicate<T>): List<T> {
    var result = mutableListOf<T>()
    for (item in toList()) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

fun <T> ListView<T>.slice(startFrom: Int, len: Int): ListView<T> {
    return ListView(items, this.start + startFrom, len)
}

fun <T> ListView<T>.slice(startFrom: Int): ListView<T> {
    return ListView(items, this.start + startFrom, len - startFrom)
}

fun <T> ListView<T>.isEmpty(): Boolean {
    return len == 0
}

fun <T> List<T>.toListView(): ListView<T> {
    return ListView(this, 0, this.size)
}

