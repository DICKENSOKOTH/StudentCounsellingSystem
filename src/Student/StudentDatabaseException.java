/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Student;

/**
 * Thrown when the student dashboard cannot talk to the database. The
 * message is written for the student and can be shown as it is.
 */
public class StudentDatabaseException extends RuntimeException {

    public StudentDatabaseException(String message) {
        super(message);
    }

    public StudentDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
