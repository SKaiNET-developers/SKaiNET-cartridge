#!/usr/bin/env python3
"""Reference signer for a cartridge manifest (companion to verify_manifest.py).

Computes the manifest digest (sha256 of the canonical JSON with 'signatures'
removed) and appends a detached Ed25519 signature over it, using only the stdlib
+ the `openssl` CLI. This is the publisher/aggregator-side step of index.adoc,
"Integrity, provenance, and weight-only updates"; a production key belongs in
CI/release, not a laptop (OQ6).

Usage:
    python3 sign_manifest.py <manifest.json> <privkey.pem> <keyid> <signer> [signed_at]

Rewrites <manifest.json> in place with the new signature appended (replacing any
existing signature with the same keyid). Prints the manifest digest — which is
also what a weights-update's base.digest must pin.
"""
import sys, json, hashlib, subprocess, tempfile, base64, datetime


def canonical_digest_hex(manifest_without_signatures: dict) -> str:
    blob = json.dumps(manifest_without_signatures, sort_keys=True, separators=(",", ":")).encode()
    return hashlib.sha256(blob).hexdigest()


def sign(privkey_path: str, message: bytes) -> str:
    with tempfile.NamedTemporaryFile() as mf:
        mf.write(message); mf.flush()
        p = subprocess.run(
            ["openssl", "pkeyutl", "-sign", "-inkey", privkey_path, "-rawin", "-in", mf.name],
            capture_output=True, check=True,
        )
        return base64.b64encode(p.stdout).decode()


def main(argv):
    if len(argv) < 5:
        print(__doc__)
        return 1
    manifest_path, privkey_path, keyid, signer = argv[1:5]
    signed_at = argv[5] if len(argv) > 5 else (
        datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))

    manifest = json.load(open(manifest_path))
    unsigned = {k: v for k, v in manifest.items() if k != "signatures"}
    digest_hex = canonical_digest_hex(unsigned)

    sig = {
        "keyid": keyid,
        "alg": "ed25519",
        "signer": signer,
        "signature": sign(privkey_path, digest_hex.encode()),
        "signed_at": signed_at,
    }
    sigs = [s for s in manifest.get("signatures", []) if s.get("keyid") != keyid]
    sigs.append(sig)
    manifest["signatures"] = sigs

    with open(manifest_path, "w") as f:
        json.dump(manifest, f, indent=2)
        f.write("\n")
    print(f"signed {manifest_path} as keyid={keyid}")
    print(f"manifest digest: sha256:{digest_hex}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
