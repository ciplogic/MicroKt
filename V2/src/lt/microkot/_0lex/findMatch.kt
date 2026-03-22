package lt.microkot._0lex

import lt.microkot.common.StringView

/**
 * Iterates through custom MatchPairs to find a valid token boundary.
 */
fun findMatch(view: StringView): MatchResult? {
    for (mp in lexingRules) {
        val length = mp.rule(view)
        if (length > 0) {
            return MatchResult(mp.type, length)
        }
    }
    return null
}
