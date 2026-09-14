# SKaiNET-cartridge

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![REUSE status](https://api.reuse.software/badge/github.com/SKaiNET-developers/SKaiNET-cartridge)](https://api.reuse.software/info/github.com/SKaiNET-developers/SKaiNET-cartridge)
[![Docs](https://img.shields.io/badge/docs-skainet--developers.github.io-blue)](https://skainet-developers.github.io/SKaiNET-cartridge/)

The home of the **cartridge** concept in the [SKaiNET](https://github.com/SKaiNET-developers/SKaiNET)
universe — spec, schemas, runtime ABI, task profiles, and decision log, versioned together.

A *cartridge* is a self-contained, application-agnostic software package that runs one model on
given hardware, with a minimal input→output API any application — in any language — can call.
It is the deployment unit that bridges a SKaiNET model and a real application on real hardware:
`App → Adapter → Cartridge`, where the cartridge is public and HW-native and the adapter is the
app-specific (often closed) glue.

Start with the [MANIFESTO](MANIFESTO.md) (the short front door), then the spec.

> **Project status — normative draft.** Spec v0.3, ABI v0.1. Every section carries a maturity
> tag (*working* / *drafted* / *direction*). Expect the drafted parts to change through ADRs.

## The spec (v0.3)

Rendered documentation: **https://skainet-developers.github.io/SKaiNET-cartridge/**

The source is an [Antora](https://antora.org) component (`skainet-cartridge`), readable directly
as Asciidoc:

- [`index.adoc`](docs/modules/ROOT/pages/index.adoc) — the concept, contract, descriptor,
  integrity/licensing model, consumption modes, open questions
- [`abi.adoc`](docs/modules/ROOT/pages/abi.adoc) — the C Runtime ABI (normative header:
  [`cartridge-abi/include/cartridge_abi.h`](cartridge-abi/include/cartridge_abi.h))
- [`task-profiles.adoc`](docs/modules/ROOT/pages/task-profiles.adoc) — per-task consumer
  contracts (`asr/v1`, `yolo/v1`) and the typed facade
- [`threat-model.adoc`](docs/modules/ROOT/pages/threat-model.adoc) — who verifies what, where
- [`roadmap.adoc`](docs/modules/ROOT/pages/roadmap.adoc) — phases 2–5
- [`adr/`](docs/modules/ROOT/pages/adr/index.adoc) — decision log

Worked, runnable examples (JSON schemas, signed example manifests, `verify_manifest.py` /
`sign_manifest.py`) live in [`docs/modules/ROOT/examples/`](docs/modules/ROOT/examples/):

```sh
cd docs/modules/ROOT/examples
python3 verify_manifest.py asr-whisper-tiny-npu.manifest.json \
    publisher-2026-09=demo-keys/publisher.pub
```

## Code

- [`cartridge-abi/`](cartridge-abi/) — the ABI header (C, FFI-clean)
- [`cartridge-task-profiles/`](cartridge-task-profiles/) — Kotlin Multiplatform module with the
  public task profiles: `asr/v1` (Flow-based core + callback bridge) and `yolo/v1`
  (single-shot), package `sk.ainet.cartridge.profile`

```sh
./gradlew build
```

## Reference cartridges

The spec was extracted from, and is validated against, two working cartridges: a batch
Whisper-tiny cartridge running natively on a vendor NPU (`asr-whisper-tiny-npu`) and a
streaming Moonshine cartridge on ONNX Runtime with VAD inside (`asr-moonshine-ort-cpu`).
Their descriptors and signed manifests are the worked examples; retrofitting both onto the
Runtime ABI is roadmap Phase 3.

## Building the docs locally

```sh
npx --yes antora@3.1 --stacktrace docs/antora-playbook.yml
open docs/build/site/index.html
```

## Contributing

Spec changes go through an ADR in `docs/modules/ROOT/pages/adr/` — see
[CONTRIBUTING.md](CONTRIBUTING.md). This project follows the SKaiNET
[Code of Conduct](CODE_OF_CONDUCT.md).

## License

MIT — see [LICENSE](LICENSE).
