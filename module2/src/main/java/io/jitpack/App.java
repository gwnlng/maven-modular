package io.jitpack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static String GREETING = "Hello World!";
    
    public static void main( String[] args )
    {
        System.out.println( GREETING );
        try (Scanner scanner = new Scanner(System.in);
             Connection conn = getConnection()) {
            System.out.print("Enter a user name to search: ");
            String userInput = scanner.nextLine();
            String sanitizedInput = sanitizeUserInput(userInput);
            String myName = findUserByName(conn, sanitizedInput);
            System.out.println("Found user: " + myName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/mydb";
        String user = System.getProperty("DB_USER", "postgres");
        String password = System.getProperty("DB_PASSWORD", "password");
        return DriverManager.getConnection(url, user, password);
    }

    public static String sanitizeUserInput(String input) {
        if (input == null) {
            return null;
        }
        // Basic sanitization: escape single quotes to reduce SQL injection risk.
        //return input.replace("'", "''");
        if (input.contains("'")) {
            throw new IllegalArgumentException("Input contains invalid characters.");
        }
        return input;
    }

    /**
     * Sample SQL injection vulnerable method for testing.
     * This intentionally concatenates raw user input into SQL.
     */
    public static String findUserByName(Connection connection, String userInput) throws SQLException {
        String sql = "SELECT id, name FROM mytable WHERE name = '" + userInput + "'";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        }
        return null;
    }
}
