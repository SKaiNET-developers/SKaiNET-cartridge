package sk.ainet.cartridge.profile.asr.v1

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The bridge must translate the full event vocabulary and always end with onSessionEnded. */
class AsrCallbackBridgeTest {

    private class FakeSession(private val script: Flow<AsrEvent>) : AsrSession {
        override suspend fun feed(chunk: AsrAudioChunk) = Unit
        override suspend fun finish() = Unit
        override fun cancel() = Unit
        override val events: Flow<AsrEvent> get() = script
        override fun close() = Unit
    }

    @Test
    fun bridgesAllEventsInOrderAndEnds() = runTest {
        val session = FakeSession(
            flowOf(
                AsrEvent.Ready,
                AsrEvent.Partial("mach"),
                AsrEvent.Partial("guten morgen"),
                AsrEvent.EndOfSpeech(atMillis = 3100),
                AsrEvent.Final("guten morgen zusammen", tokens = listOf(1, 2, 3)),
            )
        )
        val seen = mutableListOf<String>()
        val job = session.collectInto(this, object : AsrSessionCallback {
            override fun onReady() { seen += "ready" }
            override fun onPartial(text: String) { seen += "partial:$text" }
            override fun onEndOfSpeech(atMillis: Long?) { seen += "eos:$atMillis" }
            override fun onFinal(text: String, tokens: List<Int>?) { seen += "final:$text:${tokens?.size}" }
            override fun onError(error: AsrError) { seen += "error" }
            override fun onSessionEnded() { seen += "ended" }
        })
        job.join()
        assertEquals(
            listOf(
                "ready",
                "partial:mach",
                "partial:guten morgen",
                "eos:3100",
                "final:guten morgen zusammen:3",
                "ended",
            ),
            seen,
        )
    }

    @Test
    fun failureStillEndsTheSession() = runTest {
        val session = FakeSession(
            flowOf(
                AsrEvent.Ready,
                AsrEvent.Failed(AsrError(AsrErrorCode.RECOGNITION_FAILED, "backend died", recoverable = false)),
            )
        )
        val seen = mutableListOf<String>()
        val job = session.collectInto(this, object : AsrSessionCallback {
            override fun onError(error: AsrError) { seen += "error:${error.code}" }
            override fun onSessionEnded() { seen += "ended" }
        })
        job.join()
        assertEquals(listOf("error:RECOGNITION_FAILED", "ended"), seen)
    }
}
