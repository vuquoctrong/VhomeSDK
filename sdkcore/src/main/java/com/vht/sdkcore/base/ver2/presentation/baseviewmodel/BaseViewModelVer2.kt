package com.vht.sdkcore.base.ver2.presentation.baseviewmodel

import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.SingleLiveEvent
import com.vht.sdkcore.network.BaseResponse
import com.vht.sdkcore.network.ErrorResponse
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModelVer2<S : Any, E> : ViewModel(), StoreObservable<S>, CoroutineScope {

    private var messageError = SingleLiveEvent<Any>()
    private val viewModelJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob + CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    protected val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    protected val io = CoroutineScope(Dispatchers.IO + viewModelJob)


    private val store by lazy {
        ViewStateStore(this.initState())
    }

    val currentState: S
        get() = store.state

    private val _liveEvent: SingleLiveEvent<E> = SingleLiveEvent()

    val liveEvent: LiveData<E>
        get() = _liveEvent

    abstract fun initState(): S

    override fun <T> observe(owner: LifecycleOwner, selector: (S) -> T, observer: Observer<T>) {
        store.observe(owner, selector, observer)
    }

    protected fun dispatchEvent(event: E) {
        _liveEvent.value = event ?: return
    }

    protected fun dispatchState(state: S) {
        store.dispatchState(state = state)
    }

    fun handleError(
        throwable: Throwable,
        callBack: ((result: ErrorResponse) -> Unit?)?
    ) {
        if (throwable is ConnectException) {
            messageError.postValue(throwable.message)
        } else if (throwable is HttpException) {
            var errorBody: String? = null
            try {
                errorBody = throwable.response()!!.errorBody()!!.string()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var response: ErrorResponse? = null
            try {
                response =
                    Gson().fromJson(
                        errorBody,
                        ErrorResponse::class.java
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (response != null) {
                if (response.status == 401) {
                    callBack?.invoke(response)
                } else {
                    messageError.postValue(
                        response.message ?: "${response.status} " + "${response.error}"
                    )
                }
            } else {
                messageError.postValue(throwable.message)
            }
        } else {
            messageError.postValue(R.string.not_connected_internet)
        }

    }

    inline fun <reified T : BaseResponse> handleError(
        responseBody: ResponseBody
    ): T? {
        var errorBody = ""
        try {
            errorBody = responseBody.string()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val typeResponse =
            object : TypeToken<T>() {}.type
        var response: T? = null
        try {
            response =
                Gson().fromJson(
                    errorBody,
                    T::class.java
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response

    }

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }

}