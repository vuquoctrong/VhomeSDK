package com.example.lib2

import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import timber.log.Timber

@Keep
class Lib2 {

    fun print() {
        Log.e("Printing", "From lib2");
        //val quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
        // launching a new coroutine
        Log.e("ayush: ", "GlobalScope.launch ")
        Timber.e("trong","fafasfaf")
//        GlobalScope.launch {
//            val result = quotesApi.getQuotes()
//            if (result != null)
//            // Checking the results
//                Log.e("ayush: ", result.body().toString())
//        }

    }
}