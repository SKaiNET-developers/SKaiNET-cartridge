# The Cartridge Manifesto

*The front door to the cartridge concept. Nothing here is normative — the exact contract lives
in the spec: [`docs/modules/ROOT/pages/index.adoc`](docs/modules/ROOT/pages/index.adoc)
(spec v0.2), with the [Runtime ABI](docs/modules/ROOT/pages/abi.adoc),
[task profiles](docs/modules/ROOT/pages/task-profiles.adoc),
[threat model](docs/modules/ROOT/pages/threat-model.adoc),
[roadmap](docs/modules/ROOT/pages/roadmap.adoc), and
[decision log](docs/modules/ROOT/pages/adr/index.adoc).*

## The idea

A **cartridge** is a self-contained, application-agnostic software package that runs *one model
on given hardware*, exposing a minimal input→output API any application — in any language —
can call. Like a game cartridge: sealed, swappable, and the host never reaches inside it.

```
Application (any app)
    -> Adapter    (app-specific glue: the app's contract, NLU, command mapping)  — CLOSED, per-app
    -> Cartridge  (self-contained; runs one model on given HW)                    — PUBLIC, app-agnostic
```

Two forces this resolves: **reuse** (one cartridge, many applications — a voice assistant, a CLI,
a test harness, unchanged) and **replaceability** (a faster model or a new backend is a new
cartridge behind the same seam — the app is untouched).

Replaceability comes in two planes, and the spec keeps them distinct: a **binary-plane** swap
(same io contract — e.g. an updated weights drop, or the German model set for the English one)
is a package swap, nothing recompiles; a **profile-plane** swap (different io shape, same task —
batch Whisper for streaming Moonshine) happens behind a public, versioned *task profile*, with
the capability difference surfaced machine-readably, never lost silently.

## The boundary

A cartridge knows its model, its runtime, and its hardware — and *nothing about any
application*. **Boundary test:** if a line of code references both a runtime primitive *and* an
application concept, it's in the wrong layer — and because cartridges ship publicly, it has
also leaked closed logic into a public component. The same boundary is a *distribution*
boundary: cartridge code, ABI, descriptor, and signed manifest are always public and
verifiable; whether the *weights* are free, commercially licensed, or encrypted-and-gated is a
separate per-cartridge choice — and the cartridge's license must fulfill the license of
everything it bundles, weights included.

## What the contract buys

- A **C runtime ABI** every cartridge exports — consumable from Kotlin, C, Rust, Python, Go
  alike — so "replaceable as a unit" is a mechanical fact, not a diagram.
- A **capability descriptor** hosts route on: measured performance *and* measured quality
  (WER for ASR), honestly reported — including when the numbers are bad.
- **Compile-time type safety** for Kotlin consumers, generated from the descriptor in the
  SKaiNET DSL's style: wiring the wrong port shape into a cartridge fails to compile.
- A **signed, content-addressed manifest** so a host can prove a cartridge is the original,
  unmodified artifact — with per-artifact license lineage, and a weight-update path whose
  regression gate is a task metric, not a checksum.

## It already exists

This isn't an aspiration paper. The Whisper-on-NPU cartridge runs Whisper-tiny on a vendor NPU at
RTF < 1 on a real 32-bit ARM device; the ORT Moonshine cartridge streams Moonshine on ONNX Runtime
with VAD inside; the SKaiNET/IREE Moonshine v2 engine streams in a shipping voice-assistant app today. The spec names and hardens
the pattern these already follow — see the spec's instance table and the roadmap for what gets
retrofitted next.
