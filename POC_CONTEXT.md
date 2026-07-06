# POC Context — BenchmarkJava-lite-demo

This file summarizes the history, current state, and key decisions for this repository so future sessions have full context without re-exploration.

---

## What this project is

A minimal, **speed-optimized** POC of AppSecAI's automated triage (ETA) and remediation (EFA) platform. Trimmed aggressively so the full scan → triage → remediation → PR cycle completes in minutes.

Lineage: full OWASP BenchmarkJava (~2,800 tests) → BenchmarkJava25-demo (40) → BenchmarkJava22-demo (22) → **BenchmarkJava-lite-demo (7)**.

---

## Test file inventory (7 files, 8 findings)

| Category | CWE | Files | Findings |
|----------|-----|-------|----------|
| SQL Injection | CWE-89 | 00008, 00328 | 2 |
| OS Command Injection | CWE-78 | 00176, 01191 | 2 |
| XSS | CWE-79 | 00290, 00492 | 2 |
| Hard-coded Credentials | CWE-798 | **09001** (POC-authored) | 2 |

`BenchmarkTest09001.java` is **not** from OWASP Benchmark — it was authored for this POC because the Benchmark has no hard-coded-credentials category. It contains a generic-api-key string and a JWT literal, which OpenGrep `--config auto` flags via `detected-generic-api-key` and `detected-jwt-token`.

Other single-finding files were chosen deliberately to keep the finding count (and therefore triage time) minimal — 2-3 finding files like 00055/00204/00293 were avoided.

---

## The `.semgrepignore` — the key speed lever

First remediation is gated on **full triage completion**, and triage scales with **total scanner findings, not test files**. A raw `opengrep scan --config auto .` on this repo produced **57 findings**, of which 25 were noise unrelated to the test servlets:

| Excluded path | Noise findings removed |
|---------------|------------------------|
| `.github/` (CI workflows) | 11 (`github-actions-mutable-action-tag`) |
| `src/main/webapp/` (HTML forms, testsuiteutils.js) | 8 (`django-no-csrf-token`, ReDoS) |
| `src/main/java/org/owasp/benchmark/helpers/` | 6 (LDAP, reflection, TLS, file-perms) |
| `src/site/` | — |

With `.semgrepignore` applied **and** the file set trimmed to 7, the live OpenGrep scan reports exactly **8 findings** (verified locally with opengrep v1.16.1).

This is legitimate scoping — the excluded paths are CI config, front-end scaffolding, and shared test fixtures, none of which contain the target vulnerabilities. `.semgrepignore` affects scanning only, never compilation.

---

## Timing model (why 7 files / 8 findings)

Reference run on BenchmarkJava22 (57 findings): first remediation at ~39 min, because triage had to finish all 57 first.

- Fixed startup floor: ~5-6 min (first triage result didn't appear until 5m37s)
- Per-finding triage: ~0.5 min
- Target: 5-10 min first remediation → budget ≈ 8-10 findings

8 findings lands just above the startup floor — near the practical minimum.

---

## Scanner / pipeline

**Workflow:** `.github/workflows/opengrep_triage_remediation_grouped_prod.yml`
**Command:** `opengrep scan --sarif --sarif-output=opengrep-results.sarif --config auto .` (honors `.semgrepignore`)
**Post-scan:** SARIF → AppSecAI `automation-action@v1` for triage + PR generation.

Note: `vuln_file/output_sarif_100.sarif` is legacy ground truth from the 40-file era and is **not** used by this workflow (the pipeline scans live with OpenGrep).

---

## Key files

| Path | Purpose |
|------|---------|
| `src/main/java/org/owasp/benchmark/testcode/` | 7 vulnerable servlets (6 OWASP + 1 POC) |
| `.semgrepignore` | scopes the scan; the main speed lever |
| `src/main/java/org/owasp/benchmark/helpers/` | shared helpers (needed to compile; excluded from scan) |
| `.github/workflows/opengrep_triage_remediation_grouped_prod.yml` | main CI/CD pipeline |
