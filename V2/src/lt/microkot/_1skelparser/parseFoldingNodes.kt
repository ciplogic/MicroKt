package lt.microkot._1skelparser

import lt.microkot._0lex.Scanner
import lt.microkot._0lex.Token
import lt.microkot._0lex.TokenType
import lt.microkot._0lex.advance
import lt.microkot._0lex.isAtEnd
import lt.microkot._0lex.peek
import lt.microkot.common.TResult
import lt.microkot.common.asError
import lt.microkot.common.isError
import lt.microkot.common.slice
import lt.microkot.common.success
import lt.microkot.common.toListView
import lt.microkot.common.toMutableList

data class OpenCloseTokenStructure(val openText: String, val closeText: String, val skeletonType: SkeletonType)

private val specialTokens = listOf<OpenCloseTokenStructure>(
    OpenCloseTokenStructure("(", ")", SkeletonType.Parentheses),
    OpenCloseTokenStructure("{", "}", SkeletonType.CurlyBraces)
)

fun indexOfCloseToken(tokenToSearch: Token): Int {
    var pos = 0
    for (token in specialTokens) {
        if (tokenToSearch.value == token.closeText) {
            return pos
        }
        pos++
    }
    return -1
}

fun indexOfMatchingOpenCloseToken(children: MutableList<SkeletonNode>, indexOfClose: Int): Int {
    val openTokenText = specialTokens.get(indexOfClose).openText
    var pos = children.size - 1
    while (pos >= 0) {
        val currentNode = children.get(pos)
        if (currentNode.type != SkeletonType.Atom) {
            pos--
            continue
        }
        if (currentNode.token!!.value == openTokenText) {
            return pos
        }
        pos--
    }
    return -1
}

// Helper to create an Atom node with correct position
private fun atomNode(token: Token): SkeletonNode {
    return SkeletonNode(SkeletonType.Atom, token, position = token.position)
}

// Helper to create a grouping node with correct position
private fun groupNode(type: SkeletonType, children: MutableList<SkeletonNode>, position: Int): SkeletonNode {
    return SkeletonNode(type, null, children, position)
}

// Helper to create a statement node with position from first child
private fun statementNode(children: MutableList<SkeletonNode>): SkeletonNode {
    var pos = 0
    if (children.isNotEmpty()) {
        pos = children[0].position
    }
    return SkeletonNode(SkeletonType.Statement, null, children, pos)
}

fun parseFoldingNodes(scanner: Scanner): TResult<SkeletonNode> {
    val children = mutableListOf<SkeletonNode>()
    val program = SkeletonNode(SkeletonType.RootSkeleton, null, children)

    while (!scanner.isAtEnd()) {
        val peekResult = scanner.peek()
        if (peekResult.isError()) {
            return peekResult.asError()
        }
        val token = scanner.advance()
        val closedTokenIndex = indexOfCloseToken(token)
        if (closedTokenIndex == -1) {
            children.add(atomNode(token))
            continue
        }
        
        val openTokenIndex = indexOfMatchingOpenCloseToken(children, closedTokenIndex)
        // If we found the opening token, we group. Otherwise treat as atom.
        if (openTokenIndex == -1) {
            children.add(atomNode(token))
            continue
        }

        // The position of the group is the position of the opening token
        val openingNode = children.get(openTokenIndex)
        val groupPos = openingNode.position
        
        val startChildrenView = children.toListView()
        val parenChildrenList = startChildrenView.slice(openTokenIndex + 1).toMutableList()
        val startChildrenList = startChildrenView.slice(0, openTokenIndex).toMutableList()

        children.clear()
        children.addAll(startChildrenList)
        children.add(groupNode(specialTokens.get(closedTokenIndex).skeletonType, parenChildrenList, groupPos))
    }

    return success(program)
}

