#!/usr/bin/env python3
"""Reference verifier for a cartridge manifest (cartridge-manifest.schema.json).

Recomputes the manifest digest (sha256 of the canonical JSON with 'signatures'
removed) and checks every signature against a public key file, using only the
stdlib + the `openssl` CLI -- no crypto dependency beyond what's already on a
build host. This is the host-side check described in index.adoc, "Integrity,
provenance, and weight-only updates".

Usage:
    python3 verify_manifest.py <manifest.json> <keyid>=<pubkey.pem> [<keyid>=<pubkey.pem> ...]

Example:
    python3 verify_manifest.py asr-whisper-tiny-npu.manifest.json \
        publisher-2026-09=demo-keys/publisher.pub

Exits 0 and prints OK when every signature verifies against the given trust
store; exits 1 otherwise. Does NOT check artifact bytes against 'artifacts[].digest'
-- that step needs the actual artifact files, which this example doesn't ship.
"""
import sys, json, hashlib, base64, subprocess, tempfile, os


def canonical_digest_hex(manifest_without_signatures: dict) -> str:
    blob = json.dumps(manifest_without_signatures, sort_keys=True, separators=(",", ":")).encode()
    return hashlib.sha256(blob).hexdigest()


def verify_sig(pubkey_path: str, message: bytes, sig_b64: str) -> bool:
    sig = base64.b64decode(sig_b64)
    with tempfile.NamedTemporaryFile() as mf, tempfile.NamedTemporaryFile() as sf:
        mf.write(message); mf.flush()
        sf.write(sig); sf.flush()
        p = subprocess.run(
            ["openssl", "pkeyutl", "-verify", "-pubin", "-inkey", pubkey_path,
             "-rawin", "-in", mf.name, "-sigfile", sf.name],
            capture_output=True,
        )
        return p.returncode == 0


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 1
    manifest_path, *keymaps = argv[1:]
    trust_store = dict(kv.split("=", 1) for kv in keymaps)

    manifest = json.load(open(manifest_path))
    signatures = manifest.get("signatures", [])
    unsigned = {k: v for k, v in manifest.items() if k != "signatures"}
    digest_hex = canonical_digest_hex(unsigned)
    message = digest_hex.encode()

    if manifest.get("kind") == "weights-update":
        base = manifest.get("base", {})
        print(f"kind=weights-update, pinned base: {base.get('id')}@{base.get('version')} "
              f"digest={base.get('digest')}")
        print("-> before applying, re-verify that digest matches the CURRENTLY INSTALLED base "
              "manifest (recompute its digest the same way) before trusting this update.")

    ok_all = True
    for sig in signatures:
        keyid = sig["keyid"]
        pubkey_path = trust_store.get(keyid)
        if not pubkey_path:
            print(f"[SKIP] no trusted key supplied for keyid={keyid!r} (signer={sig.get('signer')!r})")
            ok_all = False
            continue
        ok = verify_sig(pubkey_path, message, sig["signature"])
        status = "OK" if ok else "FAIL"
        print(f"[{status}] keyid={keyid} signer={sig.get('signer')!r} alg={sig.get('alg')}")
        ok_all = ok_all and ok

    print(f"manifest digest: sha256:{digest_hex}")
    print("ALL SIGNATURES OK" if ok_all else "VERIFICATION FAILED")
    return 0 if ok_all else 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
