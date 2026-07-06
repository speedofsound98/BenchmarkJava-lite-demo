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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/hardcoded-00/BenchmarkTest09001")
public class BenchmarkTest09001 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Credentials are loaded from environment configuration instead of being embedded in source.
    String config = "api_key = '" + System.getenv("BENCHMARK_API_KEY") + "'";
    String token = System.getenv("BENCHMARK_AUTH_TOKEN");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // The hard-coded credentials above are used to authenticate an outbound call.
        response.getWriter().println("Authenticated using preconfigured service credentials.");
    }
}
