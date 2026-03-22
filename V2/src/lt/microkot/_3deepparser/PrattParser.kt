package lt.microkot._3deepparser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot._0lex.TokenType
import lt.microkot._0lex.errorPosToText
import lt.microkot.common.ListView
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.toListView
import lt.microkot.common.get
import lt.microkot.common.toView
import lt.microkot._0lex.Scanner
import lt.microkot._1skelparser.parseFoldingNodes
import lt.microkot.common.asError
import lt.microkot.common.isError

/**
 * Global variable for error reporting in the Pratt parser.
 */
var originalSource: String = ""

fun setParserSource(source: String) {
    originalSource = source
}

fun getParserSource(): String = originalSource

enum class Precedence(val value: Int) {
    NONE(0),
    ASSIGNMENT(1),
    OR(2),
    AND(3),
    EQUALITY(4),   // == !=
    COMPARISON(5), // < > <= >=
    RANGE(6),
    TERM(7),       // + -
    FACTOR(8),     // * / %
    UNARY(9),      // ! - (prefix)
    POSTFIX(10);   // . ( [ (infix/suffix)

    companion object {
        // Helper to handle associativity by returning slightly lower precedence
        fun lower(p: Precedence): Int = p.value - 1
    }
}

/**
 * Plain state-holder for the Pratt expression parser.
 * All parsing logic lives in extension functions below.
 */
class ExpressionParser(val nodes: ListView<SkeletonNode>, val sourceOverride: String? = null) {
    var pos = 0
    val source: String? get() = sourceOverride ?: originalSource
}

// ---------------------------------------------------------------------------
// Extension functions — all parsing logic
// ---------------------------------------------------------------------------

fun ExpressionParser.getPositionText(p: Int): String {
    if (p < 0 || p >= nodes.size) {
        return "EOF"
    }
    val node = nodes.get(p)

    // Try the node's own token first, then dig into the first child that has one.
    var token = node.token
    if (token == null) {
        for (child in node.children) {
            if (child.token != null) {
                token = child.token
                break
            }
        }
    }
    if (token == null) {
        return "pos $p"
    }

    val s = source
    if (s == null) {
        return "pos ${token.position}"
    }
    return errorPosToText(s, token.position)
}

fun ExpressionParser.parseExpression(precedence: Precedence = Precedence.NONE): TResult<DeepExpression> {
    // Skip over end-of-line tokens before the expression starts.
    while (pos < nodes.size) {
        val currentToken = nodes.get(pos).token
        if (currentToken != null && currentToken.type == TokenType.EOLN) {
            pos++
        } else {
            break
        }
    }

    val prefixResult = parsePrefix()
    if (prefixResult == null) {
        val foundDescription: String
        if (pos < nodes.size) {
            foundDescription = nodes.get(pos).type.toString()
        } else {
            foundDescription = "EOF"
        }
        return error("Error ${getPositionText(pos)}: Expected expression, but found $foundDescription. Nodes: ${nodes}")
    }
    var left = prefixResult

    while (pos < nodes.size) {
        val nextNode = nodes.get(pos)
        val nextToken = nextNode.token
        if (nextToken != null && nextToken.type == TokenType.EOLN) {
            pos++
            continue
        }
        val nextPrec = getInfixPrecedence(nextNode)

        // In Pratt, if next token precedence is <= current, we stop and return what we have (left)
        if (precedence.value >= nextPrec.value) {
            break
        }

        // Consume and handle infix
        pos++
        val res = parseInfix(left!!, nextNode)
        if (res == null) {
            return error("Internal Error: Unrecognized infix operator ${nextNode.type} at ${getPositionText(pos-1)}")
        }
        if (res.isError()) {
            return res.asError()
        }
        left = res.value!!
    }

    return success(left!!)
}

fun ExpressionParser.parsePrefix(): DeepExpression? {
    // Skip over end-of-line tokens.
    while (pos < nodes.size) {
        val currentToken = nodes.get(pos).token
        if (currentToken != null && currentToken.type == TokenType.EOLN) {
            pos++
        } else {
            break
        }
    }

    if (pos >= nodes.size) {
        return null
    }
    val node = nodes.get(pos++)

    if (node.type == SkeletonType.Atom) {
        val token = node.token
        if (token == null) {
            return null
        }
        val value = token.value

        if (token.type == TokenType.NUMBER) {
            if (value.startsWith("0x")) {
                return IntLiteral(value.substring(2).toInt(16))
            } else {
                return IntLiteral(value.toInt())
            }
        } else if (token.type == TokenType.STRING) {
            return parseStringTemplate(value.removeSurrounding("\""))
        } else if (token.type == TokenType.KEYWORD) {
            if (value == "true") return BooleanLiteral(true)
            if (value == "false") return BooleanLiteral(false)
            if (value == "if") return parseIfExpression()
            if (value == "null") return Identifier("null")
            return Identifier(value)
        } else if (token.type == TokenType.IDENTIFIER) {
            return Identifier(value)
        } else if (token.type == TokenType.OPERATOR) {
            if (value == "-" || value == "!") {
                val operandRes = parseExpression(Precedence.UNARY)
                if (operandRes.isError()) {
                    return null
                }
                return UnaryExpr(value, operandRes.value!!)
            }
            return null
        } else {
            return null
        }
    } else if (node.type == SkeletonType.Parentheses) {
        val innerParser = ExpressionParser(node.children.toListView())
        val res = innerParser.parseExpression()
        if (res.isError()) {
            return null
        }
        return res.value!!
    } else if (node.type == SkeletonType.CurlyBraces) {
        return BlockExpr(emptyList())
    } else {
        return null
    }
}

