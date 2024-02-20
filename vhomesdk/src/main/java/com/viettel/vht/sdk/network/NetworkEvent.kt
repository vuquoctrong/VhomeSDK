package com.viettel.vht.sdk.network

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import com.viettel.vht.sdk.model.ApiException
import javax.inject.Inject

sealed class NetworkState {
    object NO_INTERNET : NetworkState()
    object UNAUTHORIZED : NetworkState()
    object INITIALIZE : NetworkState()
    object ERROR : NetworkState()
    object NOT_FOUND : NetworkState()
    object BAD_REQUEST : NetworkState()
    object CONNECTION_LOST : NetworkState()
    object FORBIDDEN : NetworkState()
    object SERVER_NOT_AVAILABLE : NetworkState()
    object DATA_ERROR : NetworkState()
    object ACCESS_DENY : NetworkState()
    object NO_PERMISSION : NetworkState()
    object NO_CONNECT_INTERNET : NetworkState()
    object CONNECTED_INTERNET : NetworkState()
    data class GENERIC(val exception: ApiException) : NetworkState()
}

class NetworkEvent @Inject constructor() {

    @ExperimentalCoroutinesApi
    private val events: ConflatedBroadcastChannel<NetworkState> by lazy {
        ConflatedBroadcastChannel<NetworkState>().also { channel ->
            channel.trySend(NetworkState.INITIALIZE).isSuccess
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val observableNetworkState: Flow<NetworkState> = events.asFlow()

    @ExperimentalCoroutinesApi
    fun publish(networkState: NetworkState) {
        Handler(Looper.getMainLooper()).post {
            events.trySend(networkState).isSuccess
        }
    }
}