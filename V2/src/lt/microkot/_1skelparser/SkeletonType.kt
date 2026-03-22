package lt.microkot._1skelparser

enum class SkeletonType {
    RootSkeleton,
    Atom,       // A single Token (val, identifier, etc.)
    Parentheses,      // (...)
    CurlyBraces,      // {...}
    Statement,
    Chevron,
}
