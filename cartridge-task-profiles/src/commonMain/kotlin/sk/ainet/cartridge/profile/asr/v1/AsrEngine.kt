package sk.ainet.cartridge.profile.asr.v1

import kotlinx.coroutines.flow.Flow

/**
 * The `asr/v1` core contract (Flow-based layer). Implemented by cartridge adapters
 * (over the C Runtime ABI or a language-native cartridge API) and consumed by
 * applications' closed adapters and by runners like a native CLI — never the other
 * way around: this artifact depends on no application and no cartridge.
 *
 * Lifecycle: create (implementation-specific factory) → [session]* → [close].
 */
public interface AsrEngine : AutoCloseable {

    public val info: AsrEngineInfo

    /**
     * Open a new recognition session. Sessions are independent; whether they may run
     * concurrently is an engine/cartridge property (descriptor `requirements`), not a
     * profile guarantee.
     */
    public fun session(config: AsrSessionConfig = AsrSessionConfig()): AsrSession
}

/**
 * One utterance/recognition stream.
 *
 * Threading: [feed] and [finish] from one producer at a time; [cancel] from anywhere
 * (mirrors the ABI's one-session-one-thread rule with cancel as the only cross-thread
 * call). [events] is cold until collected and completes after the terminal event.
 */
public interface AsrSession : AutoCloseable {

    /** Feed audio. Suspends when the implementation applies backpressure. */
    public suspend fun feed(chunk: AsrAudioChunk)

    /**
     * Signal end of input (the host-driven stop). A batch engine does its work here;
     * a streaming engine drains. The terminal event follows on [events].
     */
    public suspend fun finish()

    /** Cooperative cancel; [events] terminates with [AsrEvent.Failed] (code CANCELLED). */
    public fun cancel()

    public val events: Flow<AsrEvent>
}
