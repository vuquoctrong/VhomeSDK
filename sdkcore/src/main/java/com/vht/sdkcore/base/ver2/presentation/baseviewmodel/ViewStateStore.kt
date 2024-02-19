package com.vht.sdkcore.base.ver2.presentation.baseviewmodel

import androidx.lifecycle.*

class ViewStateStore<T : Any>(
    initialState: T
) : StoreObservable<T> {
    private val stateLiveData = MutableLiveData<T>().apply {
        value = initialState
    }

    val state: T
        get() = stateLiveData.value!! // It's non-null because have added initial state

    override fun <S> observe(
        owner: LifecycleOwner,
        selector: (T) -> S,
        observer: Observer<S>
    ) {
        stateLiveData.map(selector).distinctUntilChanged().observe(owner, observer)
    }

    fun dispatchState(state: T) {
        stateLiveData.value = state
    }

}

interface StoreObservable<T : Any> {
    fun <S> observe(
        owner: LifecycleOwner,
        selector: (T) -> S,
        observer: Observer<S>
    )
}
