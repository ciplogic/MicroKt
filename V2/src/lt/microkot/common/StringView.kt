package lt.microkot.common

data class StringView(val value: String, val start: Int, val length: Int) {
    override fun toString(): String {
        return value.substring(start, start + length)
    }
}

fun StringView.slice(start: Int, end: Int): StringView {
    return StringView(value, this.start + start, end - start)
}

fun StringView.slice(start: Int): StringView {
    return StringView(value, this.start + start, length - start)
}

fun StringView.get(index: Int): Char {
    return value.get(start + index)
}

fun StringView.isEmpty(): Boolean {
    return length == 0
}

fun StringView.isNotEmpty(): Boolean {
    return length > 0
}

fun StringView.startsWith(prefix: String): Boolean {
    if (prefix.length > length) {
        return false
    }
    for (i in prefix.indices) {
        if (value.get(start + i) != prefix.get(i)) {
            return false
        }
    }
    return true
}

fun String.toView(): StringView {
    return StringView(this, 0, this.length)
}
