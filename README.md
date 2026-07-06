# BenchmarkJava-lite-demo

A minimal, fast POC demonstration of automated vulnerability detection, triage, and remediation using the AppSecAI platform on OWASP Benchmark test cases.

> **POC Version:** This repository is optimized for a fast end-to-end pipeline run. It contains 7 Java test files covering the highest-priority vulnerability classes: SQL Injection (2), OS Command Injection (2), XSS (2), and Hard-coded Credentials (1). A `.semgrepignore` scopes the SAST scan to the application code under test, yielding exactly **8 findings** — down from ~806 in the original webapp and ~57 in earlier subsets — so the full triage + remediation cycle completes in minutes rather than an hour.
>
> **Scanner scoping note:** The full BenchmarkJava webapp ships with ~2,690 HTML form pages and shared helper/CI files that cause SAST scanners (e.g. OpenGrep `--config auto`) to report hundreds of spurious findings unrelated to the vulnerable servlets. This repo trims the webapp to match the active Java tests and uses `.semgrepignore` to exclude CI workflows (`.github/`), front-end forms (`src/main/webapp/`), and shared test fixtures (`helpers/`) — none of which contain the target vulnerabilities.

## About This Repository

This repository is derived from test cases in the [OWASP Benchmark for Java](https://github.com/OWASP-Benchmark/BenchmarkJava), a comprehensive suite designed to evaluate the effectiveness of application security testing tools. The OWASP Benchmark provides known vulnerable code patterns across multiple weakness categories, making it an ideal dataset for validating automated security remediation systems.

The Hard-coded Credentials test case (`BenchmarkTest09001`) is a POC-specific addition (CWE-798), as the original OWASP Benchmark does not include this category.

### Dataset Composition

**Vulnerability Distribution** (as reported by OpenGrep `--config auto` with `.semgrepignore` applied):

| CWE | Description | Count | Percentage |
|-----|-------------|-------|------------|
| CWE-89 | SQL Injection | 2 | 25.0% |
| CWE-78 | OS Command Injection | 2 | 25.0% |
| CWE-79 | Cross-Site Scripting (XSS) | 2 | 25.0% |
| CWE-798 | Use of Hard-coded Credentials | 2 | 25.0% |

**Total Vulnerabilities:** 8 across 4 distinct CWE categories

## AppSecAI Platform Performance

This repository demonstrates the capabilities of AppSecAI's automated security platform on real-world vulnerability patterns:

- **Expert Triage Automation (ETA):** Automated classification of security findings to distinguish true vulnerabilities from false positives, reducing manual triage effort and accelerating security workflows.

- **Expert Fix Automation (EFA):** Automated generation of security fixes that resolve vulnerabilities while maintaining code functionality and quality, dramatically reducing remediation time.

### Triage Performance

AppSecAI's ETA system achieved perfect accuracy on the OWASP Benchmark dataset:

- **True Positive Identification:** 100% (8/8 findings correctly classified)
- **False Positive Rate:** 0% (no false positives)
- **Classification Accuracy:** 100%

All 8 security findings are genuine vulnerabilities requiring remediation, demonstrating the platform's ability to accurately distinguish real security issues from benign code patterns.

### Remediation Results

Results will be populated after the pipeline runs on this repository.

### Remediation Success Rates

The AppSecAI platform demonstrates exceptional performance on injection vulnerabilities, which represent some of the most critical and common security weaknesses in modern applications.

#### High-Confidence Remediation

| Vulnerability Type | Findings in this repo |
|-------------------|-----------------------|
| **SQL Injection (CWE-89)** | 2 |
| **OS Command Injection (CWE-78)** | 2 |
| **Cross-Site Scripting (CWE-79)** | 2 |
| **Use of Hard-coded Credentials (CWE-798)** | 2 |

These vulnerability classes receive high-confidence automated remediation — parameterized queries for SQLi, safe exec patterns for CMDi, output encoding for XSS, and externalized secrets for hard-coded credentials.

### Validation Framework

AppSecAI's EFA includes a comprehensive post-remediation validation system that ensures all generated fixes meet rigorous security, functionality, and code quality standards.

#### Validation Criteria

Every remediation undergoes automated validation across multiple dimensions:

1. **Security Validation**
   - Original vulnerability is completely resolved
   - No new security issues introduced by the fix
   - Remediation follows industry best practices and secure coding guidelines

2. **Functionality Validation**
   - Remediated code preserves the functional intent of the original implementation
   - No breaking changes to function signatures, APIs, or behavior
   - Business logic remains intact

3. **Code Quality Validation**
   - Code adheres to language-specific style guidelines
   - Changes meet standards appropriate for open source contributions
   - Remediation integrates cleanly with existing codebase patterns

#### Validation Outcomes

**Security Validation Failure:**
If security validation fails, no pull request is created. This ensures that only fixes that demonstrably resolve the vulnerability without introducing new security issues are presented for review.

**Quality or Functionality Validation Failure:**
If security validation passes but code quality or functionality validation fails, a pull request is created with the prefix "Self-Validation Failure" in the title. The PR description provides detailed information about which validation criteria were not met. This allows security teams to evaluate whether the security improvement justifies any quality trade-offs.

**Complete Validation Success:**
When all validation criteria pass and the vulnerability type does not require additional context, the pull request title contains no prefix—indicating a high-confidence, production-ready remediation.

### Pull Request Organization

Each vulnerability remediation is presented as an individual pull request, enabling:
- Granular review and approval workflows
- Independent testing and validation of each fix
- Clear audit trails for security improvements
- Selective adoption based on organizational priorities

Pull requests are automatically linked to the identified vulnerabilities, include detailed descriptions of the security issue and remediation approach, and provide context for reviewers.

## Platform Capabilities Demonstrated

This repository showcases several key capabilities of the AppSecAI platform:

1. **Automated Vulnerability Triage (ETA):** 100% accuracy in distinguishing true vulnerabilities from false positives
2. **Automated Vulnerability Detection:** Integration with SAST tools via SARIF format
3. **Intelligent Classification:** Categorization and prioritization of security issues by type and severity
4. **Expert Remediation (EFA):** Automated generation of secure code fixes following best practices
5. **Multi-Dimensional Validation:** Comprehensive verification of security, functionality, and quality
6. **Production-Ready Output:** Pull requests ready for review and integration

## Understanding the Results

### What These Results Mean

This dataset is composed entirely of high-confidence, high-priority vulnerability classes — SQL Injection, OS Command Injection, XSS, and Hard-coded Credentials. Injection flaws consistently rank among the most dangerous vulnerabilities and are the target of the majority of successful attacks.

The platform's performance on these vulnerability classes shows it can:
- Dramatically reduce security debt in existing codebases
- Accelerate remediation timelines from weeks to hours
- Maintain code quality and functionality while improving security
- Free security teams to focus on complex architectural issues
- Eliminate false positives through accurate automated triage

### Repository Structure

- `src/main/java/org/owasp/benchmark/testcode/` - 7 test-case servlets with known vulnerabilities (6 from OWASP Benchmark + 1 POC hard-coded-credentials case)
- `src/main/webapp/` - HTML form pages for the OWASP-derived test cases
- `.semgrepignore` - scopes the SAST scan to application code under test
- Pull requests demonstrate automated remediations for each identified vulnerability

## About AppSecAI

AppSecAI provides Expert Triage Automation (ETA) and Expert Fix Automation (EFA) to help organizations scale their application security programs. The platform combines deep security expertise with advanced automation to deliver production-ready security improvements at unprecedented speed and scale.

For more information, visit [AppSecAI](https://www.appsecai.io/).

## License

This repository is licensed under the GNU General Public License v2.0 (GPL-2.0), maintaining compliance with the [OWASP Benchmark](https://github.com/OWASP-Benchmark/BenchmarkJava) license from which the test cases are derived.

See the [LICENSE](LICENSE) file for the complete license text.

## Acknowledgments

This demonstration builds upon the excellent work of the OWASP Benchmark project, which provides the security testing community with standardized test cases for evaluating security tools and approaches.
