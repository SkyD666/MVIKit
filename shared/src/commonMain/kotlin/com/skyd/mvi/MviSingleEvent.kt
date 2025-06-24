package com.skyd.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Immutable object which represents a single event
 * like snack bar message, navigation event, a dialog trigger, etc...
 */
interface MviSingleEvent

@Composable
fun <T : MviSingleEvent> MviEventListener(
    eventFlow: Flow<T>,
    onEach: suspend (event: T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        eventFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect(onEach)
    }
}