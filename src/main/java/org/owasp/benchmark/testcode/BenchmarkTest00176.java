/**
 * OWASP Benchmark Project v1.2
 *
 * <p>This file is part of the Open Web Application Security Project (OWASP) Benchmark Project. For
 * details, please see <a
 * href="https://owasp.org/www-project-benchmark/">https://owasp.org/www-project-benchmark/</a>.
 *
 * <p>The OWASP Benchmark is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, version 2.
 *
 * <p>The OWASP Benchmark is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * @author Nick Sanidas
 * @created 2015
 */
package org.owasp.benchmark.testcode;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/cmdi-00/BenchmarkTest00176")
public class BenchmarkTest00176 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String param = "";
        if (request.getHeader("BenchmarkTest00176") != null) {
            param = request.getHeader("BenchmarkTest00176");
        }

        // URL Decode the header value since req.getHeader() doesn't. Unlike req.getParameter().
        param = java.net.URLDecoder.decode(param, "UTF-8");

        String bar = param;

        String osName = System.getProperty("os.name");
        String[] cmdArray;
        if (osName.indexOf("Windows") != -1) {
            // cmd.exe /c is a shell interpreter, so the argument it consumes must be
            // strictly allowlisted to prevent shell metacharacter interpretation.
            // Fail closed to a safe empty value and log the rejection (without the
            // raw value) when the input does not match.
            String safeBar = bar;
            if (!safeBar.matches("[a-zA-Z0-9 ]*")) {
                System.out.println(
                        "WARNING: rejected invalid input for shell-consumed command argument (length="
                                + safeBar.length() + ")");
                safeBar = "";
            }
            cmdArray = new String[] {"cmd.exe", "/c", "echo", safeBar};
        } else {
            // No shell is invoked on this path; "echo" is executed directly as the
            // process argv[0] with bar passed as a literal argument, so shell
            // metacharacters in bar cannot be interpreted.
            cmdArray = new String[] {"echo", bar};
        }

        String[] argsEnv = {"Foo=bar"};
        Runtime r = Runtime.getRuntime();

        try {
            Process p =
                    r.exec(cmdArray, argsEnv, new java.io.File(System.getProperty("user.dir")));
            org.owasp.benchmark.helpers.Utils.printOSCommandResults(p, response);
        } catch (IOException e) {
            System.out.println("Problem executing cmdi - TestCase");
            response.getWriter()
                    .println(org.owasp.esapi.ESAPI.encoder().encodeForHTML(e.getMessage()));
            return;
        }
    }
}
