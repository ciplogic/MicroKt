package lt.microkot._3deepparser

import lt.microkot._1skelparser.SkeletonNode

sealed class DeepExpression

data class IntLiteral(val value: Int) : DeepExpression()
data class StringLiteral(val value: String) : DeepExpression()
data class StringTemplate(val components: List<DeepExpression>) : DeepExpression()
data class BooleanLiteral(val value: Boolean) : DeepExpression()
data class Identifier(val name: String) : DeepExpression()

data class BinaryExpr(
    val left: DeepExpression,
    val op: String,
    val right: DeepExpression
) : DeepExpression()

data class UnaryExpr(
    val op: String,
    val expr: DeepExpression
) : DeepExpression()

data class CallExpr(
    val target: DeepExpression,
    val arguments: List<DeepExpression>
) : DeepExpression()

data class DotExpr(
    val target: DeepExpression,
    val member: String
) : DeepExpression()

data class IndexExpr(
    val target: DeepExpression,
    val indices: List<DeepExpression>
) : DeepExpression()

data class BlockExpr(
    val expressions: List<DeepExpression>
) : DeepExpression()

data class IfExpr(
    val condition: DeepExpression,
    val thenBranch: DeepExpression,
    val elseBranch: DeepExpression?
) : DeepExpression()
