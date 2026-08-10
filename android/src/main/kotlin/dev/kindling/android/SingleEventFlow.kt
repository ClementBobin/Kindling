package dev.kindling.android

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Conteneur pour gérer les événements uniques (Navigation, Snackbar) 
 * sans réémission lors des changements de configuration.
 */
class SingleEventFlow<T> {
    private val channel = Channel<T>(Channel.BUFFERED)
    
    val flow: Flow<T> = channel.receiveAsFlow()

    suspend fun send(event: T) {
        channel.send(event)
    }

    fun trySend(event: T): Boolean {
        return channel.trySend(event).isSuccess
    }
}