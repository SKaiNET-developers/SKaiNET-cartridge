package sk.ainet.cartridge.profile.asr.v1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The callback layer of `asr/v1` (ADR-005: two layers, one artifact). Provided so
 * callback-structured consumers (the app-side engines) don't each reinvent the
 * Flow→callback bridging and its cancellation edge cases privately.
 *
 * The Flow contract ([AsrSession.events]) stays the normative core; this is a bridge,
 * not a second source of truth.
 */
public interface AsrSessionCallback {
    public fun onReady() {}
    public fun onPartial(text: String) {}
    /** Signal-level end-of-speech DATA event (ADR-003); acting on it is the consumer's policy. */
    public fun onEndOfSpeech(atMillis: Long?) {}
    public fun onFinal(text: String, tokens: List<Int>?) {}
    public fun onError(error: AsrError) {}
    /** Always invoked last, after the terminal event or collection failure. */
    public fun onSessionEnded() {}
}

/**
 * Collect this session's events into [callback] on [scope]. Returns the collection
 * [Job]; cancelling it detaches the callback without cancelling the session itself —
 * use [AsrSession.cancel] for that.
 */
public fun AsrSession.collectInto(scope: CoroutineScope, callback: AsrSessionCallback): Job =
    scope.launch {
        try {
            events.collect { event ->
                when (event) {
                    is AsrEvent.Ready -> callback.onReady()
                    is AsrEvent.Partial -> callback.onPartial(event.text)
                    is AsrEvent.EndOfSpeech -> callback.onEndOfSpeech(event.atMillis)
                    is AsrEvent.Final -> callback.onFinal(event.text, event.tokens)
                    is AsrEvent.Failed -> callback.onError(event.error)
                }
            }
        } finally {
            callback.onSessionEnded()
        }
    }
