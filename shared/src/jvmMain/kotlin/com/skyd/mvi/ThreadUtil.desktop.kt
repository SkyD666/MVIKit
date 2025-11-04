package com.skyd.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual val isMainThread: Boolean =
    (Dispatchers.Main.immediate as? CoroutineDispatcher)
        ?.isDispatchNeeded(CoroutineScope(Dispatchers.Main).coroutineContext) == false

actual val currentThreadName: String = Thread.currentThread().name
