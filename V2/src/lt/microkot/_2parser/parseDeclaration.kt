package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot.common.toListView
import lt.microkot.common.slice
import lt.microkot.common.get
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError

fun parseDeclaration(statement: SkeletonNode): TResult<Declaration?> {
    val modifiers = mutableListOf<String>()
    val children = statement.children.toListView() // This is our full list
    var i = 0

    // 1. Sweep Modifiers
    while (i < children.size) {
        val child = children.get(i)
        val value = getAtomValue(child)

        if (value == "") {
            i = i + 1
            continue
        }

        if (isModifier(value)) {
            modifiers.add(value)
            i = i + 1
        } else {
            // Stop at the first non-modifier (class, fun, etc.)
            break
        }
    }

    // 2. Create the "Slice" of the remaining nodes
    val remaining = children.slice(i)
    if (remaining.size == 0) {
        return success(null)
    }

    var keywordIdx = 0
    while (keywordIdx < remaining.size && getAtomValueOfListView(remaining, keywordIdx) == "") {
        keywordIdx++
    }
    if (keywordIdx >= remaining.size) {
        return success(null)
    }

    val keyword = getAtomValueOfListView(remaining, keywordIdx)
    val payloadSlice = remaining.slice(keywordIdx)
    val modifierList = ModifierList(modifiers)

    // 3. Dispatch using the Slice
    if (keyword == "class") {
        val res = parseClass(payloadSlice.slice(1), modifierList)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value as Declaration?)
    } else if (keyword == "fun") {
        val res = parseFunction(payloadSlice.slice(1), modifierList)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value as Declaration?)
    } else if (keyword == "val") {
        val res = parseProperty(payloadSlice.slice(1), modifierList, false)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value as Declaration?)
    } else if (keyword == "var") {
        val res = parseProperty(payloadSlice.slice(1), modifierList, true)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value as Declaration?)
    } else if (keyword == "typealias") {
        val res = parseTypeAlias(payloadSlice.slice(1), modifierList)
        if (res.isError()) {
            return res.asError()
        }
        return success(res.value as Declaration?)
    }

    return success(null)
}
