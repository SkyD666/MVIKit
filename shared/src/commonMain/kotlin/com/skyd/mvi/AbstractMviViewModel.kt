package com.skyd.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.coroutines.ContinuationInterceptor

private var mviConfig: MviConfig = MviConfig(printLog = true)

inline fun mviConfig(builderAction: MviConfigBuilder.() -> Unit) {
    val builder = MviConfigBuilder()
    builder.builderAction()
    builder.build()
}

@MviConfigDslMarker
class MviConfigBuilder @PublishedApi internal constructor() {
    var printLog: Boolean = true

    @PublishedApi
    internal fun build(): MviConfig = MviConfig(printLog = printLog)
        .also { mviConfig = it }
}

private fun debugCheckMainThread() {
    if (mviConfig.printLog) {
        check(isMainThread) { "Expected to be called on the main thread but was $currentThreadName" }
    }
}

suspend fun debugCheckImmediateMainDispatcher(log: Logger) {
    if (mviConfig.printLog) {
        val interceptor = currentCoroutineContext()[ContinuationInterceptor]
        log.d(
            "debugCheckImmediateMainDispatcher: $interceptor, ${Dispatchers.Main.immediate}, ${Dispatchers.Main}"
        )

        check(interceptor === Dispatchers.Main.immediate) {
            "Expected ContinuationInterceptor to be Dispatchers.Main.immediate but was $interceptor"
        }
    }
}

abstract class AbstractMviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> :
    MviViewModel<I, S, E>, ViewModel() {
    protected open val rawLogTag: String? = null

    private val log by lazy(PUBLICATION) {
        Logger.withTag((rawLogTag ?: this::class.simpleName).orEmpty().take(MAX_TAG_LENGTH))
    }

    private val eventChannel = Channel<E>(Channel.UNLIMITED)
    private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = Int.MAX_VALUE)

    final override val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    final override suspend fun processIntent(intent: I) {
        debugCheckMainThread()
        debugCheckImmediateMainDispatcher(log)

        log.i("processIntent: $intent")
        check(intentMutableFlow.tryEmit(intent)) { "Failed to emit intent: $intent" }
    }

    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }

    // Send event and access intent flow.

    /**
     * Must be called in [MainCoroutineDispatcher.immediate],
     * otherwise it will throw an exception.
     *
     * If you want to send an event from other [kotlinx.coroutines.CoroutineDispatcher],
     * use `withContext(Dispatchers.Main.immediate) { sendEvent(event) }`.
     */
    protected suspend fun sendEvent(event: E) {
        debugCheckMainThread()
        debugCheckImmediateMainDispatcher(log)

        eventChannel.trySend(event)
            .onSuccess {
                if (mviConfig.printLog) log.i("sendEvent: event=$event")
            }
            .onFailure {
                if (mviConfig.printLog) log.e("$it. Failed to send event: $event")
            }
            .getOrThrow()
    }

    protected val intentFlow: Flow<I> get() = intentMutableFlow.asSharedFlow()

    // Extensions on Flow using viewModelScope.

    protected fun Flow<S>.toState(initialValue: S) = stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue
    )

    protected fun <T> Flow<T>.debugLog(subject: String): Flow<T> =
        if (mviConfig.printLog) {
            onEach { log.i(">>> $subject: $it") }
        } else {
            this
        }

    protected fun <T> SharedFlow<T>.debugLog(subject: String): SharedFlow<T> =
        if (mviConfig.printLog) {
            val self = this

            object : SharedFlow<T> by self {
                private val subscriberMutex = Mutex()
                private var subscriberCount = 0

                override suspend fun collect(collector: FlowCollector<T>): Nothing {
                    val count = subscriberMutex.withLock {
                        val c = subscriberCount
                        subscriberCount += 1
                        c
                    }

                    self.collect {
                        log.i(">>> $subject ~ $count: $it")
                        collector.emit(it)
                    }
                }
            }
        } else {
            this
        }

    private companion object {
        private const val MAX_TAG_LENGTH = 23
    }
}
