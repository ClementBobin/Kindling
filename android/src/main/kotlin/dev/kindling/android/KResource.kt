package dev.kindling.android

sealed interface KResource<out T> {
    data class Success<T>(val data: T) : KResource<T>
    data class Error(val throwable: Throwable, val message: String? = throwable.localizedMessage) : KResource<Nothing>
    data object Loading : KResource<Nothing>

    val dataOrNull: T?
        get() = (this as? Success)?.data
}

inline fun <T, R> KResource<T>.map(transform: (T) -> R): KResource<R> {
    return when (this) {
        is KResource.Success -> KResource.Success(transform(data))
        is KResource.Error -> this
        is KResource.Loading -> KResource.Loading
    }
}