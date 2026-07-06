/**
 * OWASP Benchmark v1.2
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
 * @author Dave Wichers
 * @created 2015
 */
package org.owasp.benchmark.testcode;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/sqli-00/BenchmarkTest00008")
public class BenchmarkTest00008 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(BenchmarkTest00008.class.getName());

    // Allowlist of stored procedure names that this endpoint is permitted to invoke.
    // The database schema (see DatabaseHelper.initDataBase()) only defines these procedures.
    private static final java.util.Set<String> ALLOWED_PROCEDURES =
            new java.util.HashSet<>(
                    java.util.Arrays.asList("verifyUserPassword", "verifyEmployeeSalary"));

    // Matches the overall "procedureName(arg1, arg2, ...)" call syntax.
    private static final java.util.regex.Pattern CALL_PATTERN =
            java.util.regex.Pattern.compile("^\\s*(\\w+)\\s*\\((.*)\\)\\s*$");

    // Requires the argument list to be empty or a strict comma-separated sequence of
    // single-quoted string literals (with '' as the only allowed embedded-quote escape).
    // Commas and escaped quotes inside a quoted literal are therefore not treated as
    // list separators, closing the ambiguity that made the previous split-on-comma
    // parsing bypassable.
    private static final java.util.regex.Pattern ARGS_LIST_PATTERN =
            java.util.regex.Pattern.compile(
                    "^\\s*(?:'(?:[^']|'')*'\\s*(?:,\\s*'(?:[^']|'')*'\\s*)*)?$");

    // Extracts each individual quoted literal from an argument list already validated
    // by ARGS_LIST_PATTERN.
    private static final java.util.regex.Pattern ARG_PATTERN =
            java.util.regex.Pattern.compile("'((?:[^']|'')*)'");

    private static String sanitizeForLog(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]", "_");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // some code
        response.setContentType("text/html;charset=UTF-8");

        String param = "";
        if (request.getHeader("BenchmarkTest00008") != null) {
            param = request.getHeader("BenchmarkTest00008");
        }

        // URL Decode the header value since req.getHeader() doesn't. Unlike req.getParameter().
        param = java.net.URLDecoder.decode(param, "UTF-8");

        // Parse the expected "procedureName('arg1','arg2', ...)" call syntax so the
        // procedure name can be checked against the allowlist and the arguments can be
        // bound as parameters instead of concatenated into the SQL call string.
        java.util.regex.Matcher callMatcher = CALL_PATTERN.matcher(param);
        if (!callMatcher.matches() || !ALLOWED_PROCEDURES.contains(callMatcher.group(1))) {
            LOGGER.warning(
                    "Rejected stored procedure call request with invalid or disallowed "
                            + "procedure name: "
                            + sanitizeForLog(param));
            response.getWriter().println("Error processing request.");
            return;
        }
        String procedureName = callMatcher.group(1);
        String argsPart = callMatcher.group(2).trim();

        if (!ARGS_LIST_PATTERN.matcher(argsPart).matches()) {
            LOGGER.warning(
                    "Rejected stored procedure call request with invalid argument list: "
                            + sanitizeForLog(param));
            response.getWriter().println("Error processing request.");
            return;
        }

        java.util.List<String> args = new java.util.ArrayList<>();
        java.util.regex.Matcher argMatcher = ARG_PATTERN.matcher(argsPart);
        while (argMatcher.find()) {
            args.add(argMatcher.group(1).replace("''", "'"));
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "{call " + procedureName + "(" + placeholders + ")}";

        try (java.sql.Connection connection =
                        org.owasp.benchmark.helpers.DatabaseHelper.getSqlConnection();
                java.sql.CallableStatement statement = connection.prepareCall(sql)) {
            for (int i = 0; i < args.size(); i++) {
                statement.setString(i + 1, args.get(i));
            }
            java.sql.ResultSet rs = statement.executeQuery();
            org.owasp.benchmark.helpers.DatabaseHelper.printResults(rs, sql, response);

        } catch (java.sql.SQLException e) {
            if (org.owasp.benchmark.helpers.DatabaseHelper.hideSQLErrors) {
                response.getWriter().println("Error processing request.");
                return;
            } else throw new ServletException(e);
        }
    }
}
