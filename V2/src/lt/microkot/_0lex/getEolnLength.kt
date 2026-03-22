package lt.microkot._0lex

import lt.microkot.common.StringView
import lt.microkot.common.get

fun getEolnLength(view: StringView): Int {
    var firstChar = view.get(0)
    if (firstChar != '\n' && firstChar != '\r') {
        return 0
    }
    val textOfChars = "\r\n\t "
    var len = 0
    while (len < view.length) {
        val charItem = view.get(len)
        if (!textOfChars.contains(charItem)) {
            break
        }
        len++
    }

    return len
}
