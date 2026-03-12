#include "minikt.hpp"

// --- Forward Declarations ---
enum class SkeletonType : Int;
enum class TokenType : Int;
enum class SkeletonType : Int;
template <typename T>
struct ListView;
struct MatchPair;
template <typename T>
struct TResult;
struct StringView;
struct Token;
struct PairRule;
struct SkeletonNode;
struct SkeletonRule;
struct MatchResult;
struct Scanner;
struct MiniType;
struct MiniClass;
struct MiniTypeAlias;
struct MiniBody;
struct MiniEnum;
struct CompilationUnit;
struct CompilationUnitHandler;
struct IntermediateSemanticParseHeader;
struct SymbolInfo;
struct GlobalSymbolTable;
struct MiniFunction;
struct CppExternal;

// --- Type Aliases ---
template <typename T>
using Predicate = Func<Boolean(T)>;
template <typename T>
using ActionOf = Func<Unit(T)>;
using LexerRule = Func<Int(StringView)>;
using HeaderParserMapper = Func<TResult<SkeletonNode>(Scanner)>;
using FoldRule = Func<TResult<SkeletonNode>(Scanner, List<Token>)>;
using SemanticAnalysisHandler = Func<Unit(SkeletonNode, CompilationUnit)>;
using SortyFunc = Func<Boolean(SymbolInfo, SymbolInfo)>;
// Enum: TokenType
enum class TokenType : Int {
    None,
    KEYWORD,
    IDENTIFIER,
    NUMBER,
    OPERATOR,
    STRING,
    Comment,
    Annotation,
    EndOfFile,
    EOLN
};

// Enum: SkeletonType
enum class SkeletonType : Int {
    CONSTRUCT,
    ATOM,
    PAREN,
    BRACE,
    BRACKET,
    CHEVRON,
    ENUM,
    IMPORT,
    PACKAGE,
    TYPEALIAS,
    CLASS,
    RET,
    IF,
    WHILE,
    PROPERTY,
    FUNCTION,
    FOR
};

// Class: ListView<T>
template <typename T>
struct ListView {
    List<T> items;
    Int start;
    Int len;

    ListView() = default;
};

// Class: MatchPair
struct MatchPair {
    TokenType type;
    LexerRule rule;

    MatchPair() = default;
};

// Class: TResult<T>
template <typename T>
struct TResult {
    T value;
    String errorMessage;

    TResult() = default;
};

// Class: StringView
struct StringView {
    String value;
    Int start;
    Int length;

    StringView() = default;
};

// Class: Token
struct Token {
    TokenType type;
    String value;

    Token() = default;
};

// Class: PairRule
struct PairRule {
    String open;
    String close;
    SkeletonType type;

    PairRule() = default;
};

// Class: SkeletonNode
struct SkeletonNode : KtObject {
    SkeletonType type;
    Token token;
    MutableList<SkeletonNode> children;

    SkeletonNode() = default;
};

// Class: SkeletonRule
struct SkeletonRule : KtObject {
    String trigger;
    FoldRule folder;

    SkeletonRule() = default;
};

// Class: MatchResult
struct MatchResult : KtObject {
    TokenType type;
    Int length;

    MatchResult() = default;
};

// Class: Scanner
struct Scanner : KtObject {
    StringView source;
    Int pos;

    Scanner() = default;
};

// Class: MiniType
struct MiniType {
    String name;
    List<MiniType> generics;
    Boolean isNullable;

    MiniType() = default;
};

// Class: MiniClass
struct MiniClass : KtObject {
    MiniType name;
    Boolean isData;
    List<MiniProperty> properties;

    MiniClass() = default;
};

// Class: MiniTypeAlias
struct MiniTypeAlias : KtObject {
    MiniType name;
    List<MiniProperty> params;
    MiniType returnType;

    MiniTypeAlias() = default;
};

// Class: MiniBody
struct MiniBody {
    List children;
    Ref<SkeletonNode> node;

    MiniBody() = default;
};

// Class: MiniEnum
struct MiniEnum : KtObject {
    String name;
    List<String> constants;

    MiniEnum() = default;
};

// Class: CompilationUnit
struct CompilationUnit : KtObject {
    String pkg;
    MutableList<String> imports;
    MutableList<Any> declarations;

    CompilationUnit() = default;
};

// Class: CompilationUnitHandler
struct CompilationUnitHandler {
    SkeletonType skeletonType;
    SemanticAnalysisHandler handler;

    CompilationUnitHandler() = default;
};

// Class: IntermediateSemanticParseHeader
struct IntermediateSemanticParseHeader {
    MiniType classType;
    Boolean isData;
    List<SkeletonNode> remainderNodes;

    IntermediateSemanticParseHeader() = default;
};

// Class: SymbolInfo
struct SymbolInfo : KtObject {
    MiniType name;
    Boolean isData;
    SkeletonType type;
    Any decl;

    SymbolInfo() = default;
};

// Class: GlobalSymbolTable
struct GlobalSymbolTable : KtObject {
    MutableList<SymbolInfo> symbols;
    MutableList<SymbolInfo> functions;

    GlobalSymbolTable() = default;
};

// Class: MiniFunction
struct MiniFunction : KtObject {
    MiniType name;
    MiniType receiverType;
    List<MiniProperty> params;
    MiniType returnType;
    MiniBody body;

    MiniFunction() = default;
};

// Class: CppExternal
struct CppExternal : KtObject {

    CppExternal() = default;
};

// --- Function Signatures ---

String readFileAsText(String fileName);

void writeFileAsText(String fileName, String content);

List<File> scanFolderByExtension(String folderName, String extension);

template <typename T>
T last(ListView<T> self);

template <typename T>
ListView<T> removeAllIf(ListView<T> self, Predicate<T> function);

template <typename T>
Int indexOfFirst(ListView<T> self, Predicate<T> function);

template <typename T>
T get(ListView<T> self, Int index);

template <typename T>
List<T> toList(ListView<T> self);

template <typename T>
List<T> where(ListView<T> self, Predicate<T> predicate);

template <typename T>
ListView<T> slice(ListView<T> self, Int startFrom, Int len);

template <typename T>
ListView<T> slice(ListView<T> self, Int startFrom);

template <typename T>
Boolean isEmpty(ListView<T> self);

template <typename T>
ListView<T> toListView(List<T> self);

template <typename T, typename R>
TResult<R> asError(TResult<T> self);

template <typename T>
Boolean isSuccess(TResult<T> self);

template <typename T>
Boolean isError(TResult<T> self);

template <typename T>
TResult<T> success(T value);

template <typename T>
TResult<T> onSuccess(TResult<T> self, ActionOf<T> block);

StringView slice(StringView self, Int start, Int end);

StringView slice(StringView self, Int start);

Char get(StringView self, Int index);

Boolean isEmpty(StringView self);

