package com.skyd.mvi

import android.os.Looper

actual val isMainThread: Boolean
    get() = Looper.getMainLooper() === Looper.myLooper()

actual val currentThreadName: String
    get() = Thread.currentThread().name
