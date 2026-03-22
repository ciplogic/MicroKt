package lt.microkot._2parser
import lt.microkot._3deepparser.DeepExpression as Expression

import lt.microkot._1skelparser.SkeletonNode
import lt.microkot._1skelparser.SkeletonType
import lt.microkot.common.ListView
import lt.microkot.common.get
import lt.microkot.common.slice
import lt.microkot.common.toListView
import lt.microkot.common.TResult
import lt.microkot.common.success
import lt.microkot.common.error
import lt.microkot.common.isError
import lt.microkot.common.asError
import lt.microkot.common.isSuccess

fun parseClass(view: ListView<SkeletonNode>, modifiers: ModifierList): TResult<ClassDeclaration> {
    var name = ""
    var typeParameters = mutableListOf<DeclaredType>()
    var bodyNode: SkeletonNode? = null
    var paramsNode: SkeletonNode? = null
    var baseClassStart = -1
    var i = 0

    while (i < view.size && getAtomValueOfListView(view, i) == "") {
        i++
    }

    if (i < view.size) {
        name = getAtomValueOfListView(view, i)
        i++
    }

    while (i < view.size && getAtomValueOfListView(view, i) == "") {
        i++
    }

    if (i < view.size && view.get(i).type == SkeletonType.Chevron) {
        val chevron = view.get(i)
        typeParameters.addAll(parseGenericList(chevron.children.toListView()))
        i++
    }

    while (i < view.size) {
        val child = view.get(i)
        val type = child.type
        val v = getAtomValue(child)
        if (v == "") {
            i++
            continue
        }

        if (type == SkeletonType.Parentheses && baseClassStart == -1) {
            paramsNode = child
        } else if (v == ":") {
            baseClassStart = i + 1
        } else if (type == SkeletonType.CurlyBraces) {
            bodyNode = child
            break
        }
        i++
    }

    val primaryConstructor = if (paramsNode != null) {
        val res = parseParameters(paramsNode)
        if (res.isError()) {
            return res.asError()
        }
        res.value!!
    } else {
        emptyList()
    }
    var baseClass: DeclaredType? = null
    var baseConstructorArgs: Expression? = null

    if (baseClassStart != -1) {
        val res = parseType(view.slice(baseClassStart))
        if (res.isSuccess()) {
            val parsedResult = res.value!!
            baseClass = parsedResult.type
            val afterTypeIdx = baseClassStart + parsedResult.consumed
            if (afterTypeIdx < view.size && view.get(afterTypeIdx).type == SkeletonType.Parentheses) {
                val parseExpressionResult = parseExpression(view.get(afterTypeIdx).children.toListView())
                baseConstructorArgs = parseExpressionResult.value
            }
        }
    }

    // Now we can handle the body
    val decls = mutableListOf<PropertyDeclaration>()
    val funcs = mutableListOf<FunctionDeclaration>()
    if (bodyNode != null) {
        val res = parseClassBody(bodyNode, decls, funcs)
        if (res.isError()) {
            return res.asError()
        }
    }

    return success(ClassDeclaration(name, modifiers, typeParameters, primaryConstructor, baseClass, baseConstructorArgs, decls, funcs))
}
