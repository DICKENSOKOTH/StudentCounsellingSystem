package dao;

import database.DBConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import models.User;

public class UserDao {

    /** Returns the user if email and password match, otherwise null. Throws if the DB is unreachable.
     * @param email
     * @param password
     * @return 
     * @throws java.sql.SQLException */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role FROM users WHERE email = ?";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("No database connection");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString("password_hash").equals(hash(password))) {
                        return new User(rs.getInt("user_id"), rs.getString("full_name"),
                                rs.getString("email"), rs.getString("phone"),
                                rs.getString("password_hash"), rs.getString("role"));
                    }
                }
            }
        }
        return null;
    }

    /** Creates the users row and the students row together. Returns false if the email or reg number exists. */
    /** Returns null on success, or an error message. */
public String registerStudent(String fullName, String email, String phone, String password,
                              String regNo, String course, int year) throws SQLException {
    try (Connection c = DBConnection.getConnection()) {
        if (c == null) throw new SQLException("No database connection");
        c.setAutoCommit(false);
        try {
            int userId = insertUser(c, fullName, email, phone, password, "STUDENT");
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO students (user_id, registration_number, course, year_of_study) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setString(2, regNo);
                ps.setString(3, course);
                ps.setInt(4, year);
                ps.executeUpdate();
            }
            c.commit();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            c.rollback();
            return duplicateMessage(e);
        } catch (SQLException e) {
            c.rollback();
            throw e;
        }
    }
}


public String registerCounsellor(String fullName, String email, String phone, String password,
                                 String qualification, String religion, String gender) throws SQLException {
    try (Connection c = DBConnection.getConnection()) {
        if (c == null) throw new SQLException("No database connection");
        c.setAutoCommit(false);
        try {
            int userId = insertUser(c, fullName, email, phone, password, "COUNSELLOR");
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO counsellors (user_id, qualification, religion, gender) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setString(2, qualification);
                ps.setString(3, religion);
                ps.setString(4, gender);
                ps.executeUpdate();
            }
            c.commit();
            return null;
        } catch (SQLIntegrityConstraintViolationException e) {
            c.rollback();
            return duplicateMessage(e);
        } catch (SQLException e) {
            c.rollback();
            throw e;
        }
    }
}

private int insertUser(Connection c, String fullName, String email, String phone,
                       String password, String role) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, fullName);
        ps.setString(2, email.trim().toLowerCase());
        ps.setString(3, phone.isEmpty() ? null : phone);
        ps.setString(4, hash(password));
        ps.setString(5, role);
        ps.executeUpdate();
        try (ResultSet keys = ps.getGeneratedKeys()) {
            keys.next();
            return keys.getInt(1);
        }
    }
}

private String duplicateMessage(SQLIntegrityConstraintViolationException e) {
    String m = String.valueOf(e.getMessage()).toLowerCase();
    if (m.contains("email")) return "That email is already registered.";
    if (m.contains("registration")) return "That student ID is already registered.";
    return "That account already exists.";
}

    private static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}