fun ExpressionParser.parseInfix(left: DeepExpression, opNode: SkeletonNode): TResult<DeepExpression>? {
    if (opNode.type == SkeletonType.Atom) {
        val token = opNode.token
        if (token == null) {
            return null
        }
        val value = token.value

        if (value == ".") {
            if (pos >= nodes.size) {
                return TResult(null, "Error ${getPositionText(pos - 1)}: Expected member name after '.'")
            }
            val memberNode = nodes.get(pos++)
            val memberToken = memberNode.token
            if (memberToken == null) {
                return TResult(null, "Error ${getPositionText(pos - 1)}: Expected member name after '.'")
            }
            val memberPos: String
            val s = source
            if (s != null) {
                memberPos = errorPosToText(s, memberToken.position)
            } else {
                memberPos = "pos ${memberToken.position}"
            }
            if (memberToken.type != TokenType.IDENTIFIER) {
                return TResult(null, "Error $memberPos: Expected identifier after '.' but found ${memberToken.type}")
            }
            return success(DotExpr(left, memberToken.value))
        }

        if (value == "[") {
            val indexNodes = mutableListOf<SkeletonNode>()
            while (pos < nodes.size) {
                val n = nodes.get(pos++)
                val nToken = n.token
                if (n.type == SkeletonType.Atom && nToken != null && nToken.value == "]") {
                    break
                }
                indexNodes.add(n)
            }
            val splitNodes = splitByComma(indexNodes.toListView())
            val indices = mutableListOf<DeepExpression>()
            for (group in splitNodes) {
                val res = ExpressionParser(group.toListView(), source).parseExpression()
                if (res.isError()) {
                    return TResult(null, res.errorMessage)
                }
                indices.add(res.value!!)
            }
            return success(IndexExpr(left, indices))
        }

        val prec = getInfixPrecedence(opNode)
        // Right-associative operators get a slightly lower target so the right side is parsed first.
        val targetPrecValue: Int
        if (isRightAssociative(value)) {
            targetPrecValue = prec.value - 1
        } else {
            targetPrecValue = prec.value
        }

        val rightRes = parseExpressionInternal(targetPrecValue)
        if (rightRes.isError()) {
            return TResult(null, rightRes.errorMessage)
        }

        return success(BinaryExpr(left, value, rightRes.value!!))
    } else if (opNode.type == SkeletonType.Parentheses) {
        val args = mutableListOf<DeepExpression>()
        if (opNode.children.isNotEmpty()) {
            val splitNodes = splitByComma(opNode.children.toListView())
            for (group in splitNodes) {
                val res = ExpressionParser(group.toListView(), source).parseExpression()
                if (res.isError()) {
                    return TResult(null, res.errorMessage)
                }
                args.add(res.value!!)
            }
        }
        return success(CallExpr(left, args))
    } else {
        return null
    }
}

fun ExpressionParser.parseExpressionInternal(precedenceValue: Int): TResult<DeepExpression> {
    // Skip over end-of-line tokens.
    while (pos < nodes.size) {
        val currentToken = nodes.get(pos).token
        if (currentToken != null && currentToken.type == TokenType.EOLN) {
            pos++
        } else {
            break
        }
    }

    val prefixResult = parsePrefix()
    if (prefixResult == null) {
        return error("Error ${getPositionText(pos)}: Expected expression but found nothing. Nodes: ${nodes}")
    }
    var left = prefixResult

    while (pos < nodes.size) {
        val nextNode = nodes.get(pos)
        val nextToken = nextNode.token
        if (nextToken != null && nextToken.type == TokenType.EOLN) {
            pos++
            continue
        }
        val nextPrec = getInfixPrecedence(nextNode)

        if (precedenceValue >= nextPrec.value) {
            break
        }

        pos++
        val res = parseInfix(left!!, nextNode)
        if (res == null) {
            return error("Internal Error: Unrecognized infix operator ${nextNode.type} at ${getPositionText(pos-1)}")
        }
        if (res.isError()) {
            return res
        }
        left = res.value!!
    }

    return success(left!!)
}

