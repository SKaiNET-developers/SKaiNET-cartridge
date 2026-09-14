# Contributing to SKaiNET-cartridge

Thanks for your interest. This repository holds the cartridge **spec** (Asciidoc under
`docs/`), the JSON **schemas** and signed **example manifests**, the C **runtime ABI** header,
and the Kotlin Multiplatform **task profiles**. It follows the conventions of the
[SKaiNET](https://github.com/SKaiNET-developers/SKaiNET) organisation.

## Ground rules

- Be kind. This project follows the [Code of Conduct](CODE_OF_CONDUCT.md).
- No CLA and no DCO sign-off are required. By contributing you agree that your contribution is
  licensed under the [MIT License](LICENSE).
- Keep changes clear and well-scoped. Prefer the patterns already in the repo over new
  abstractions, and keep unrelated churn out of focused PRs.

## Workflow

1. Open an issue (or pick one) describing the change.
2. Branch from `main`: `feature/short-name` or `feature/123-short-name`.
3. Commit in present tense, first line under ~60 characters, body explaining *why*.
4. Open a pull request into `main`. CI must be green (`build.yml`, `docs.yml`, REUSE).

## Changing the spec

The spec is normative and versioned. Anything that changes a contract — descriptor or manifest
schema fields, ABI functions or structs, task-profile surfaces, the threat model — goes through
an **Architecture Decision Record**:

1. Add `docs/modules/ROOT/pages/adr/adr-NNN-short-title.adoc` (next free number, never reused)
   with `*Status:* proposed (<date>)`, `*Context:*`, `*Decision:*`, `*Consequences:*`.
2. Register it in `docs/modules/ROOT/pages/adr/index.adoc`.
3. Make the schema / header / prose change in the same PR where practical, keeping the ADR's
   status `proposed` until merged.
4. On merge, a follow-up flips the status to `accepted`, bumps `spec_version` (schemas and
   `index.adoc`), and adds a changelog entry at the bottom of `index.adoc`.

Bug fixes, typos, wording clarifications and tooling changes do not need an ADR.

## Examples must stay verifiable

If you touch anything under `docs/modules/ROOT/examples/`:

- Validate every descriptor and manifest against its schema.
- Keep the signed manifests verifiable: `python3 verify_manifest.py <manifest> <keyid>=<pub>`.
  If you change a signed file, re-sign it with throwaway keys (`sign_manifest.py`), commit only
  the `.pub` halves under `demo-keys/`, and note the new keyids in `demo-keys/README.md`.
- Never put internal hostnames, device IPs, or product names into examples — `measured_on`
  names a board class and a date, nothing more.

## Building locally

```sh
./gradlew build                                      # task profiles + tests
npx --yes antora@3.1 --stacktrace docs/antora-playbook.yml   # docs → docs/build/site
```
