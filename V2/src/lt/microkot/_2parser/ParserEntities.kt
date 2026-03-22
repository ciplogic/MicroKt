package lt.microkot._2parser
import lt.microkot._3deepparser.DeepExpression as Expression

data class ModifierList(val modifiers: List<String> = emptyList())
data class Import(val path: String, val alias: String? = null)

/**
 * The top-level container for a MiniKotlin file.
 */
class KotlinParsedFile(
    val fileName: String,
    var packageName: String,
    var imports: MutableList<Import>,
    var declarations: MutableList<Declaration>
) {
    override fun toString() = "File($fileName, $packageName)"
}

/**
 * Represents a type like "String", "List<Int>", or "Map<K, V>"
 */
data class DeclaredType(
    val name: String,
    val generics: List<DeclaredType> = emptyList(),
    val isNullable: Boolean = false // MiniKotlin might need this for Any?
) {
    override fun toString(): String {
        if (generics.isEmpty()) {
            return name
        }
        return "$name<${generics.joinToString(",")}>"
    }
}

class FunctionDeclaration(
    val name: String,
    modifiers: ModifierList,
    val typeParameters: List<DeclaredType> = emptyList(),
    val receiver: DeclaredType? = null, // For String.length()
    val parameters: List<Parameter>,
    val returnType: DeclaredType,
    val body: Block?
) : Declaration(modifiers) {
    override fun toString() = name
}

class TypeAliasDeclaration(
    val aliasType: DeclaredType,
    modifiers: ModifierList,
    val parameterTypes: List<DeclaredType>,
    val returnType: DeclaredType
) : Declaration(modifiers) {
    override fun toString() = aliasType.name
}

class ClassDeclaration(
    val name: String,
    modifiers: ModifierList, // e.g., "data", "sealed"
    val typeParameters: List<DeclaredType> = emptyList(),
    val primaryConstructor: List<Parameter> = emptyList(),
    val baseClass: DeclaredType? = null,
    val baseConstructorArgs: Expression? = null,
    val properties: List<PropertyDeclaration> = emptyList(),
    val functions: List<FunctionDeclaration> = emptyList()
) : Declaration(modifiers) {
    override fun toString() = name
}

class PropertyDeclaration(
    val name: String,
    val isVar: Boolean,
    modifiers: ModifierList,
    val receiver: DeclaredType? = null,
    val type: DeclaredType?,
    val initializer: Expression?
) : Declaration(modifiers) {
    override fun toString() = name
}

sealed class Declaration(val modifiers: ModifierList)

class EnumDeclaration(
    val name: String,
    modifiers: ModifierList,
    val primaryConstructor: List<Parameter> = emptyList(),
    val constants: List<EnumConstant> = emptyList(),
    val properties: List<PropertyDeclaration> = emptyList(),
    val functions: List<FunctionDeclaration> = emptyList()
) : Declaration(modifiers) {
    override fun toString() = name
}

data class EnumConstant(val name: String, val arguments: List<Expression> = emptyList())

// --- Utility Components ---

data class Parameter(val name: String, val type: DeclaredType) {
    override fun toString() = name
}

/**
 * Represents the code inside a { ... }
 */
class Block(val statements: List<Statement>)

sealed class Statement

class DeclarationStatement(val declaration: Declaration) : Statement()
class ExpressionStatement(val expression: Expression) : Statement()
class ReturnStatement(val expression: Expression?) : Statement()
class IfStatement(val condition: Expression, val thenBlock: Block) : Statement()
class ElseStatement(val block: Block) : Statement()
class WhileStatement(val condition: Expression, val block: Block) : Statement()
class VarStatement(val name: String, val isVar: Boolean, val type: DeclaredType?, val initializer: Expression?) : Statement()
class ForStatement(val loopVar: String, val rangeExpr: Expression, val body: Block) : Statement()
class BreakStatement : Statement()
class ContinueStatement : Statement()



