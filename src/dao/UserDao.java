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
    public boolean registerStudent(String fullName, String email, String phone, String password,
                                   String regNo, String course, int year) throws SQLException {
        String userSql = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, 'STUDENT')";
        String studentSql = "INSERT INTO students (user_id, registration_number, course, year_of_study) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("No database connection");
            c.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = c.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, fullName);
                    ps.setString(2, email.trim().toLowerCase());
                    ps.setString(3, phone);
                    ps.setString(4, hash(password));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); userId = keys.getInt(1); }
                }
                try (PreparedStatement ps = c.prepareStatement(studentSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, regNo);
                    ps.setString(3, course);
                    ps.setInt(4, year);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                c.rollback();
                return false;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
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