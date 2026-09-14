# Security Policy

## Reporting a vulnerability

Please report vulnerabilities privately through GitHub's
[private vulnerability reporting](https://github.com/SKaiNET-developers/SKaiNET-cartridge/security/advisories/new)
(Security tab → *Report a vulnerability*). Do not open a public issue for security reports.

You will receive an acknowledgement within a few days. Please give us reasonable time to
assess and fix the issue before disclosing it publicly.

## Scope

In scope:

- The JSON schemas and the integrity model they define (manifest digests, signatures,
  weight-update base pinning).
- The reference `sign_manifest.py` / `verify_manifest.py` scripts.
- The C runtime ABI header (`cartridge-abi/include/cartridge_abi.h`) and the Kotlin task
  profiles in `cartridge-task-profiles/`.

Out of scope:

- Vulnerabilities in cartridges built by third parties against this spec.
- Third-party dependency CVEs that are not reachable from this repository's code.

## Supported versions

The `main` branch and the latest tagged spec version.
