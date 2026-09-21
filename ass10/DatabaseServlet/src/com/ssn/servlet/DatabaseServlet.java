package com.ssn.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

// Flow: browser submits the GET form in index.html -> Tomcat routes the
// request here based on the <url-pattern> in web.xml -> doGet() opens a
// JDBC connection to MySQL, runs the query, and writes the results as HTML.
public class DatabaseServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/testdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h1>Employee Records</h1>");
        out.println("<table border='1' cellpadding='6'>");
        out.println("<tr><th>ID</th><th>Age</th><th>First Name</th><th>Last Name</th></tr>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // registers the MySQL JDBC driver
        } catch (ClassNotFoundException e) {
            out.println("</table><p>Driver not found: " + e.getMessage() + "</p></body></html>");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Employees")) {

            while (rs.next()) {
                out.println("<tr>"
                        + "<td>" + rs.getInt("id") + "</td>"
                        + "<td>" + rs.getInt("age") + "</td>"
                        + "<td>" + rs.getString("first") + "</td>"
                        + "<td>" + rs.getString("last") + "</td>"
                        + "</tr>");
            }
        } catch (Exception e) {
            out.println("</table>");
            out.println("<p>Database error: " + e.getMessage() + "</p>");
            out.println("</body></html>");
            return;
        }

        out.println("</table>");
        out.println("</body></html>");
    }
}
