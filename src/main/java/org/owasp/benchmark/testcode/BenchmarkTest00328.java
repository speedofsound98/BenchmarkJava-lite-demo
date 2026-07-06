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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/sqli-00/BenchmarkTest00328")
public class BenchmarkTest00328 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Explicit allowlist of known-safe stored procedure names mapped to their expected
    // argument count. Only procedures registered here can be invoked, and the value is never
    // used to build the SQL statement text.
    private static final Map<String, Integer> ALLOWED_PROCEDURES;

    static {
        Map<String, Integer> procedures = new HashMap<>();
        procedures.put("verifyUserPassword", 2);
        procedures.put("verifyEmployeeSalary", 1);
        ALLOWED_PROCEDURES = Collections.unmodifiableMap(procedures);
    }

    private static final Pattern CALL_PATTERN = Pattern.compile("^([A-Za-z][A-Za-z0-9_]*)\\((.*)\\)$");
    private static final Pattern ARGS_LIST_PATTERN =
            Pattern.compile(
                    "^\\s*('(?:[^'\\\\]|\\\\.)*'\\s*(?:,\\s*'(?:[^'\\\\]|\\\\.)*'\\s*)*)?$");
    private static final Pattern ARG_PATTERN = Pattern.compile("'((?:[^'\\\\]|\\\\.)*)'");

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
        java.util.Enumeration<String> headers = request.getHeaders("BenchmarkTest00328");

        if (headers != null && headers.hasMoreElements()) {
            param = headers.nextElement(); // just grab first element
        }

        // URL Decode the header value since req.getHeaders() doesn't. Unlike req.getParameters().
        param = java.net.URLDecoder.decode(param, "UTF-8");

        String bar;

        // Simple ? condition that assigns param to bar on false condition
        int num = 106;

        bar = (7 * 42) - num > 200 ? "This should never happen" : param;

        // Only a procedure name registered in ALLOWED_PROCEDURES, invoked through a
        // parameterized CallableStatement, is executed. Anything else is rejected and results
        // in an empty result set rather than SQL built from untrusted input.
        Matcher callMatcher = CALL_PATTERN.matcher(bar.trim());
        String procedureName = callMatcher.matches() ? callMatcher.group(1) : null;
        String argsList = callMatcher.matches() ? callMatcher.group(2) : null;
        Integer expectedArgCount =
                procedureName != null ? ALLOWED_PROCEDURES.get(procedureName) : null;

        List<String> arguments = new ArrayList<>();
        boolean validArgs = expectedArgCount != null && argsList != null;
        if (validArgs) {
            if (expectedArgCount == 0) {
                validArgs = argsList.trim().isEmpty();
            } else if (ARGS_LIST_PATTERN.matcher(argsList).matches()) {
                Matcher argMatcher = ARG_PATTERN.matcher(argsList);
                while (argMatcher.find()) {
                    arguments.add(argMatcher.group(1).replace("\\'", "'").replace("\\\\", "\\"));
                }
                validArgs = arguments.size() == expectedArgCount;
            } else {
                validArgs = false;
            }
        }

        if (!validArgs) {
            // Invalid or non-allowlisted procedure/arguments: fail closed with an empty result.
            org.owasp.benchmark.helpers.DatabaseHelper.printResults(
                    (java.sql.ResultSet) null, "", response);
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < expectedArgCount; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        String sql = "{call " + procedureName + "(" + placeholders + ")}";

        try {
            java.sql.Connection connection =
                    org.owasp.benchmark.helpers.DatabaseHelper.getSqlConnection();
            try (java.sql.CallableStatement statement = connection.prepareCall(sql)) {
                for (int i = 0; i < arguments.size(); i++) {
                    statement.setString(i + 1, arguments.get(i));
                }
                java.sql.ResultSet rs = statement.executeQuery();
                org.owasp.benchmark.helpers.DatabaseHelper.printResults(rs, sql, response);
            }

        } catch (java.sql.SQLException e) {
            if (org.owasp.benchmark.helpers.DatabaseHelper.hideSQLErrors) {
                response.getWriter().println("Error processing request.");
                return;
            } else throw new ServletException(e);
        }
    }
}