Boolean isNotEmpty(StringView self);

Boolean startsWith(StringView self, String prefix);

StringView toView(String self);

String toDisplayString(Token self);

void kMain(Array<String> args);

MutableList<CompilationUnit> parseFilesToUnits(List<File> kotlinFiles);

Ref<CompilationUnit> parseFileToCompilationUnit(File file);

void printNode(Ref<SkeletonNode> node, Int indent);

Int getIdentifierLength(StringView view);

Int getAnnotationLength(StringView view);

Boolean isIdentifierStart(Char c);

Boolean isIdentifierPart(Char c);

Boolean isDigit(Char c);

Int getEolnLength(StringView view);

Int getKeywordLength(StringView view);

Boolean isWhitespace(Char c);

Int getNumberLength(StringView view);

Int getOperatorLength(StringView view);

Int getStringLength(StringView view);

Int getCommentLength(StringView view);

Ref<MatchResult> findMatch(StringView view);

Boolean isModifier(String text);

void skipWhiteSpaces(Ref<Scanner> self);

TResult<Token> peek(Ref<Scanner> self);

String errorAt(Ref<Scanner> self, Int pos);

Token advance(Ref<Scanner> self);

Boolean isAtEnd(Ref<Scanner> self);

String peekText(Ref<Scanner> self);

Boolean skipIf(Ref<Scanner> self, String text);

TResult<SkeletonNode> expect(Ref<Scanner> self, String text, SkeletonType nodeType);

TResult<Token> match(Ref<Scanner> self, String text);

List<SkeletonNode> modifiersToAtoms(List<Token> modifiers);

