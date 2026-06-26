# POC Context — BenchmarkJava22-demo

This file summarizes the history, current state, and key decisions for this repository so future sessions have full context without re-exploration.

---

## What this project is

A minimal POC demonstration of AppSecAI's automated triage (ETA) and remediation (EFA) platform, built on top of OWASP BenchmarkJava. The repo ships:

- **22 Java test files** — focused on the highest-impact CWE categories for the POC
- **22 HTML form pages** — one per Java test, used as the web UI for each test case
- **`vuln_file/output_sarif_100.sarif`** — hand-crafted SARIF ground truth (subset relevant to these 22 files)
- **GitHub Actions workflows** — OpenGrep scan + AppSecAI triage/remediation pipeline

Derived from **BenchmarkJava25-demo** (40 files), which was itself derived from the full OWASP BenchmarkJava (~2,800 test cases).

---

## Java test file inventory (22 files)

| Category | Count | Test IDs |
|----------|-------|----------|
| sqli (CWE-89) | 10 | 00008, 00204, 00328, 00441, 00603, 01963, 02177, 02369, 02534, 02655 |
| cmdi (CWE-78) | 5 | 00176, 00293, 01191, 01942, 02244 |
| xss (CWE-79) | 5 | 00290, 00492, 00724, 00728, 01347 |
| crypto (CWE-326/327) | 1 | 00055 |
| hash (CWE-328) | 1 | 00963 |

Test files live in `src/main/java/org/owasp/benchmark/testcode/`.  
Each is a `@WebServlet` servlet — auto-discovered at deploy time, no registry needed.

---

## SARIF ground truth

`vuln_file/output_sarif_100.sarif` — 135 true-positive findings across the 40 Java test files.

| Rule | Findings |
|------|----------|
| des-is-deprecated | 33 |
| desede-is-deprecated | 25 |
| tainted-sql-from-http-request | 24 |
| use-of-sha1 | 12 |
| use-of-md5 | 11 |
| tainted-cmd-from-http-request | 11 |
| xss.no-direct-response-writer | 11 |
| cookie-missing-httponly | 8 |

---

## POC build history

### Step 1 — Initial trim from BenchmarkJava100
Reduced the original ~2,800 test case project to 40 Java test files (1+ per CWE class).  
Build config (pom.xml) was optimized for faster Tomcat compilation.

### Step 2 — Webapp HTML cleanup (this session)
**Problem:** Although the Java files were trimmed to 40, the `src/main/webapp/` directory still contained the full ~2,690 HTML form pages from the original project. When OpenGrep ran with `--config auto .`, it scanned all files in the repo, generating ~806 findings — the vast majority coming from:
- 2,650 orphaned HTML files (no corresponding Java handler)
- `testsuiteutils.js` patterns (`$.html(xhr.responseText)` flagged as XSS)

**Action:** Deleted the 2,650 HTML files that had no matching Java test file, plus 23 now-empty webapp subdirectories (`pathtraver-*`, `weakrand-*`, `trustbound-*`, `ldapi-00`, `securecookie-00`, `xpathi-00`, and sub-dirs of cmdi/crypto/hash/sqli/xss for trimmed test IDs).

**Result:** Webapp now contains exactly 40 HTML files matching the 40 Java tests. Expected scanner output drops from ~806 to ~135 true positives.

---

## Webapp structure (post-cleanup)

```
src/main/webapp/
  cmdi-00/    (2 html)    cmdi-01/  (1 html)    cmdi-02/  (2 html)
  crypto-00/  (6 html)    crypto-01/(1 html)    crypto-02/ (4 html)
  hash-00/    (4 html)    hash-01/  (3 html)    hash-02/  (2 html)
  sqli-00/    (4 html)    sqli-01/  (1 html)    sqli-04/  (2 html)
  sqli-05/    (2 html)    sqli-06/  (1 html)
  xss-00/     (2 html)    xss-01/   (2 html)    xss-02/   (1 html)
  WEB-INF/    js/   css/   img/   404.html   Index.html
```

---

## Scanner / pipeline

**Workflow:** `.github/workflows/opengrep_triage_remediation_grouped_prod.yml`  
**Command:** `opengrep scan --sarif --sarif-output=opengrep-results.sarif --config auto .`  
**Post-scan:** SARIF uploaded as artifact, then passed to AppSecAI `automation-action@v1` for triage + PR generation.

**Known remaining noise source:** `src/main/webapp/js/testsuiteutils.js` contains `$("#code").html(xhr.responseText)` — direct HTML injection from AJAX response — which JS rules will flag. This is intentional benchmark scaffolding, not a real app vulnerability.

---

## Key files

| Path | Purpose |
|------|---------|
| `src/main/java/org/owasp/benchmark/testcode/` | 40 vulnerable Java servlets |
| `src/main/java/org/owasp/benchmark/helpers/` | Shared helpers (DatabaseHelper, Utils, etc.) |
| `vuln_file/output_sarif_100.sarif` | Ground-truth SARIF (135 TPs) |
| `src/main/webapp/js/testsuiteutils.js` | Shared JS for all HTML test pages |
| `src/config/web.xml` | Servlet config (Spring DispatcherServlet) |
| `src/main/webapp/WEB-INF/benchmark-servlet.xml` | Spring component-scan config |
| `pom.xml` | Maven build; includes Tomcat deploy profiles |
| `.github/workflows/opengrep_triage_remediation_grouped_prod.yml` | Main CI/CD pipeline |
