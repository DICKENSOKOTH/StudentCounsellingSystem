/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Faith Varaidzo
 */
public class DBConnection {
    private static final String URL ="jdbc:mysql://localhost:3306/student_counselling_db";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());

    return null;
            
        }
    }
}
