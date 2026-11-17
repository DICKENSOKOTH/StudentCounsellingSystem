/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forms;
import database.DBConnection;
import java.sql.Connection;

/**
 *
 * @author Faith Varaidzo
 */
public class TestConnection {
       public static void main(String[] args) {

        Connection connection = DBConnection.getConnection();

        if (connection != null) {
            System.out.println("Successfully connected to MySQL!");
        } else {
            System.out.println("Connection unsuccessful.");
        }
    }
}
