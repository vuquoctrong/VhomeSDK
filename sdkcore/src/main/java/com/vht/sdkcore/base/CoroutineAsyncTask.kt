package com.vht.sdkcore.base

import android.util.Log
import com.vht.sdkcore.utils.Define
import kotlinx.coroutines.*
import java.util.concurrent.Executors

abstract class CoroutineAsyncTask<Params, Progress, Result>() {

    val TAG by lazy {
        CoroutineAsyncTask::class.java.simpleName
    }

    companion object {
        private var threadPoolExecutor: CoroutineDispatcher? = null
    }

    var status: Define.Status = Define.Status.PENDING
    var preJob: Job? = null
    var bgJob: Deferred<Result>? = null
    abstract fun doInBackground(vararg params: Params?): Result
    open fun onProgressUpdate(vararg values: Progress?) {}
    open fun onPostExecute(result: Result?) {}
    open fun onPreExecute() {}
    open fun onCancelled(result: Result?) {}
    protected var isCancelled = false

    /**
     * Executes background task parallel with other background tasks in the queue using
     * default thread pool
     */
    fun execute(vararg params: Params?) {
        execute(Dispatchers.Default, *params)
    }

    /**
     * Executes background tasks sequentially with other background tasks in the queue using
     * single thread executor @Executors.newSingleThreadExecutor().
     */
    fun executeOnExecutor(vararg params: Params?) {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        }
        execute(threadPoolExecutor!!, *params)
    }

    private fun execute(dispatcher: CoroutineDispatcher, vararg params: Params?) {

        if (status != Define.Status.PENDING) {
            when (status) {
                Define.Status.RUNNING -> throw IllegalStateException("Cannot execute task:" + " the task is already running.")
                Define.Status.FINISHED -> throw IllegalStateException("Cannot execute task:"
                        + " the task has already been executed "
                        + "(a task can be executed only once)")
                else -> {
                }
            }
        }

        status = Define.Status.RUNNING

        // it can be used to setup UI - it should have access to Main Thread
        GlobalScope.launch(Dispatchers.Main) {
            preJob = launch(Dispatchers.Main) {
                printLog("onPreExecute started")
                onPreExecute()
                printLog("onPreExecute finished")
                bgJob = async(dispatcher) {
                    printLog("doInBackground started")
                    doInBackground(*params)
                }
            }
            preJob!!.join()
            if (!isCancelled) {
                withContext(Dispatchers.Main) {
                    onPostExecute(bgJob!!.await())
                    printLog("doInBackground finished")
                    status = Define.Status.FINISHED
                }
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        if (preJob == null || bgJob == null) {
            printLog("has already been cancelled/finished/not yet started.")
            return
        }
        if (mayInterruptIfRunning || (!preJob!!.isActive && !bgJob!!.isActive)) {
            isCancelled = true
            status = Define.Status.FINISHED
            if (bgJob!!.isCompleted) {
                GlobalScope.launch(Dispatchers.Main) {
                    onCancelled(bgJob!!.await())
                }
            }
            preJob?.cancel(CancellationException("PreExecute: Coroutine Task cancelled"))
            bgJob?.cancel(CancellationException("doInBackground: Coroutine Task cancelled"))
            printLog("has been cancelled.")
        }
    }

    fun publishProgress(vararg progress: Progress) {
        //need to update main thread
        GlobalScope.launch(Dispatchers.Main) {
            if (!isCancelled) {
                onProgressUpdate(*progress)
            }
        }
    }

    private fun printLog(message: String) {
        Log.d(TAG, message)
    }
}