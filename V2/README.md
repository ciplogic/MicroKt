# microKot

A self-hosting transpiler from Kotlin to C++, designed for minimal runtime overhead and high performance.

## Project Goal

The primary objective is to create a compiler for a subset of the Kotlin language (referred to as MiniKotlin) that is capable of compiling its own source code into efficient C++.

## Project Structure

The project is organized into sequential stages, reflected in the package numbering:

### 1. Lexical Analysis (`lt.microkot._0lex`)
- **Scanner**: Converts the source text into a stream of tokens.
- Handles basic tokenization including identifiers, keywords, literals, and operators.

### 2. Skeleton Parsing (`lt.microkot._1skelparser`)
- **SkeletonNode**: A tree structure that groups tokens by balancing "folding" characters like `()`, `{}`, and `[]`.
- **Statement Splitting**: Breaks the token stream into logical statements based on semicolons or newlines.
- This stage simplifies the subsequent parsing by providing a structured view of the code's hierarchy before full semantic parsing.

### 3. Structural Parsing (`lt.microkot._2parser`)
- Parses high-level Kotlin constructs such as:
    - `class`, `data class`, `sealed class`, `enum class`.
    - Functions (`fun`), properties (`val`, `var`), and `import` statements.
- Produces a high-level Abstract Syntax Tree (AST) defined in `ParserEntities.kt`.

### 4. Expression Parsing (`lt.microkot._3deepparser`)
- **Pratt Parser**: A top-down operator precedence parser used to handle complex expressions.
- Handles operator precedence, function calls, property access, and control flow expressions (like `if` and `when`).
- Results in a `DeepExpression` tree.

### 5. Utilities (`lt.microkot.common`)
- Common data structures like `TResult` for error handling and `StringView`/`ListView` for efficient memory management.

---

## Roadmap & Missing Parts

To achieve a full transpilation to C++, the following stages are yet to be implemented:

### Phase 5: Semantic Analysis (Planned)
- **Symbol Table**: Tracking all declarations, scopes, and types.
- **Type Checking**: Verifying type safety and performing type inference.
- **Name Resolution**: Linking usage of identifiers to their respective definitions across different files and packages.

### Phase 6: C++ Code Generation (Planned)
- **C++ Emitter**: Transforming the analyzed AST into C++ source code.
- **Runtime Library**: A minimal set of C++ headers/classes providing Kotlin-like primitives (e.g., `String`, `List`, `Any`).
- **Memory Management**: Implementation of a memory management strategy suitable for the target C++ environment (e.g., smart pointers or a small GC).

---

## How to Run

Currently, the project contains a demo that parses its own source code and runs an expression parsing demonstration.

To run the demo:
1. Open the project in IntelliJ IDEA.
2. Run `lt.microkot.MainKt`.

The output will show the progress of parsing the `src` folder and the results of the Pratt parser demo.
