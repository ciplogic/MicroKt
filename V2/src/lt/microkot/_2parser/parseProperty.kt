package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.ListView
import lt.microkot.common.get
import lt.microkot.common.slice
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot.common.isSuccess
import lt.microkot._3deepparser.DeepExpression as Expression
import lt.microkot._3deepparser.ExpressionParser
import lt.microkot._3deepparser.parseExpression
import lt.microkot.common.isEmpty

data class PropertyParts(
    val name: String,
    val type: DeclaredType?,
    val initializer: Expression?,
    val receiver: DeclaredType?
)

fun parseProperty(
    view: ListView<SkeletonNode>,
    modifiers: ModifierList,
    isVar: Boolean
): TResult<PropertyDeclaration> {
    val res = parsePropertyParts(view)
    if (res.isError()) {
        return res.asError()
    }
    val parts = res.value!!
    return success(PropertyDeclaration(parts.name, isVar, modifiers, parts.receiver, parts.type, parts.initializer))
}

fun parsePropertyParts(view: ListView<SkeletonNode>): TResult<PropertyParts> {
    var name = ""
    var type: DeclaredType? = null
    var initializer: Expression? = null
    var receiver: DeclaredType? = null

    var i = 0
    val size = view.size

    while (i < size) {
        val child = view.get(i)
        val value = getAtomValue(child)

        if (child.type == SkeletonType.Atom) {
            if (value == ":") {
                // The next part is the Type
                i = i + 1
                if (i < size) {
                    val parsedResult = parseType(view.slice(i))
                    if (parsedResult.isError()) {
                        return parsedResult.asError()
                    }
                    val parsed = parsedResult.value!!
                    type = parsed.type
                    i = i + parsed.consumed - 1
                }
            } else if (value == "=") {
                // Everything after '=' is the initializer expression
                i = i + 1
                if (i < size) {
                    val exprRes = parseExpression(view.slice(i))
                    if (exprRes.isError()) {
                        return exprRes.asError()
                    }
                    initializer = exprRes.value
                }
                break // Initializer is usually the end of the statement
            } else if (value == ".") {
                receiver = DeclaredType(name)
                name = "" // Reset name to catch the next atom
            } else {
                if (name == "" && value != "") {
                    name = value
                }
            }
        }
        i++
    }
    
    if (name == "") {
        return error("Property name not found.")
    }
    
    return success(PropertyParts(name, type, initializer, receiver))
}

/**
 * Parses a deep expression using the Pratt parser.
 */
fun parseExpression(view: ListView<SkeletonNode>): TResult<Expression> {
    if (view.isEmpty()) return error("No expression found.")
    val parser = ExpressionParser(view)
    return parser.parseExpression()
}