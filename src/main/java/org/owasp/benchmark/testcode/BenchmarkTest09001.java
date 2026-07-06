/**
 * OWASP Benchmark Project v1.2
 *
 * This file is a member of the OWASP Benchmark Project. For details, please see
 * <a href="https://owasp.org/www-project-benchmark/">https://owasp.org/www-project-benchmark/</a>.
 *
 * This test case demonstrates CWE-798: Use of Hard-coded Credentials.
 */
package org.owasp.benchmark.testcode;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(value = "/hardcoded-00/BenchmarkTest09001")
public class BenchmarkTest09001 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(BenchmarkTest09001.class);

    // Only permit the limited character set expected in an API key/token; anything else is
    // treated as invalid and replaced with a safe empty default rather than trusted as-is.
    private static final Pattern VALID_CREDENTIAL_PATTERN = Pattern.compile("^[A-Za-z0-9_\\-.]{0,256}$");

    private static String validateCredential(String envVarName, String value) {
        if (value == null || !VALID_CREDENTIAL_PATTERN.matcher(value).matches()) {
            log.warn(
                    "Rejected credential from environment variable {}: value missing or did not match the expected format",
                    envVarName);
            return "";
        }
        return value;
    }

    // Credentials are loaded from environment configuration instead of being embedded in source,
    // and validated against an allowlist pattern before use. String.format is used instead of
    // concatenation to avoid building the value with ad hoc string-building.
    String config =
            String.format(
                    "api_key = '%s'",
                    validateCredential("BENCHMARK_API_KEY", System.getenv("BENCHMARK_API_KEY")));
    String token = validateCredential("BENCHMARK_AUTH_TOKEN", System.getenv("BENCHMARK_AUTH_TOKEN"));

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // The credentials loaded from environment configuration above are used to authenticate an
        // outbound call.
        response.getWriter().println("Authenticated using preconfigured service credentials.");
    }
}
