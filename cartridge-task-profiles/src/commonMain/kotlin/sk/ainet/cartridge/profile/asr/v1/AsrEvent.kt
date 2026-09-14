package sk.ainet.cartridge.profile.asr.v1

/**
 * The event vocabulary of an ASR session, merged from the ecosystem's two de-facto
 * contracts (a native CLI runner's `AsrEvent` and the ASR subset of the first consumer app's engine callbacks).
 *
 * Ordering contract: [Ready] → [Partial]* → ([EndOfSpeech])? → ([Final] | [Failed]).
 * The events flow completes after the terminal event ([Final] or [Failed]).
 *
 * A batch engine (e.g. Whisper-on-NPU) emits no [Partial]s and one [Final]; a
 * streaming engine emits [Partial]s and, when its descriptor says
 * `endpointing: signal`, may emit [EndOfSpeech]. Capability, not shape.
 */
public sealed interface AsrEvent {

    /** The session is initialized and accepting audio. */
    public data object Ready : AsrEvent

    /** Incremental transcript; may be superseded by later partials or the final. */
    public data class Partial(val text: String) : AsrEvent

    /**
     * DATA event per the endpointing ruling (ADR-003): the engine detected signal-level
     * end of speech. What that means for the session — stop, keep listening, start a
     * countdown — is the CONSUMER's policy; the engine never decides.
     */
    public data class EndOfSpeech(val atMillis: Long?) : AsrEvent

    /** The committed transcript for this session. Terminal. */
    public data class Final(
        val text: String,
        /** Raw token ids, when the engine exposes them (tokenizer-specific — see spec). */
        val tokens: List<Int>? = null,
    ) : AsrEvent

    /** Terminal failure. */
    public data class Failed(val error: AsrError) : AsrEvent {
        override fun toString(): String = "Failed(${error.code}: ${error.message})"
    }
}
