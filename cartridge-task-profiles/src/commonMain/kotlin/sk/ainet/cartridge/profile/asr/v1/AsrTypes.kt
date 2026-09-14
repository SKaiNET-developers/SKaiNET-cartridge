package sk.ainet.cartridge.profile.asr.v1

/**
 * Task profile `asr/v1` — the public, versioned consumer contract for ASR cartridges
 * (spec: docs/modules/ROOT/pages/task-profiles.adoc, ADR-005).
 *
 * The profile owns ALL of its types: nothing here references any application's contract
 * (no application types, no command formats) and nothing here references any one cartridge's io
 * shape. Capability differences between engines stay visible in [AsrEngineInfo] and the
 * cartridge descriptor — the profile hides the io shape, never the capability.
 */

/** Profile-owned engine description — mirrors descriptor facts, never app concepts. */
public class AsrEngineInfo(
    /** The cartridge id this engine wraps (descriptor `id`), if cartridge-backed. */
    public val cartridgeId: String?,
    public val name: String,
    public val version: String,
    /** BCP-47 tags, per descriptor `attributes.languages`. */
    public val languages: List<String>,
    /** Whether partial results are emitted incrementally (descriptor `attributes.streaming`). */
    public val streaming: Boolean,
    /** Endpointing machinery inside the engine (descriptor `attributes.endpointing`, ADR-003). */
    public val endpointing: AsrEndpointing,
)

/** Mirrors descriptor `attributes.endpointing` — what machinery is INSIDE the engine. */
public enum class AsrEndpointing { NONE, SIGNAL, SEMANTIC }

/**
 * One chunk of mono PCM16 audio. [ShortArray] on purpose: no wire endianness ambiguity
 * at the API boundary — encoding to the cartridge's io port is the implementation's job.
 */
public class AsrAudioChunk(
    public val pcm16: ShortArray,
    public val sampleRateHz: Int = 16_000,
)

public class AsrSessionConfig(
    /** Requested language (BCP-47); must be within [AsrEngineInfo.languages]. */
    public val language: String? = null,
)

public class AsrError(
    public val code: AsrErrorCode,
    public val message: String,
    public val recoverable: Boolean,
)

public enum class AsrErrorCode {
    ENGINE_UNAVAILABLE,
    MODEL_LOAD_FAILED,
    AUDIO_STREAM_ERROR,
    RECOGNITION_FAILED,
    INFERENCE_TIMEOUT,
    CANCELLED,
    UNKNOWN,
}
