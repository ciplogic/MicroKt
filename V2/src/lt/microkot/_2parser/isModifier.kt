package lt.microkot._2parser

private val MODIFIER_KEYWORDS = listOf(
    "public", "private", "internal",
    "data", "sealed", "open",
    "expect", "actual", "annotation"
)

fun isModifier(value: String): Boolean {
    return MODIFIER_KEYWORDS.contains(value)
}