fun splitNodeIntoStatements(node: SkeletonNode) {
    // 1. First, recurse: process the children of this node's children
    for (child in node.children) {
        splitNodeIntoStatements(child)
    }

    // 2. Only perform the "Statement Splitting" logic if this node is a container
    if (node.type != SkeletonType.RootSkeleton && node.type != SkeletonType.CurlyBraces) {
        return
    }
    
    val oldChildren = ArrayList(node.children) // Snapshot of current children
    node.children.clear()

    val currentBuffer = mutableListOf<SkeletonNode>()

    for (i in 0 until oldChildren.size) {
        val child = oldChildren[i]
        
        val isSeparator = child.type == SkeletonType.Atom &&
                (child.token?.type == TokenType.EOLN || child.token?.value == ";")
        
        val isElseKeyword = child.type == SkeletonType.Atom && child.token?.value == "else"

        if (isElseKeyword && currentBuffer.isNotEmpty()) {
            node.children.add(statementNode(ArrayList(currentBuffer)))
            currentBuffer.clear()
        }

        if (isSeparator) {
            // Check if we should ignore EOLN as a separator
            if (child.token?.type == TokenType.EOLN) {
                // Heuristic 1: Next line starts with continuation (dot, brace, paren)
                var nextRelevant: SkeletonNode? = null
                for (j in i + 1 until oldChildren.size) {
                    val next = oldChildren[j]
                    if (next.type == SkeletonType.Atom && next.token?.type == TokenType.EOLN) continue
                    nextRelevant = next
                    break
                }
                
                if (nextRelevant != null) {
                    val v = nextRelevant.token?.value ?: ""
                    if (v == "." || nextRelevant.type == SkeletonType.CurlyBraces || nextRelevant.type == SkeletonType.Parentheses) {
                        continue
                    }
                }

                // Heuristic 2: Current buffer ends with a keyword or head that REQUIRES body
                if (currentBuffer.isNotEmpty()) {
                    val last = currentBuffer.last()
                    val lastAtom = last.token?.value ?: ""
                    
                    // Ends with if/while/for head? e.g. "if (cond)"
                    if (last.type == SkeletonType.Parentheses && currentBuffer.size >= 2) {
                        val prev = currentBuffer[currentBuffer.size - 2].token?.value ?: ""
                        if (prev == "if" || prev == "while" || prev == "for") {
                            continue
                        }
                    }
                    
                    // Ends with keyword that needs continuation?
                    if (lastAtom == "if" || lastAtom == "while" || lastAtom == "for" || lastAtom == "else") {
                        continue
                    }
                }
            }

            if (currentBuffer.isNotEmpty()) {
                node.children.add(statementNode(ArrayList(currentBuffer)))
                currentBuffer.clear()
            }
        } else {
            currentBuffer.add(child)
        }
    }

    if (currentBuffer.isNotEmpty()) {
        node.children.add(statementNode(ArrayList(currentBuffer)))
    }
}

fun foldChevrons(statements: MutableList<SkeletonNode>) {
    for (statement in statements) {
        if (statement.type == SkeletonType.Statement) {
            foldChevronsList(statement.children)
        }
    }
}

fun foldChevronsList(children: MutableList<SkeletonNode>) {
    var i = 0
    while (i < children.size) {
        val child = children.get(i)
        
        if (child.type == SkeletonType.Atom && child.token?.value == ">") {
            var openTokenIndex = -1
            var pos = i - 1
            while (pos >= 0) {
                val currentNode = children.get(pos)
                if (currentNode.type == SkeletonType.Atom && currentNode.token?.value == "<") {
                    openTokenIndex = pos
                    break
                }
                pos--
            }
            if (openTokenIndex != -1) {
                val openingNode = children.get(openTokenIndex)
                val chevronChildren = ArrayList<SkeletonNode>()
                for (j in openTokenIndex + 1 until i) {
                    chevronChildren.add(children.get(j))
                }
                val removeCount = i - openTokenIndex + 1
                for (j in 0 until removeCount) {
                    children.removeAt(openTokenIndex)
                }
                children.add(openTokenIndex, groupNode(SkeletonType.Chevron, chevronChildren, openingNode.position))
                i = openTokenIndex
            }
        }
        i++
    }
}