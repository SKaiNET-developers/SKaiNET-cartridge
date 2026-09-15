# The Cartridge Manifesto

*A statement of intent, not a contract. The contract is the
[spec](docs/modules/ROOT/pages/index.adoc), the [Runtime ABI](docs/modules/ROOT/pages/abi.adoc),
the [task profiles](docs/modules/ROOT/pages/task-profiles.adoc), the
[threat model](docs/modules/ROOT/pages/threat-model.adoc) and the
[decision log](docs/modules/ROOT/pages/adr/index.adoc). This page says why they exist.*

## The problem we refuse to keep solving

Every application that wants to run a model on real hardware re-integrates the same three things
from scratch: a model, a runtime, and a target. Every new model is a new integration. Every new
chip is a new integration. Every backend swap is a rewrite. The knowledge of how to make one
model run fast on one device lives in one app, in one team, and dies there.

We think that is a packaging failure, not an engineering necessity. So we named the missing unit.

## What a cartridge is

A **cartridge** is a self-contained, application-agnostic package that runs *one model on given
hardware* and exposes a minimal input→output interface that any application, in any language,
can call.

The name is literal. Like a game cartridge: sealed, swappable, and the console never reaches
inside. You change what the machine can do by changing the cartridge, not by rewiring the
machine.

```
Application   — owns the user, the UI, the product decisions
    ↓
Adapter       — app-specific glue: the app's contract, NLU, command mapping   CLOSED, per app
    ↓
Cartridge     — model + runtime + hardware, behind a C ABI                    PUBLIC, app-agnostic
```

## What we hold to be true

**1. A cartridge knows nothing about any application.**
It knows its model, its runtime, and its hardware. Not a session, not a command schema, not a
product intent. If a line of code references both a runtime primitive and an application
concept, it is in the wrong layer. Because cartridges ship publicly, that line has also just
leaked closed product logic into a public component. The boundary is architectural *and*
a distribution boundary, and we test for it, not just draw it.

**2. Replaceability is a mechanical fact, not a diagram.**
Every cartridge exports the same C runtime ABI, consumable from Kotlin, C, Rust, Python and Go
alike. "Swap the cartridge" must mean: swap the package, and nothing recompiles. Where a swap
does change the shape of the input or output, it happens behind a public, versioned task
profile, and the capability that was gained or lost is written down machine-readably. A swap
that silently loses a language or a streaming mode is not a swap; it is a regression with a
nicer name.

**3. Performance and quality are measured, never promised.**
A cartridge declares what it *is* and what it *measures*: real-time factor on a named device,
word error rate on a pinned evaluation set. When the numbers are bad, the descriptor says so.
A cartridge that is four times slower on one ABI than on another states both numbers. Hosts
route on facts, and a spec sheet is not a fact.

**4. Sealed means verifiable.**
A cartridge runs with native-code and accelerator privileges inside someone's product. Before a
host loads one it must be able to prove this is the artifact the publisher built: every bundled
file content-addressed, the manifest signed, weight updates pinned to the exact base they apply
to. Integrity is designed in from the first cartridge, not retrofitted after the first incident.
And we say plainly what a signature does not prove: it proves origin and integrity of bytes,
not the truth of the numbers inside.

**5. The effective license must fulfill every upstream license.**
A cartridge is a derived work of everything it bundles: weights descended from a published
checkpoint, calibration data, vendored runtime code, a tokenizer. Each artifact carries its
license and its lineage in the manifest, and the cartridge's own license must be satisfiable
given all of them. An artifact with no declared license is a packaging error, not a default.
"App-agnostic" and "open weights" are separate axes; the contract requires the first and
leaves the second to the publisher, but never lets licensing terms leak into the cartridge as
code.

**6. Honesty beats elegance.**
An unoptimized, portable reference implementation is a legitimate cartridge, not a degraded
one, as long as it says what it is. Two Moonshine engines with different endpointing behaviour
are both contract-compliant; hiding the difference would not be. The spec carries maturity
tags on every section, an open-questions list, and a decision log that records why, because a
standard that pretends to be finished is less useful than one that tells you where it isn't.

## What we build toward

- One cartridge, many applications: a voice assistant, a CLI, a test harness, unchanged.
- One application, many cartridges: a faster model, a new quantization, a different accelerator,
  each a package swap behind the same seam.
- Typed, generated bindings so that wiring the wrong tensor shape into a cartridge fails to
  compile rather than at runtime on a user's device.
- A build step that resolves, verifies and stages cartridges per target, and a conformance
  checker that can say "this package is a cartridge" mechanically.
- Weight-only updates, including federated rounds, that land in a prepared shape: separately
  signed, pinned by digest, gated by a task metric rather than a checksum.

## This is not an aspiration paper

The pattern exists. A Whisper-tiny cartridge runs natively on a vendor NPU at real-time factor
below one on a 32-bit ARM device. A Moonshine cartridge streams on ONNX Runtime with voice
activity detection inside. A SKaiNET/IREE Moonshine engine streams in a shipping voice-assistant
application today. The spec names the pattern these already follow and hardens it, so the next
model, the next chip and the next application do not start from scratch.

Sealed. Swappable. Honest about what it does. That is a cartridge.