fun ExpressionParser.getInfixPrecedence(node: SkeletonNode): Precedence {
    if (node.type == SkeletonType.Parentheses) {
        return Precedence.POSTFIX
    }
    if (node.type == SkeletonType.Atom) {
        val nodeToken = node.token
        val value: String
        if (nodeToken != null) {
            value = nodeToken.value
        } else {
            value = ""
        }

        if (value == "." || value == "[") return Precedence.POSTFIX
        if (value == "*" || value == "/" || value == "%") return Precedence.FACTOR
        if (value == "+" || value == "-") return Precedence.TERM
        if (value == ".." || value == "..<") return Precedence.RANGE
        if (value == "<" || value == ">" || value == "<=" || value == ">=") return Precedence.COMPARISON
        if (value == "==" || value == "!=" || value == "is" || value == "!is" || value == "as" || value == "!as") return Precedence.EQUALITY
        if (value == "&&") return Precedence.AND
        if (value == "||") return Precedence.OR
        if (value == "=") return Precedence.ASSIGNMENT

        if (value.endsWith("=")) return Precedence.ASSIGNMENT

        // Check if it's a potential infix function call (identifier as operator)
        if (nodeToken != null && nodeToken.type == TokenType.IDENTIFIER) {
            // Heuristic: if the next thing could be a prefix, treat this as infix function.
            // However, Pratt normally requires knowing which identifiers are infix.
            // In Kotlin, any function marked 'infix' can be used this way.
            // For now, let's treat any IDENTIFIER as NONE to avoid breaking things,
            // BUT the error message said "infix operator Atom", and identifiers are Atoms.
            return Precedence.NONE
        }

        return Precedence.NONE
    }
    return Precedence.NONE
}

fun ExpressionParser.isRightAssociative(op: String): Boolean {
    if (op == "=") {
        return true
    }
    if (op.endsWith("=") && op != "==" && op != "!=") {
        return true
    }
    return false
}

fun ExpressionParser.parseIfExpression(): DeepExpression? {
    if (pos >= nodes.size) {
        return null
    }
    val condNode = nodes.get(pos++)
    if (condNode.type != SkeletonType.Parentheses) {
        return null
    }

    val condParser = ExpressionParser(condNode.children.toListView(), source)
    val condRes = condParser.parseExpression()
    val cond = condRes.value
    if (cond == null) {
        return null
    }

    val thenRes = parseExpression(Precedence.NONE)
    val thenBranch = thenRes.value
    if (thenBranch == null) {
        return null
    }

    var elseBranch: DeepExpression? = null
    if (pos < nodes.size) {
        val next = nodes.get(pos)
        val nextToken = next.token
        if (next.type == SkeletonType.Atom && nextToken != null && nextToken.value == "else") {
            pos++
            val elseRes = parseExpression(Precedence.NONE)
            elseBranch = elseRes.value
        }
    }

    return IfExpr(cond, thenBranch, elseBranch)
}

fun ExpressionParser.parseStringTemplate(value: String): DeepExpression {
    if (!value.contains("$")) {
        return StringLiteral(value)
    }

    val components = mutableListOf<DeepExpression>()
    var lastIdx = 0
    var i = 0

    if (value.startsWith("$")) {
        components.add(StringLiteral(""))
    }

    while (i < value.length) {
        if (value[i] == '$' && i + 1 < value.length) {
            if (i > lastIdx) {
                components.add(StringLiteral(value.substring(lastIdx, i)))
            }

            i++ // skip $
            if (value[i] == '{') {
                i++
                val start = i
                var braceCount = 1
                while (i < value.length && braceCount > 0) {
                    if (value[i] == '{') braceCount++
                    else if (value[i] == '}') braceCount--

                    if (braceCount > 0) i++
                }
                val exprStr = value.substring(start, i)
                components.add(parseEmbeddedExpression(exprStr))
                i++ // skip '}'
            } else {
                val start = i
                while (i < value.length && (value[i].isLetterOrDigit() || value[i] == '_')) {
                    i++
                }
                val id = value.substring(start, i)
                components.add(Identifier(id))
            }
            lastIdx = i
        } else {
            i++
        }
    }

    if (lastIdx < value.length) {
        components.add(StringLiteral(value.substring(lastIdx)))
    }

    return StringTemplate(components)
}

fun ExpressionParser.parseEmbeddedExpression(text: String): DeepExpression {
    val scanner = Scanner(text.toView())
    val skelRes = parseFoldingNodes(scanner)
    if (skelRes.isError()) {
        return Identifier("ERROR_SKEL")
    }
    val skelNodes = skelRes.value!!.children.toListView()
    val exprRes = ExpressionParser(skelNodes, text).parseExpression()
    val exprValue = exprRes.value
    if (exprValue == null) {
        return Identifier("ERROR_PRATT")
    }
    return exprValue
}

fun ExpressionParser.splitByComma(view: ListView<SkeletonNode>): List<List<SkeletonNode>> {
    val result = mutableListOf<List<SkeletonNode>>()
    var current = mutableListOf<SkeletonNode>()
    for (i in 0 until view.size) {
        val node = view.get(i)
        val nodeToken = node.token
        val isComma = node.type == SkeletonType.Atom && nodeToken != null && nodeToken.value == ","
        if (isComma) {
            if (current.isNotEmpty()) {
                result.add(ArrayList(current))
            }
            current.clear()
        } else {
            current.add(node)
        }
    }
    if (current.isNotEmpty()) {
        result.add(current)
    }
    return result
}
