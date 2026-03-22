package lt.microkot._0lex

import lt.microkot.common.*

class Scanner(val source: StringView) {
    var pos: Int = 0

    override fun toString(): String {
        return source.slice(pos).toString()
    }
}
