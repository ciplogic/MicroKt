package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.ListView
import lt.microkot.common.get

fun getAtomValue(node: SkeletonNode): String {
    if (node.type != SkeletonType.Atom) {
        return ""
    }
    
    val t = node.token
    if (t == null) {
        return ""
    }
    
    if (t.value.trim() == "") {
        return ""
    }
    
    return t.value
}

fun getAtomValueOfList(list: List<SkeletonNode>, index: Int): String {
    if (index < 0 || index >= list.size) {
        return ""
    }
    return getAtomValue(list.get(index))
}

fun getAtomValueOfListView(view: ListView<SkeletonNode>, index: Int): String {
    if (index < 0 || index >= view.size) {
        return ""
    }
    return getAtomValue(view.get(index))
}