TResult<SkeletonNode> foldClassHeader(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldClass(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldClassProperty(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldClassBody(Ref<Scanner> scanner);

TResult<SkeletonNode> foldClassFunction(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldEnum(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldPackage(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldImport(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldVar(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldReturn(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldWhile(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldFor(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldTypeAlias(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldIf(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> parseBranch(Ref<Scanner> scanner);

TResult<SkeletonNode> foldGroup(Ref<Scanner> scanner, String open, String close, SkeletonType type);

TResult<SkeletonNode> foldLineConstruct(Ref<Scanner> scanner, SkeletonType type);

TResult<SkeletonNode> foldExpressionGroup(Ref<Scanner> scanner);

TResult<SkeletonNode> tryFoldChevron(Ref<Scanner> scanner);

TResult<SkeletonNode> foldFunction(Ref<Scanner> scanner, List<Token> modifiers);

TResult<SkeletonNode> foldFunctionHeader(Ref<Scanner> scanner, List<Token> modifiers);

TResult<List<SkeletonNode>> foldLineRemainder(Ref<Scanner> scanner);

TResult<SkeletonNode> foldGroupInHeader(Ref<Scanner> scanner, PairRule rule, HeaderParserMapper childParser);

TResult<SkeletonNode> parseHeaderNext(Ref<Scanner> scanner);

TResult<SkeletonNode> parseNext(Ref<Scanner> scanner);

List<Token> accumulateModifiers(Ref<Scanner> self);

String mapToFullName(MiniType self);

MiniType nameToMiniType(String self);

Ref<CompilationUnit> semanticAnalyze(List<SkeletonNode> nodes);

void analyzePackage(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

void analyzeImport(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

void analyzeEnum(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

void analyzeTypeAlias(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

void analyzeClass(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

void analyzeFunction(Ref<SkeletonNode> node, Ref<CompilationUnit> unit);

Boolean isAtomIdentifier(Ref<SkeletonNode> node);

String semanticJoinAtoms(Ref<SkeletonNode> node);

String semanticGetText(Ref<SkeletonNode> node);

MiniProperty extractFromFoldedProperty(Ref<SkeletonNode> node);

List<SkeletonNode> foldLocalPropertiesFromAtoms(List<SkeletonNode> children);

Ref<SkeletonNode> processPropertyMetadata(Ref<SkeletonNode> prop);

Ref<MiniFunction> semanticLowerFunction(Ref<SkeletonNode> node);

MiniType inferReturnFunctionType(Ref<SkeletonNode> node);

MiniType inferReceiverOfFunction(Ref<SkeletonNode> node);

MiniType inferFunctionType(Ref<SkeletonNode> node);

String semanticResolveType(MiniType name, Ref<GlobalSymbolTable> table);

String semanticResolveTypeFull(MiniType name, Ref<GlobalSymbolTable> table);

Ref<MiniClass> semanticLowerClass(Ref<SkeletonNode> node);

IntermediateSemanticParseHeader intermediateSemanticParseHeader(Ref<SkeletonNode> node);

MutableList<MiniProperty> semanticExtractClassProperties(List<SkeletonNode> node);

void semanticExtractProperties(Ref<SkeletonNode> node, MutableList<MiniProperty> list);

void semanticExtractTypeParams(Ref<SkeletonNode> node, MutableList<MiniType> list);

MiniType semanticExtractType(List<SkeletonNode> nodes);

void semanticExtractGenericParams(ListView<SkeletonNode> genericChildView, MutableList<MiniType> generics);

MiniBody semanticBody(Ref<SkeletonNode> body);

Ref<MiniEnum> semanticLowerEnum(Ref<SkeletonNode> node);

Ref<MiniTypeAlias> semanticTypeAlias(Ref<SkeletonNode> node);

void extractParametersInTypeAlias(ListView<SkeletonNode> children, MutableList<MiniProperty> parameters);

void parseIndividualParameter(ListView<SkeletonNode> toList, MutableList<MiniProperty> parameters);

MiniType extractTypeAliasReturnType(ListView<SkeletonNode> childrenListView);

void semanticExtractTypeParams(Ref<SkeletonNode> node, MutableList<String> list);

template <typename T>
List<T> getTableDeclarations(Ref<GlobalSymbolTable> self);

List<MiniFunction> getTableFunctions(Ref<GlobalSymbolTable> self);

TResult<GlobalSymbolTable> semanticCollectSymbols(List<CompilationUnit> units);

TResult<GlobalSymbolTable> extractTypeDeclarations(List<CompilationUnit> units, Ref<GlobalSymbolTable> table);

void shellSort(MutableList<SymbolInfo> list, SortyFunc sortyFunc);

List<MiniType> getDependentTypesOfMiniClass(Ref<MiniClass> decl);

Boolean isTypeDependentOnOtherType(Ref<SymbolInfo> t1, MiniType typeToSearch);

Boolean areTypesOrdered(Ref<SymbolInfo> t1, Ref<SymbolInfo> t2);

Ref<SymbolInfo> semanticFindSymbol(List<SymbolInfo> list, MiniType name);

template <typename T>
List<T> getDeclarations(List<CompilationUnit> self);

template <typename T>
List<T> getDeclarations(Ref<CompilationUnit> self);

String generateCPlusPlusPreamble(Ref<GlobalSymbolTable> table);

void generateCPlusPlusEnums(MutableList<CompilationUnit> unit, StringBuilder sb);

void generateFunctionBody(MiniBody body, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table);

void generateFunctionBodyNode(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table);

void generateStatement(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table);

void generateLocalVar(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateExpression(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateIf(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateBranch(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

Int findElseIndex(Ref<SkeletonNode> node);

void generateLocalProperty(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateWhile(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateFor(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table);

void generateFunctionSignatures(StringBuilder sb, Ref<GlobalSymbolTable> table);

void generatePerFunctionHeader(Ref<MiniFunction> decl, StringBuilder sb, Ref<GlobalSymbolTable> table);

void generateTemplateHeaderForFunction(Ref<MiniFunction> decl, StringBuilder sb);

void generateAllFunctionBodies(StringBuilder sb, Ref<GlobalSymbolTable> table);

void generateCPlusPlusTypeAliases(Ref<CompilationUnit> unit, StringBuilder sb, Ref<GlobalSymbolTable> table);

void generateOutputCode(Ref<GlobalSymbolTable> globalTable, MutableList<CompilationUnit> allUnits);

void generateStructDefinitions(MutableList<CompilationUnit> unit, StringBuilder sb, Ref<GlobalSymbolTable> table);

void generateCppStructProperty(Ref<MiniClass> decl, Ref<GlobalSymbolTable> table, StringBuilder sb);

// --- Function Implementations ---

String readFileAsText(String fileName) {

    valfile=File(fileName);

    returnfile->readText();

}
void writeFileAsText(String fileName, String content) {

    valfile=File(fileName);

    file->writeText(content);

}
List<File> scanFolderByExtension(String folderName, String extension) {

    valfolder=File(folderName);

    if(!folder->exists()||!folder->isDirectory);
returnemptyList();


    valnormalizedExtension=extension->removePrefix(".");

    returnfolder
        ->walkTopDown();

        ->filter{it->isFile&&it->extension==normalizedExtension}
template <typename T>
T last<T>(ListView<T> self) {

    if(self->len==0);
{
        returnnullptr;
    }
template <typename T>
ListView<T> removeAllIf<T>(ListView<T> self, Predicate<T> function) {

    valresultItems=mutableListOf<T>;
();

    for(indexin0..<len);
{
        valitem=items->get(index);

        if(!function(item));
{
            resultItems->add(item);

        }
template <typename T>
Int indexOfFirst<T>(ListView<T> self, Predicate<T> function) {

    for(iinstart..<start+len);
{
        if(function(itemsi));
{
            returni-start
        }
template <typename T>
T get<T>(ListView<T> self, Int index) {

    returnitemsstart+index;

}
template <typename T>
List<T> toList<T>(ListView<T> self) {

    returnitems->subList(start,start+len);

}
template <typename T>
List<T> where<T>(ListView<T> self, Predicate<T> predicate) {

    varresult=mutableListOf<T>;
();

    for(itemintoList());
{
        if(predicate(item));
{
            result->add(item);

        }
template <typename T>
ListView<T> slice<T>(ListView<T> self, Int startFrom, Int len) {

    returnListView(items,self->start+startFrom,len);

}
template <typename T>
ListView<T> slice<T>(ListView<T> self, Int startFrom) {

    returnListView(items,self->start+startFrom,len-startFrom);

}
template <typename T>
Boolean isEmpty<T>(ListView<T> self) {

    returnlen==0
}
template <typename T>
ListView<T> toListView<T>(List<T> self) {

    returnListView(self,0,self->size);

}
template <typename T,typename R>
TResult<R> asError<T, R>(TResult<T> self) {

    returnTResult<R>;
(nullptr,errorMessage);

}
template <typename T>
Boolean isSuccess<T>(TResult<T> self) {

    returnvalue!=nullptr
}
template <typename T>
Boolean isError<T>(TResult<T> self) {

    returnerrorMessage!=nullptr
}
template <typename T>
TResult<T> success<T>(T value) {

    returnTResult(value,nullptr);

}
template <typename T>
TResult<T> onSuccess<T>(TResult<T> self, ActionOf<T> block) {

    if(!isError());
block(value!!);

    returnself
}
StringView slice(StringView self, Int start, Int end) {

    returnStringView(value,self->start+start,end-start);

}
StringView slice(StringView self, Int start) {

    returnStringView(value,self->start+start,length-start);

}
Char get(StringView self, Int index) {

    returnvaluestart+index;

}
Boolean isEmpty(StringView self) {

    returnlength==0
}
Boolean isNotEmpty(StringView self) {

    returnlength>0
}
Boolean startsWith(StringView self, String prefix) {

    if(prefix->length>length);
{
        returnfalse
    }
StringView toView(String self) {

    returnStringView(self,0,self->length);

}
String toDisplayString(Token self) {

    
    if(self->type==TokenType->EOLN);
return"\\n"

    
    returnself->value;

}
void kMain(Array<String> args) {

    parseFileToCompilationUnit(File("src/main/kotlin/_0lex/LexerRules.kt"));

    valkotlinFiles=scanFolderByExtension("src/main","kt");

    valallUnits=parseFilesToUnits(kotlinFiles);

    
    valtableResult=semanticCollectSymbols(allUnits);

    if(tableResult->isError());
{
        println("SYMBOL ERROR: ${tableResult.errorMessage}");

        return
    }
MutableList<CompilationUnit> parseFilesToUnits(List<File> kotlinFiles) {

    valallUnits=mutableListOf<CompilationUnit>;
();


    for(fileinkotlinFiles);
{
        valunit=parseFileToCompilationUnit(file);

        allUnits->add(unit);

    }
Ref<CompilationUnit> parseFileToCompilationUnit(File file) {

    println("--- Lowering: ${file.name} ---");

    valcontent=readFileAsText(file->canonicalPath);

    valscanner=Scanner(content->toView());


    
    valskeletonNodes=mutableListOf<SkeletonNode>;
();

    while(!scanner->isAtEnd());
{
        valres=parseNext(scanner);

        if(res->isError());
{
            println("SKIP: ${file.name} due to Lexer/Skeleton error: ${res.errorMessage}");

            break
        }
void printNode(Ref<SkeletonNode> node, Int indent) {

    valprefix="  "->repeat(indent);


    
    if(node->type==SkeletonType->ATOM);
{
        valtoken=node->token!!
        println("$prefix${token.toDisplayString()}");

        return
    }
Int getIdentifierLength(StringView view) {

    if(!isIdentifierStart(view->get(0)));
{
        return0
    }
Int getAnnotationLength(StringView view) {

    if(view->get(0)!='@');
{
        return0
    }
Boolean isIdentifierStart(Char c) {

    return(cin'a'..'z');
||(cin'A'..'Z');
||c=='_'
}
Boolean isIdentifierPart(Char c) {

    returnisIdentifierStart(c);
||(cin'0'..'9');

}
Boolean isDigit(Char c) {

    varresult=cin'0'..'9'
    returnresult
}
Int getEolnLength(StringView view) {

    varfirstChar=view->get(0);

    if(firstChar!='\n'&&firstChar!='\r');
return0
    valtextOfChars="\r\n\t "
    varlen=0
    while(len<view->length);
{
        if(!textOfChars->contains(view->get(len)));
{
            break
        }
Int getKeywordLength(StringView view) {

    validLen=getIdentifierLength(view);

    if(idLen==0);
return0

    
    for(kwinkeywords);
{
        if(kw->length==idLen&&view->startsWith(kw));
{
            returnkw->length
        }
Boolean isWhitespace(Char c) {

    returnc==' '||c=='\t'
}
Int getNumberLength(StringView view) {

    if(!isDigit(view->get(0)));
return0
    varlen=1
    while(len<view->length&&isDigit(view->get(len)));
{
        len++
    }
Int getOperatorLength(StringView view) {

    
    for(opinmultiCharOps);
{
        if(view->startsWith(op));
returnop->length
    }
Int getStringLength(StringView view) {

    valquote=view->get(0);

    
    if(quote!='"'&&quote!='\'');
return0

    varlen=1
    while(len<view->length);
{
        valc=view->get(len);


        
        if(c=='\\'&&len+1<view->length);
{
            len+=2
            continue
        }
Int getCommentLength(StringView view) {

    if(view->length<2);
return0
    if(view->startsWith("//"));
{
        varlen=2
        while(len<view->length&&view->get(len)!='\n');
len++
        returnlen
    }
Ref<MatchResult> findMatch(StringView view) {

    for(mpinrules);
{
        vallength=mp->rule(view);

        if(length>0);
returnMatchResult(mp->type,length);

    }
Boolean isModifier(String text) {

    for(minmodifiers);
{
        if(text==m);
returntrue
    }
void skipWhiteSpaces(Ref<Scanner> self) {

    while(pos<source->length);
{
        valcurrentView=source->slice(pos);


        
        if(isWhitespace(source->get(pos)));
{
            pos++
            continue
        }
TResult<Token> peek(Ref<Scanner> self) {

    skipWhiteSpaces();


    if(pos>=source->length);
{
        returnsuccess(Token(TokenType->EndOfFile,""));

    }
String errorAt(Ref<Scanner> self, Int pos) {

    varline=1
    varcol=1

    
    for(iin0untilpos);
{
        if(source->valuei=='\n');
{
            line++
            col=1
        }
Token advance(Ref<Scanner> self) {

    valres=peek();

    if(res->isError());
{
        pos++
        returnToken(TokenType->None,source->slice(pos-1,1)->toString());

    }
Boolean isAtEnd(Ref<Scanner> self) {

    skipWhiteSpaces();

    returnpos>=source->length
}
String peekText(Ref<Scanner> self) {

    returnpeek();
->value?->value?:""
}
Boolean skipIf(Ref<Scanner> self, String text) {

    if(peekText()==text);
{
        advance();

        returntrue
    }
TResult<SkeletonNode> expect(Ref<Scanner> self, String text, SkeletonType nodeType) {

    valtokenRes=peek();

    if(tokenRes->isError());
returntokenRes->asError();


    valtoken=tokenRes->value!!
    if(token->value!=text);
{
        returnerror("Expected '$text' but found '${token.value}' at ${errorAt(pos)}");

    }
TResult<Token> match(Ref<Scanner> self, String text) {

    valres=self->peek();

    if(res->isError());
returnres->asError();


    valtoken=res->value!!
    if(token->value==text);
{
        self->advance();

        returnsuccess(token);

    }
List<SkeletonNode> modifiersToAtoms(List<Token> modifiers) {

    varresult=mutableListOf<SkeletonNode>;
();

    for(minmodifiers);
{
        result->add(SkeletonNode(SkeletonType->ATOM,m));

    }
TResult<SkeletonNode> foldClassHeader(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->CLASS);

    valmodifierNodes=modifiersToAtoms(modifiers);

    node->children->addAll(modifierNodes);

    valkeyword=scanner->advance();

    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));

    
    
    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
{
            returnpeekRes->asError();

        }
TResult<SkeletonNode> foldClass(Ref<Scanner> scanner, List<Token> modifiers) {

    valnodeResult=foldClassHeader(scanner,modifiers);

    if(nodeResult->isError());
returnnodeResult
    valnode=nodeResult->value!!

    
    if(!scanner->isAtEnd()&&scanner->peek()->value?->value=="{");
{
        valbodyResult=foldClassBody(scanner);

        if(bodyResult->isError());
returnbodyResult
        node->children->add(bodyResult->value!!);

    }
TResult<SkeletonNode> foldClassProperty(Ref<Scanner> scanner, List<Token> modifiers) {

    valkeyword=scanner->advance();

    valnode=SkeletonNode(SkeletonType->PROPERTY);

    node->children->addAll(modifiersToAtoms(modifiers));


    
    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));


    
    valnameRes=scanner->peek();

    if(nameRes->isError()||nameRes->value!!->type!=TokenType->IDENTIFIER);
{
        returnerror("Expected property name at ${scanner.errorAt(scanner.pos)}");

    }
TResult<SkeletonNode> foldClassBody(Ref<Scanner> scanner) {

    scanner->advance();

    valbodyNode=SkeletonNode(SkeletonType->BRACE);


    while(!scanner->isAtEnd());
{
        valpeekResult=scanner->peek();

        if(peekResult->isError());
returnpeekResult->asError();

        valpeek=peekResult->value!!

        valtext=peek->value
        if(text=="}");
{
            scanner->advance();

            break
        }
TResult<SkeletonNode> foldClassFunction(Ref<Scanner> scanner, List<Token> modifiers) {

    varfoldFunction=foldFunctionHeader(scanner,modifiers);

    if(foldFunction->isError());
returnfoldFunction->asError();

    valnode=foldFunction->value!!

    
    valnext=scanner->peek();

    if(next->isSuccess());
{
        valt=next->value!!
        valtext=t->value

        if(text=="{");
{
            
            valbodyResult=parseHeaderNext(scanner);

            if(bodyResult->isError());
returnbodyResult
            node->children->add(bodyResult->value!!);

        }
TResult<SkeletonNode> foldEnum(Ref<Scanner> scanner, List<Token> modifiers) {

    
    valenumKeyword=scanner->advance();


    
    valnextResult=scanner->peek();

    if(nextResult->isError());
{
        returnerror(nextResult->errorMessage!!);

    }
TResult<SkeletonNode> foldPackage(Ref<Scanner> scanner, List<Token> modifiers) {

    returnfoldLineConstruct(scanner,SkeletonType->PACKAGE);

}
TResult<SkeletonNode> foldImport(Ref<Scanner> scanner, List<Token> modifiers) {

    returnfoldLineConstruct(scanner,SkeletonType->IMPORT);

}
TResult<SkeletonNode> foldVar(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->RET);


    node->children->addAll(modifiersToAtoms(modifiers));


    scanner->advance();



    
    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnpeekRes->asError();


        valt=peekRes->value!!

        if(t->type==TokenType->EOLN);
{
            returnsuccess(node);

        }
TResult<SkeletonNode> foldReturn(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->RET);


    scanner->advance();



    
    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnpeekRes->asError();


        valt=peekRes->value!!

        if(t->type==TokenType->EOLN);
{
            returnsuccess(node);

        }
TResult<SkeletonNode> foldWhile(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->WHILE);


    
    valkwRes=scanner->expect("while",SkeletonType->ATOM);

    if(kwRes->isError());
returnkwRes->asError();

    node->children->add(kwRes->value!!);


    
    if(scanner->peekText()=="(");
{
        
        valcondRes=foldExpressionGroup(scanner);

        if(condRes->isError());
returncondRes->asError();

        node->children->add(condRes->value!!);

    }
TResult<SkeletonNode> foldFor(Ref<Scanner> scanner, List<Token> modifiers) {

    valkeyword=scanner->advance();

    valnode=SkeletonNode(SkeletonType->FOR);


    for(minmodifiers);
node->children->add(SkeletonNode(SkeletonType->ATOM,m));

    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));


    
    if(scanner->peek()->value?->value=="(");
{
        
        valheader=foldGroup(scanner,"(",")",SkeletonType->PAREN);

        if(header->isError());
returnheader
        node->children->add(header->value!!);

    }
TResult<SkeletonNode> foldTypeAlias(Ref<Scanner> scanner, List<Token> modifiers) {

    valkeyword=scanner->advance();

    valnode=SkeletonNode(SkeletonType->TYPEALIAS);

    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));


    
    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnerror(peekRes->errorMessage!!);


        valt=peekRes->value!!
        
        if(t->type==TokenType->EOLN);
{
            scanner->advance();

            break
        }
TResult<SkeletonNode> foldIf(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->IF);


    
    for(minmodifiers);
{
        node->children->add(SkeletonNode(SkeletonType->ATOM,m));

    }
TResult<SkeletonNode> parseBranch(Ref<Scanner> scanner) {

    valpeek=scanner->peek();
->value!!
    if(peek->value=="{");
{
        returnfoldGroup(scanner,"{","}",SkeletonType->BRACE);

    }
TResult<SkeletonNode> foldGroup(Ref<Scanner> scanner, String open, String close, SkeletonType type) {

    valstartToken=scanner->advance();

    valnode=SkeletonNode(type);


    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnpeekRes->asError();

        valtoken=peekRes->value!!
        valtext=token->value

        if(text==close);
{
            scanner->advance();

            returnsuccess(node);

        }
TResult<SkeletonNode> foldLineConstruct(Ref<Scanner> scanner, SkeletonType type) {

    valkeyword=scanner->advance();

    valnode=SkeletonNode(type);

    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));


    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnerror(peekRes->errorMessage!!);


        valt=peekRes->value!!

        if(t->type==TokenType->EOLN);
{
            
            break
        }
TResult<SkeletonNode> foldExpressionGroup(Ref<Scanner> scanner) {

    valtext=scanner->peekText();


    if(text=="(");
{
        returnfoldGroup(scanner,"(",")",SkeletonType->PAREN);

    }
TResult<SkeletonNode> tryFoldChevron(Ref<Scanner> scanner) {

    valstartPos=scanner->pos
    valopenToken=scanner->advance();


    valchildren=mutableListOf<SkeletonNode>;
();

    varbalance=1

    while(!scanner->isAtEnd()&&balance>0);
{
        valp=scanner->peek();
->value!!
        valtxt=p->value

        if(txt==">");
{
            balance--
            scanner->advance();

            if(balance==0);
{
                valnode=SkeletonNode(SkeletonType->CHEVRON);

                node->children->addAll(children);

                returnsuccess(node);

            }
TResult<SkeletonNode> foldFunction(Ref<Scanner> scanner, List<Token> modifiers) {

    varfoldFunction=foldFunctionHeader(scanner,modifiers);

    if(foldFunction->isError());
returnfoldFunction->asError();

    valnode=foldFunction->value!!

    
    valnext=scanner->peek();

    if(next->isSuccess());
{
        valt=next->value!!
        valtext=t->value

        if(text=="{");
{
            
            valbodyResult=parseNext(scanner);

            if(bodyResult->isError());
returnbodyResult
            node->children->add(bodyResult->value!!);

        }
TResult<SkeletonNode> foldFunctionHeader(Ref<Scanner> scanner, List<Token> modifiers) {

    valnode=SkeletonNode(SkeletonType->FUNCTION);


    valmodifierNodes=modifiersToAtoms(modifiers);

    node->children->addAll(modifierNodes);

    valkeyword=scanner->advance();

    node->children->add(SkeletonNode(SkeletonType->ATOM,keyword));


    
    while(!scanner->isAtEnd());
{
        valpeekRes=scanner->peek();

        if(peekRes->isError());
returnpeekRes->asError();


        valt=peekRes->value!!

        
        if(t->value=="{"||t->value=="=");
break

        
        
        if(t->type==TokenType->EOLN);
{
            node->children->add(SkeletonNode(SkeletonType->ATOM,scanner->advance()));

            continue
        }
TResult<List<SkeletonNode>> foldLineRemainder(Ref<Scanner> scanner) {

    vallist=mutableListOf<SkeletonNode>;
();

    while(!scanner->isAtEnd());
{
        valpeek=scanner->peek();
->value!!
        if(peek->type==TokenType->EOLN);
break

        valres=parseNext(scanner);

        if(res->isError());
{
            returnerror(res->errorMessage!!);

        }
TResult<SkeletonNode> foldGroupInHeader(Ref<Scanner> scanner, PairRule rule, HeaderParserMapper childParser) {

    valopener=scanner->advance();

    valnode=SkeletonNode(rule->type,opener);


    while(!scanner->isAtEnd());
{
        valpeekResult=scanner->peek();

        if(peekResult->isError());
returnpeekResult->asError();


        valpeek=peekResult->value!!

        
        if(peek->value==rule->close);
{
            scanner->advance();

            returnsuccess(node);

        }
TResult<SkeletonNode> parseHeaderNext(Ref<Scanner> scanner) {

    valres=scanner->peek();

    if(res->isError());
returnres->asError();


    valtoken=res->value!!
    valtext=token->value

    
    for(ruleinheaderPairs);
{
        if(text==rule->open);
{
            
            returnfoldGroupInHeader(scanner,rule,::parseHeaderNext);

        }
TResult<SkeletonNode> parseNext(Ref<Scanner> scanner) {

    
    valmodifiers=scanner->accumulateModifiers();


    valres=scanner->peek();

    if(res->isError());
{
        if(modifiers->isNotEmpty());
{
            returnerror("Expected construct after modifiers at ${scanner.errorAt(scanner.pos)}");

        }
List<Token> accumulateModifiers(Ref<Scanner> self) {

    valmodifiers=mutableListOf<Token>;
();

    valscanner=self

    while(!scanner->isAtEnd());
{
        valres=scanner->peek();

        if(res->isError());
break

        valtoken=res->value!!
        
        valisGoodToken=isModifier(token->value);
||token->type==TokenType->Annotation
        if(isGoodToken);
{
            modifiers->add(scanner->advance());

        }
String mapToFullName(MiniType self) {

    valsb=StringBuilder();

    sb->append(name);

    if(generics->isNotEmpty());
{
        sb->append("<");

        for(iingenerics->indices);
{
            sb->append(genericsi->mapToFullName());

            if(i<generics->size-1);
{
                sb->append(",");

            }
MiniType nameToMiniType(String self) {

    returnMiniType(self);

}
Ref<CompilationUnit> semanticAnalyze(List<SkeletonNode> nodes) {

    valunit=CompilationUnit("",mutableListOf<String>(),mutableListOf<Any>());


    for(nodeinnodes);
{
        for(handlerinrulesOfSemanticAnalysis);
{
            if(node->type==handler->skeletonType);
{
                handler->handler(node,unit);

                break
            }
void analyzePackage(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->pkg=semanticJoinAtoms(node);

}
void analyzeImport(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->imports->add(semanticJoinAtoms(node));

}
void analyzeEnum(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->declarations->add(semanticLowerEnum(node));

}
void analyzeTypeAlias(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->declarations->add(semanticTypeAlias(node));

}
void analyzeClass(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->declarations->add(semanticLowerClass(node));

}
void analyzeFunction(Ref<SkeletonNode> node, Ref<CompilationUnit> unit) {

    unit->declarations->add(semanticLowerFunction(node));

}
Boolean isAtomIdentifier(Ref<SkeletonNode> node) {

    if(node->type!=SkeletonType->ATOM);
{
        returnfalse
    }
String semanticJoinAtoms(Ref<SkeletonNode> node) {

    varresult=StringBuilder();

    valatoms=node->children->toListView();
->where(::isAtomIdentifier);


    for(childinatoms);
{
        valt=child->token!!
        result->append(t->value);


    }
String semanticGetText(Ref<SkeletonNode> node) {

    if(node->type==SkeletonType->ATOM);
{
        returnnode->token?->value?:""
    }
MiniProperty extractFromFoldedProperty(Ref<SkeletonNode> node) {

    varname=""
    varisVar=false

    varchildView=node->children->toListView();

    valvarToken=childView->get(0);
->token!!
    if(varToken->value=="var");
{
        isVar=true
        childView=childView->slice(1);

    }
List<SkeletonNode> foldLocalPropertiesFromAtoms(List<SkeletonNode> children) {

    if(children->isEmpty());
returnemptyList();


    valresult=mutableListOf<SkeletonNode>;
();

    varpos=0
    
    varcurrentProperty=SkeletonNode(SkeletonType->PROPERTY);


    while(pos<children->size);
{
        valnode=childrenpos;

        valtoken=node->token

        if(token?->value==",");
{
            
            result->add(processPropertyMetadata(currentProperty));

            currentProperty=SkeletonNode(SkeletonType->PROPERTY);

            pos++
            continue
        }
Ref<SkeletonNode> processPropertyMetadata(Ref<SkeletonNode> prop) {

    
    
    vartypeStartIndex=-1

    for(iinprop->children->indices);
{
        if(prop->childreni->token?->value==":");
{
            typeStartIndex=i+1
            break
        }
Ref<MiniFunction> semanticLowerFunction(Ref<SkeletonNode> node) {

    valparams=mutableListOf<MiniProperty>;
();

    varbody:SkeletonNode?=nullptr

    varfuncType=inferFunctionType(node);

    if(funcType->name=="main");
{
        funcType=MiniType("kMain");

    }
MiniType inferReturnFunctionType(Ref<SkeletonNode> node) {

    valchildren=node->children->toListView();

    valindexOfColon=children->indexOfFirst{it->type==SkeletonType->ATOM&&it->token?->value==":"}
MiniType inferReceiverOfFunction(Ref<SkeletonNode> node) {

    valchildren=node->children->toListView();

    varindexOfDot=children->indexOfFirst{it->type==SkeletonType->ATOM&&it->token?->value=="."}
MiniType inferFunctionType(Ref<SkeletonNode> node) {

    valchildren=node->children->toListView();

    valindexOfFunc=children->indexOfFirst{it->type==SkeletonType->ATOM&&it->token?->value=="fun"}
String semanticResolveType(MiniType name, Ref<GlobalSymbolTable> table) {

    
    valsymbol=semanticFindSymbol(table->symbols,name);


    if(symbol!=nullptr);
{
        
        if(!symbol->isData&&symbol->type==SkeletonType->CLASS);
{
            return"Ref<"+name->name->nameToMiniType();
+">"
        }
String semanticResolveTypeFull(MiniType name, Ref<GlobalSymbolTable> table) {

    returnsemanticResolveType(name,table);

}
Ref<MiniClass> semanticLowerClass(Ref<SkeletonNode> node) {


    valintermediate=intermediateSemanticParseHeader(node);


    valproperties=semanticExtractClassProperties(intermediate->remainderNodes);

    returnMiniClass(intermediate->classType,intermediate->isData,properties);

}
IntermediateSemanticParseHeader intermediateSemanticParseHeader(Ref<SkeletonNode> node) {


    varchildrenListView=node->children->toListView();

    valdataIndexOf=childrenListView->indexOfFirst({it->type==SkeletonType->ATOM&&it->token?->value=="data"});

    valisData=dataIndexOf!=-1
    valclassIndexOf=childrenListView->indexOfFirst({it->type==SkeletonType->ATOM&&it->token?->value=="class"});

    valclassName=childrenListView->get(classIndexOf+1);
->token?->value?:""

    valchevronIndexOf=childrenListView->indexOfFirst({it->type==SkeletonType->CHEVRON});

    valtypeParams=mutableListOf<MiniType>;
();

    if(chevronIndexOf!=-1);
{
        semanticExtractTypeParams(childrenListView->get(chevronIndexOf),typeParams);

    }
MutableList<MiniProperty> semanticExtractClassProperties(List<SkeletonNode> node) {

    valchildrenList=node->toList();

    valproperties=mutableListOf<MiniProperty>;
();

    for(childinchildrenList);
{
        if(child->type==SkeletonType->PAREN);
{
            semanticExtractProperties(child,properties);

        }
void semanticExtractProperties(Ref<SkeletonNode> node, MutableList<MiniProperty> list) {

    if(node->children->isEmpty());
{
        return
    }
void semanticExtractTypeParams(Ref<SkeletonNode> node, MutableList<MiniType> list) {

    
    for(childinnode->children);
{
        if(child->type==SkeletonType->ATOM);
{
            valt=child->token!!
            
            if(t->type==TokenType->IDENTIFIER);
{
                list->add(t->value->nameToMiniType());

            }
MiniType semanticExtractType(List<SkeletonNode> nodes) {

    varchildView=nodes->toListView();


    varlastNode=childView->last();

    varisNullable=false

    if(lastNode!!->type==SkeletonType->ATOM&&lastNode->token?->value=="?");
{
        isNullable=true
        childView=childView->slice(0,childView->len-2);

    }
void semanticExtractGenericParams(ListView<SkeletonNode> genericChildView, MutableList<MiniType> generics) {

    valindexOfComma=genericChildView->indexOfFirst{it->token?->value==","}
MiniBody semanticBody(Ref<SkeletonNode> body) {

    if(body==nullptr);
returnMiniBody(nullptr,nullptr);

    if(body->type==SkeletonType->BRACE);
{
        valchildren=body->children
        valsemChildren=children->map{semanticBody(it);
}
Ref<MiniEnum> semanticLowerEnum(Ref<SkeletonNode> node) {

    valconstants=mutableListOf<String>;
();


    
    

    valenumName=
        node->children->toListView();
->where({it->type==SkeletonType->ATOM&&it->token?->type==TokenType->IDENTIFIER});

            ->get(0);
->token!!->value


    
    for(childinnode->children);
{
        if(child->type==SkeletonType->BRACE);
{
            
            
            for(bodyChildinchild->children);
{
                if(bodyChild->type==SkeletonType->ATOM);
{
                    valt=bodyChild->token!!
                    if(t->type==TokenType->IDENTIFIER);
{
                        constants->add(t->value);

                    }
Ref<MiniTypeAlias> semanticTypeAlias(Ref<SkeletonNode> node) {

    varchildrenListView=node->children->toListView();

    
    
    valaliasName=childrenListView->get(1);
->token!!->value
    valhasGenerics=childrenListView->get(2);
->type==SkeletonType->CHEVRON
    valgenericsTypes=mutableListOf<MiniType>;
();


    varminiType=MiniType(aliasName,genericsTypes,false);


    if(hasGenerics);
{
        valinterestingList=childrenListView->slice(1,2);
->toList();

        miniType=semanticExtractType(interestingList);

        childrenListView=childrenListView->slice(4);

    }
void extractParametersInTypeAlias(ListView<SkeletonNode> children, MutableList<MiniProperty> parameters) {

    if(children->len==0);
{
        return
    }
void parseIndividualParameter(ListView<SkeletonNode> toList, MutableList<MiniProperty> parameters) {

    valindexOfColon=toList->indexOfFirst{it->type==SkeletonType->ATOM&&it->token!!->value==":"}
MiniType extractTypeAliasReturnType(ListView<SkeletonNode> childrenListView) {

    varreturnType=MiniType("void",emptyList(),false);


    valindexOfArrow=childrenListView->indexOfFirst{it->type==SkeletonType->ATOM&&it->token!!->value=="->"}
void semanticExtractTypeParams(Ref<SkeletonNode> node, MutableList<String> list) {

    
    for(childinnode->children);
{
        if(child->type==SkeletonType->ATOM);
{
            valt=child->token!!
            
            if(t->type==TokenType->IDENTIFIER);
{
                list->add(t->value);

            }
template <typename T>
List<T> getTableDeclarations<T>(Ref<GlobalSymbolTable> self) {

    valresult=mutableListOf<T>;
();


    for(declinsymbols);
{
        if(decl->declisT);
{
            result->add(decl->declasT);

        }
List<MiniFunction> getTableFunctions(Ref<GlobalSymbolTable> self) {

    valresult=mutableListOf<MiniFunction>;
();


    for(declinfunctions);
{
        if(decl->declisMiniFunction);
{
            result->add(decl->decl);

        }
TResult<GlobalSymbolTable> semanticCollectSymbols(List<CompilationUnit> units) {

    valtable=GlobalSymbolTable();


    valresultExtraction=extractTypeDeclarations(units,table);

    if(resultExtraction->isError());
{
        returnresultExtraction->asError();

    }
TResult<GlobalSymbolTable> extractTypeDeclarations(List<CompilationUnit> units, Ref<GlobalSymbolTable> table) {

    
    valclasses:List<MiniClass>;
=units->getDeclarations<MiniClass>;
();

    for(declinclasses);
{
        if(semanticFindSymbol(table->symbols,decl->name)!=nullptr);
{
            returnerror("Duplicate class: ${decl.name}");

        }
void shellSort(MutableList<SymbolInfo> list, SortyFunc sortyFunc) {

    vargap=list->size/2
    while(gap>0);
{
        for(iingap..<list->size);
{
            valrightSideValue=listi;

            varj=i-gap
            while(j>=0);
{
                varcurrent=listj;

                if(sortyFunc(current,rightSideValue));
{
                    listi;
=current
                    listj;
=rightSideValue
                }
List<MiniType> getDependentTypesOfMiniClass(Ref<MiniClass> decl) {

    returndecl->properties->map{it->type}
Boolean isTypeDependentOnOtherType(Ref<SymbolInfo> t1, MiniType typeToSearch) {

    if(t1->type==SkeletonType->CLASS);
{
        valminiType=t1->declasMiniClass
        valdependentTypes=getDependentTypesOfMiniClass(miniType);

        if(dependentTypes->isEmpty());
{
            returnfalse
        }
Boolean areTypesOrdered(Ref<SymbolInfo> t1, Ref<SymbolInfo> t2) {

    if(t1->type==SkeletonType->ENUM);
{
        returnfalse
    }
Ref<SymbolInfo> semanticFindSymbol(List<SymbolInfo> list, MiniType name) {

    for(sinlist);
{
        if(s->name->mapToFullName()==name->mapToFullName());
{
            returns
        }
template <typename T>
List<T> getDeclarations<T>(List<CompilationUnit> self) {

    valresult=mutableListOf<T>;
();

    for(unitinself);
{
        vardeclarations=unit->getDeclarations<T>;
();

        result->addAll(declarations);

    }
template <typename T>
List<T> getDeclarations<T>(Ref<CompilationUnit> self) {

    valresult=mutableListOf<T>;
();


    for(declindeclarations);
{
        if(declisT);
{
            result->add(decl);

        }
String generateCPlusPlusPreamble(Ref<GlobalSymbolTable> table) {

    valsb=StringBuilder();

    sb->append("#include \"minikt.hpp\"\n\n");

    sb->append("// --- Forward Declarations ---\n");


    for(symbolintable->symbols);
{
        
        if(symbol->type==SkeletonType->ENUM);
{
            sb->append("enum class ");
->append(symbol->name);
->append(" : Int;\n");

            continue
        }
void generateCPlusPlusEnums(MutableList<CompilationUnit> unit, StringBuilder sb) {

    valenums=unit->getDeclarations<MiniEnum>;
();

    for(declinenums);
{
        sb->append("// Enum: ");
->append(decl->name);
->append("\n");

        sb->append("enum class ");
->append(decl->name);
->append(" : Int {\n");


        for(iin0untildecl->constants->size);
{
            sb->append("    ");
->append(decl->constantsi);

            if(i<decl->constants->size-1);
{
                sb->append(",");

            }
void generateFunctionBody(MiniBody body, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table) {

    
    valnode=body->node!!
    generateFunctionBodyNode(node,sb,isExtension,table);

}
void generateFunctionBodyNode(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table) {

    if(node->type==SkeletonType->BRACE);
{
        sb->append(" {\n");

        for(childinnode->children);
{
            generateStatement(child,sb,isExtension,table);

        }
void generateStatement(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExtension, Ref<GlobalSymbolTable> table) {

    valtype=node->type

    vallocalSb=StringBuilder();

    if(type==SkeletonType->IF);
generateIf(node,localSb,isExtension,table);

    elseif(type==SkeletonType->WHILE);
generateWhile(node,localSb,isExtension,table);

    elseif(type==SkeletonType->FOR);
generateFor(node,localSb,isExtension,table);

    elseif(type==SkeletonType->PROPERTY);
generateLocalVar(node,localSb,isExtension,table);

    elseif(type==SkeletonType->ATOM);
{
        
        generateExpression(node,localSb,isExtension,table);

        if(node->token?->value=="\n");
sb->append(";\n");

    }
void generateLocalVar(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    
    varisVar=false
    varname=""
    varequalsIndex=-1

    
    for(iin0untilnode->children->size);
{
        valchild=node->childreni;

        valtext=semanticGetText(child);


        if(text=="var");
{
            isVar=true
        }
void generateExpression(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    if(node->type==SkeletonType->ATOM);
{
        valt=node->token!!
        valtext=t->value

        if(text=="this");
{
            if(isExt);
sb->append("self");
elsesb->append("this");

        }
void generateIf(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    
    sb->append("if ");


    
    valcondNode=node->children->find{it->type==SkeletonType->PAREN}
void generateBranch(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    if(node->type==SkeletonType->BRACE);
{
        
        generateFunctionBodyNode(node,sb,isExt,table);

    }
Int findElseIndex(Ref<SkeletonNode> node) {

    for(iin0untilnode->children->size);
{
        if(node->childreni->token?->value=="else");
returni
    }
void generateLocalProperty(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    
    varisVar=false
    varname=""
    vartype="auto"
    varhasInitializer=false

    for(childinnode->children);
{
        valtext=semanticGetText(child);

        if(text=="var");
isVar=true
        elseif(text=="val");
isVar=false
        elseif(child->token?->type==TokenType->IDENTIFIER&&name=="");
{
            name=text
        }
void generateWhile(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    sb->append("while ");


    
    valcondNode=node->children->find{it->type==SkeletonType->PAREN}
void generateFor(Ref<SkeletonNode> node, StringBuilder sb, Boolean isExt, Ref<GlobalSymbolTable> table) {

    sb->append("for (");


    
    
    valheader=node->children->find{it->type==SkeletonType->PAREN}
void generateFunctionSignatures(StringBuilder sb, Ref<GlobalSymbolTable> table) {


    sb->append("// --- Function Signatures ---\n\n");

    
    valfunctions:List<MiniFunction>;
=table->getTableFunctions();


    for(declinfunctions);
{
        generatePerFunctionHeader(decl,sb,table);

    }
void generatePerFunctionHeader(Ref<MiniFunction> decl, StringBuilder sb, Ref<GlobalSymbolTable> table) {

    
    generateTemplateHeaderForFunction(decl,sb);


    
    valcppReturn=semanticResolveType(decl->returnType,table);

    sb->append(cppReturn);
->append(" ");
->append(decl->name->name);
->append("(");


    
    varfirstParam=true

    
    if(decl->receiverType!=nullptr);
{
        valreceiverCppType=semanticResolveType(decl->receiverType,table);

        sb->append(receiverCppType);
->append(" self");

        firstParam=false
    }
void generateTemplateHeaderForFunction(Ref<MiniFunction> decl, StringBuilder sb) {

    if(decl->name->generics->isEmpty());
{
        return
    }
void generateAllFunctionBodies(StringBuilder sb, Ref<GlobalSymbolTable> table) {

    sb->append("// --- Function Implementations ---\n\n");


    varfunctions=table->getTableFunctions();

    for(declinfunctions);
{
        
        valbody=decl->body?:continue

        
        if(decl->name->generics->isNotEmpty());
{
            sb->append("template <");

            sb->append(decl->name->generics->joinToString(","){"typename $it"});

            sb->append(">\n");

        }
void generateCPlusPlusTypeAliases(Ref<CompilationUnit> unit, StringBuilder sb, Ref<GlobalSymbolTable> table) {

    valminiTypes=unit->getDeclarations<MiniTypeAlias>;
();


    for(declinminiTypes);
{
        
        
        if(decl->name->generics->isNotEmpty());
{
            sb->append("template <");

            varfirst=true
            for(paramindecl->name->generics);
{
                if(!first);
sb->append(", ");

                sb->append("typename ");
->append(param);

                first=false
            }
void generateOutputCode(Ref<GlobalSymbolTable> globalTable, MutableList<CompilationUnit> allUnits) {

    
    valfinalCpp=StringBuilder();


    
    finalCpp->append(generateCPlusPlusPreamble(globalTable));


    
    for(unitinallUnits);
{
        generateCPlusPlusTypeAliases(unit,finalCpp,globalTable);

    }
void generateStructDefinitions(MutableList<CompilationUnit> unit, StringBuilder sb, Ref<GlobalSymbolTable> table) {

    valclasses=table->getTableDeclarations<MiniClass>;
();

    for(declinclasses);
{
        vallocalSb=StringBuilder();

        localSb->append("// Class: ");
->append(decl->name);
->append("\n");


        
        if(decl->name->generics->isNotEmpty());
{
            valgenericsJoinedText=decl->name->generics->joinToString(",");
{"typename $it"}
void generateCppStructProperty(Ref<MiniClass> decl, Ref<GlobalSymbolTable> table, StringBuilder sb) {

    if(decl->properties->isEmpty());
{
        return
    }
