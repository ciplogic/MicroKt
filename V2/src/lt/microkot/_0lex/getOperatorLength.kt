package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get

fun getOperatorLength(view: StringView): Int {
    val c = view.get(0)

    // 1. Keep ONLY structural delimiters that are never part of a multi-char op
    val structural = "()[]{};,"
    if (structural.contains(c)) {
        return 1
    }

    // 2. Everything else that can be combined (like >, =, !, +, -, etc.)
    val multiOps = "+-*/%&|^~.<>=!:?"
    if (!multiOps.contains(c)) {
        return 0
    }

    var opLen = 0
    while (opLen < view.length && multiOps.contains(view.get(opLen))) {
        opLen++

        // Safety: Kotlin doesn't usually have 3-char operators like '>=='
        // unless you're doing something very specific.
        // For MiniKotlin, 2 is usually enough for >=, <=, ==, !=, &&, ||
        if (opLen == 2) {
            break
        }
    }

    if (opLen > 0) {
        return opLen
    } else {
        return 1
    }
}
