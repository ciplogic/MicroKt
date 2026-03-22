package lt.microkot._2parser

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot._1skelparser.splitNodeIntoStatements
import lt.microkot.common.ListView
import lt.microkot.common.get
import lt.microkot.common.toListView
import lt.microkot.common.isSuccess
import lt.microkot._3deepparser.DeepExpression as Expression

fun parsePackage(node: SkeletonNode): String {
    var result = StringBuilder()
    var j = 1 // Skip 'package'
    while (j < node.children.size) {
        val child = node.children.get(j)
        if (child.type == SkeletonType.Atom) {
            result.append(child.token!!.value)
        }
        j = j + 1
    }
    return result.toString()
}

fun parseImport(statement: SkeletonNode): Import {
    var path = ""
    var alias: String? = null
    var i = 1 // Start at 1 to skip the "import" keyword

    val children = statement.children
    val size = children.size

    var parsingAlias = false

    while (i < size) {
        val child = children.get(i)

        if (child.type == SkeletonType.Atom) {
            val token = child.token
            if (token != null) {
                val value = token.value
                if (value == "as") {
                    parsingAlias = true
                } else if (parsingAlias) {
                    alias = value
                } else {
                    path += value
                }
            }
        }
        i = i + 1
    }

    return Import(path, alias)
}

fun parseEnum(statement: SkeletonNode): EnumDeclaration {
    var name = ""
    var primaryConstructor: List<Parameter> = emptyList()
    var constants = mutableListOf<EnumConstant>()
    var properties = mutableListOf<PropertyDeclaration>()
    var functions = mutableListOf<FunctionDeclaration>()

    val view = statement.children.toListView()
    var i = 0
    while (i < view.size) {
        val child = view.get(i)
        val v = getAtomValue(child)
        if (v == "enum" || v == "class") {
            i++
            continue
        }

        if (child.type == SkeletonType.Atom && name == "") {
            name = v
        } else if (child.type == SkeletonType.Parentheses && primaryConstructor.isEmpty()) {
            val res = parseParameters(child)
            if (res.isSuccess()) {
                primaryConstructor = res.value!!
            }
        } else if (child.type == SkeletonType.CurlyBraces) {
            parseEnumBody(child, constants, properties, functions)
        }
        i++
    }
    return EnumDeclaration(name, ModifierList(emptyList()), primaryConstructor, constants, properties, functions)
}

fun parseEnumBody(node: SkeletonNode, constants: MutableList<EnumConstant>, properties: MutableList<PropertyDeclaration>, functions: MutableList<FunctionDeclaration>) {
    val children = node.children
    var i = 0
    // 1. Parse constants until we hit ';' OR the end
    while (i < children.size) {
        val child = children.get(i)
        val v = getAtomValue(child)

        if (v == ";") {
            i++
            break
        }

        if (child.type == SkeletonType.Atom) {
            val constantName = v
            if (constantName != "" && constantName != ",") {
                 var args = emptyList<Expression>()
                 // Peek if next is Parentheses
                 if (i + 1 < children.size && children.get(i + 1).type == SkeletonType.Parentheses) {
                     i++
                     val paren = children.get(i)
                     args = splitByComma(paren.children.toListView()).map {
                         parseExpression(it.toListView()).value!!
                     }
                 }
                 constants.add(EnumConstant(constantName, args))
            }
        }
        i++
    }

    // 2. Parse the rest as members
    if (i < children.size) {
       // Wrap the remaining nodes in a dummy structure and use parseClassBody
       val remainingChildren = ArrayList(children.subList(i, children.size))
       val dummyNode = SkeletonNode(SkeletonType.CurlyBraces, null, remainingChildren)
       
       // We must split the remaining atoms into Statement nodes before calling parseClassBody
       splitNodeIntoStatements(dummyNode)
       
       parseClassBody(dummyNode, properties, functions)
    }
}

fun splitByComma(view: ListView<SkeletonNode>): List<List<SkeletonNode>> {
    val result = mutableListOf<List<SkeletonNode>>()
    var current = mutableListOf<SkeletonNode>()
    for (i in 0 until view.size) {
        val node = view.get(i)
        if (node.type == SkeletonType.Atom && node.token?.value == ",") {
            if (current.isNotEmpty()) result.add(ArrayList(current))
            current.clear()
        } else {
            current.add(node)
        }
    }
    if (current.isNotEmpty()) result.add(current)
    return result
}