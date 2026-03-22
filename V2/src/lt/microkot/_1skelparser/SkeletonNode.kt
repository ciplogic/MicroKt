package lt.microkot._1skelparser

import lt.microkot._0lex.Token

class SkeletonNode(
    val type: SkeletonType,
    val token: Token? = null,
    val children: MutableList<SkeletonNode> = mutableListOf(),
    val position: Int = 0
) {
    override fun toString(): String {
        val sb = StringBuilder()
        if (this.token != null) {
            sb.append(this.token.toString())
        }
        for (child in children) {
            sb.append(child.toString()).append(" ")
        }
        return sb.toString()
    }
}