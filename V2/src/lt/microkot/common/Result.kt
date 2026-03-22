package lt.microkot.common

data class TResult<T>(val value: T?, val errorMessage: String)

fun <T, R> TResult<T>.asError(): TResult<R> {
    return TResult<R>(null, errorMessage)
}

fun <T> TResult<T>.isSuccess(): Boolean {
    return errorMessage.isEmpty()
}

fun <T> TResult<T>.isError(): Boolean {
    return !isSuccess()
}

fun <T> success(value: T): TResult<T> {
    return TResult<T>(value, "")
}
fun <T> error(errMessage: String): TResult<T> {
    return TResult<T>(null, errMessage)
}

