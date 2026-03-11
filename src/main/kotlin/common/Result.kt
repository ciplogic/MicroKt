package org.example.common

data class TResult<T>(val value: T?, val errorMessage: String?) {

}

fun <T, R> TResult<T>.asError(): TResult<R> {
    return TResult<R>(null, errorMessage)
}

fun <T> TResult<T>.isSuccess(): Boolean {
    return value != null
}
fun <T> TResult<T>.isError(): Boolean {
    return errorMessage != null
}
fun <T> success(value: T): TResult<T> {
    return TResult(value, null)
}
typealias ActionOf<T> = (T) -> Unit

// Global helper for the Parser
fun <T> TResult<T>.onSuccess(block: ActionOf<T>): TResult<T> {
    if (!isError()) block(value!!)
    return this
}
