package com.skyd.mvi

import platform.Foundation.NSThread

actual val isMainThread: Boolean
    get() = NSThread.isMainThread

actual val currentThreadName: String
    get() = NSThread.currentThread().name ?: "unnamed"
