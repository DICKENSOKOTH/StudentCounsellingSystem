/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Student;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All database access for the student dashboard (MySQL / MariaDB).
 *
 * Every method takes the LOGGED-IN USER'S ID (users.user_id), which is what
 * the dashboard holds. The appointments table stores students.student_id and
 * counsellors.counsellor_id, so each query joins through the students table.
 * Only the existing tables are used; no table is created or altered here.
 *
 * Dates and times are sent and read as text ("2026-12-01", "09:00") so the
 * result never shifts with the computer's time zone.
 *
 * Failures throw StudentDatabaseException with a message fit for the student.
 */
public final class StudentAppointmentDao {

    private static final String ACTIVE = "('PENDING','APPROVED')";

    private static final String SELECT_APPOINTMENT
            = "SELECT a.appointment_id, s.user_id, s.registration_number, "
            + "u.full_name AS student_name, a.counsellor_id, "
            + "cu.full_name AS counsellor_name, a.appointment_date, "
            + "a.appointment_time, a.reason, a.status, "
            + "f.rating, f.comments "
            + "FROM appointments a "
            + "JOIN students s ON s.student_id = a.student_id "
            + "JOIN users u ON u.user_id = s.user_id "
            + "JOIN counsellors c ON c.counsellor_id = a.counsellor_id "
            + "JOIN users cu ON cu.user_id = c.user_id "
            + "LEFT JOIN feedback f ON f.appointment_id = a.appointment_id ";

    private StudentAppointmentDao() {
    }

    // ------------------------------------------------------------------
    // Connection helpers
    // ------------------------------------------------------------------
    private static Connection open() {

        Connection c = DBConnection.getConnection();
        if (c == null) {
            throw new StudentDatabaseException("Could not reach the database. "
                    + "Check that MySQL is running in XAMPP.");
        }
        return c;
    }

    private static StudentDatabaseException failure(SQLException e) {
        return new StudentDatabaseException(
                "The database reported a problem: " + e.getMessage(), e);
    }

    /** 1062 = duplicate key (MySQL and MariaDB); SQLState 23000 family. */
    private static boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 1062
                || (e.getSQLState() != null
                && e.getSQLState().startsWith("23")
                && String.valueOf(e.getMessage()).toLowerCase()
                        .contains("duplicate"));
    }

    private static String sqlTime(LocalTime t) {
        return String.format("%02d:%02d:00", t.getHour(), t.getMinute());
    }

    private static StudentAppointment readAppointment(ResultSet rs)
            throws SQLException {

        String date = rs.getString("appointment_date");
        String time = rs.getString("appointment_time");

        StudentAppointment a = new StudentAppointment(
                rs.getInt("appointment_id"),
                rs.getInt("user_id"),
                rs.getString("student_name"),
                rs.getInt("counsellor_id"),
                rs.getString("counsellor_name"),
                LocalDate.parse(date.substring(0, 10)),
                LocalTime.parse(time.substring(0, 5)),
                rs.getString("reason"),
                rs.getString("status"));

        a.setStudentRegNo(rs.getString("registration_number"));
        int rating = rs.getInt("rating");
        if (!rs.wasNull()) {
            a.setFeedback(rating, rs.getString("comments"));
        }
        return a;
    }

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------
    /** Every counsellor in the system, for the booking form. */
    public static List<StudentCounsellor> getCounsellors() {

        String sql = "SELECT c.counsellor_id, u.full_name, c.specialization "
                + "FROM counsellors c JOIN users u ON u.user_id = c.user_id "
                + "ORDER BY u.full_name";

        List<StudentCounsellor> list = new ArrayList<>();

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new StudentCounsellor(rs.getInt(1),
                        rs.getString(2), rs.getString(3)));
            }
            return list;

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    /** The student's registration number (Student ID), or "" if none. */
    public static String getRegistrationNumber(int userId) {

        String sql = "SELECT registration_number FROM students "
                + "WHERE user_id = ?";

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    public static List<StudentAppointment> getAppointmentsForStudent(
            int userId) {

        String sql = SELECT_APPOINTMENT + "WHERE s.user_id = ? "
                + "ORDER BY a.appointment_date, a.appointment_time";

        List<StudentAppointment> list = new ArrayList<>();

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readAppointment(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    public static StudentAppointment getAppointmentById(int appointmentId,
            int userId) {

        try (Connection c = open()) {
            return getAppointmentById(c, appointmentId, userId);
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static StudentAppointment getAppointmentById(Connection c,
            int appointmentId, int userId) throws SQLException {

        String sql = SELECT_APPOINTMENT
                + "WHERE a.appointment_id = ? AND s.user_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readAppointment(rs) : null;
            }
        }
    }

    public static boolean isSlotTaken(int counsellorId, LocalDate date,
            LocalTime time) {

        try (Connection c = open()) {
            return slotTaken(c, counsellorId, date, time, -1);
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static boolean slotTaken(Connection c, int counsellorId,
            LocalDate date, LocalTime time, int excludeAppointmentId)
            throws SQLException {

        String sql = "SELECT 1 FROM appointments WHERE counsellor_id = ? "
                + "AND appointment_date = ? AND appointment_time = ? "
                + "AND status IN " + ACTIVE
                + " AND appointment_id <> ? LIMIT 1";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, counsellorId);
            ps.setString(2, date.toString());
            ps.setString(3, sqlTime(time));
            ps.setInt(4, excludeAppointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean hasStudentConflict(int userId, LocalDate date,
            LocalTime time, int excludeAppointmentId) {

        try (Connection c = open()) {
            return studentConflict(c, userId, date, time,
                    excludeAppointmentId);
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static boolean studentConflict(Connection c, int userId,
            LocalDate date, LocalTime time, int excludeAppointmentId)
            throws SQLException {

        String sql = "SELECT 1 FROM appointments a "
                + "JOIN students s ON s.student_id = a.student_id "
                + "WHERE s.user_id = ? AND a.appointment_date = ? "
                + "AND a.appointment_time = ? AND a.status IN " + ACTIVE
                + " AND a.appointment_id <> ? LIMIT 1";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setString(3, sqlTime(time));
            ps.setInt(4, excludeAppointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Every time on the given day that the counsellor OR the student is
     * already booked for. One query instead of one per time slot.
     */
    public static Set<LocalTime> getUnavailableTimes(int counsellorId,
            int userId, LocalDate date, int excludeAppointmentId) {

        String sql = "SELECT a.appointment_time FROM appointments a "
                + "JOIN students s ON s.student_id = a.student_id "
                + "WHERE a.appointment_date = ? AND a.status IN " + ACTIVE
                + " AND a.appointment_id <> ? "
                + "AND (a.counsellor_id = ? OR s.user_id = ?)";

        Set<LocalTime> busy = new HashSet<>();

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ps.setInt(2, excludeAppointmentId);
            ps.setInt(3, counsellorId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    busy.add(LocalTime.parse(rs.getString(1).substring(0, 5)));
                }
            }
            return busy;

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    // ------------------------------------------------------------------
    // Booking
    // ------------------------------------------------------------------
    /**
     * Saves a new PENDING appointment. The rules (future weekday, working
     * hours) are checked by the caller; clashes are checked here and, as a
     * last line of defence, by the database's unique_active_slot key.
     *
     * @throws IllegalArgumentException with a student-friendly message
     */
    public static StudentAppointment bookAppointment(int userId,
            int counsellorId, LocalDate date, LocalTime time, String reason) {

        try (Connection c = open()) {

            int studentRecord = studentRecordId(c, userId);
            if (studentRecord < 0) {
                throw new IllegalArgumentException(
                        "No student record was found for your account.");
            }

            if (slotTaken(c, counsellorId, date, time, -1)) {
                throw new IllegalArgumentException(
                        "This counsellor already has an appointment "
                        + "at that time.");
            }
            if (studentConflict(c, userId, date, time, -1)) {
                throw new IllegalArgumentException(
                        "You already have an appointment at that time.");
            }

            String insert = "INSERT INTO appointments (student_id, "
                    + "counsellor_id, appointment_date, appointment_time, "
                    + "reason) VALUES (?, ?, ?, ?, ?)";

            int newId;
            try (PreparedStatement ps = c.prepareStatement(insert,
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, studentRecord);
                ps.setInt(2, counsellorId);
                ps.setString(3, date.toString());
                ps.setString(4, sqlTime(time));
                ps.setString(5, reason);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    newId = keys.getInt(1);
                }

            } catch (SQLException e) {
                if (isDuplicate(e)) {
                    throw new IllegalArgumentException(
                            "This counsellor already has an appointment "
                            + "at that time.");
                }
                if (e.getErrorCode() == 1452) {
                    throw new IllegalArgumentException(
                            "That counsellor could not be found.");
                }
                throw e;
            }

            return getAppointmentById(c, newId, userId);

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static int studentRecordId(Connection c, int userId)
            throws SQLException {

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT student_id FROM students WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // ------------------------------------------------------------------
    // Cancel / reschedule
    // ------------------------------------------------------------------
    public static boolean cancelAppointment(int appointmentId, int userId) {

        String sql = "UPDATE appointments a "
                + "JOIN students s ON s.student_id = a.student_id "
                + "SET a.status = 'CANCELLED' "
                + "WHERE a.appointment_id = ? AND s.user_id = ? "
                + "AND a.status IN " + ACTIVE;

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    /** @return why the appointment cannot move to that slot, or null */
    public static String checkReschedule(int appointmentId, int userId,
            LocalDate newDate, LocalTime newTime) {

        try (Connection c = open()) {
            return rescheduleProblem(c, appointmentId, userId,
                    newDate, newTime);
        } catch (SQLException e) {
            throw failure(e);
        }
    }

    private static String rescheduleProblem(Connection c, int appointmentId,
            int userId, LocalDate newDate, LocalTime newTime)
            throws SQLException {

        StudentAppointment a = getAppointmentById(c, appointmentId, userId);

        if (a == null) {
            return "That appointment could not be found.";
        }
        if (!StudentAppointmentStore.isActive(a)) {
            return "Only pending or approved appointments can be rescheduled.";
        }

        String slotError = StudentAppointmentStore.validateSlot(
                newDate, newTime);
        if (slotError != null) {
            return slotError;
        }

        if (a.getAppointmentDate().equals(newDate)
                && a.getAppointmentTime().equals(newTime)) {
            return "That is already the time of this appointment.";
        }
        if (slotTaken(c, a.getCounsellorId(), newDate, newTime,
                appointmentId)) {
            return "This counsellor already has an appointment at that time.";
        }
        if (studentConflict(c, userId, newDate, newTime, appointmentId)) {
            return "You already have an appointment at that time.";
        }
        return null;
    }

    /** Moves the appointment; it returns to PENDING for re-approval. */
    public static boolean rescheduleAppointment(int appointmentId,
            int userId, LocalDate newDate, LocalTime newTime) {

        String sql = "UPDATE appointments a "
                + "JOIN students s ON s.student_id = a.student_id "
                + "SET a.appointment_date = ?, a.appointment_time = ?, "
                + "a.status = 'PENDING' "
                + "WHERE a.appointment_id = ? AND s.user_id = ? "
                + "AND a.status IN " + ACTIVE;

        try (Connection c = open()) {

            if (rescheduleProblem(c, appointmentId, userId,
                    newDate, newTime) != null) {
                return false;
            }

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, newDate.toString());
                ps.setString(2, sqlTime(newTime));
                ps.setInt(3, appointmentId);
                ps.setInt(4, userId);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                if (isDuplicate(e)) {
                    return false;   // someone took the slot a moment ago
                }
                throw e;
            }

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    // ------------------------------------------------------------------
    // Feedback
    // ------------------------------------------------------------------
    /**
     * Saves feedback for a COMPLETED appointment of this student.
     *
     * @return an error message, or null when it was saved
     */
    public static String saveFeedback(int appointmentId, int userId,
            int rating, String comments) {

        if (rating < 1 || rating > 5) {
            return "Please choose a rating from 1 to 5 stars.";
        }

        try (Connection c = open()) {

            StudentAppointment a = getAppointmentById(c, appointmentId,
                    userId);

            if (a == null) {
                return "That appointment could not be found.";
            }
            if (!StudentAppointment.COMPLETED.equals(a.getStatus())) {
                return "Feedback can only be given for completed "
                        + "appointments.";
            }
            if (a.getRating() > 0) {
                return "You have already given feedback for this "
                        + "appointment.";
            }

            String sql = "INSERT INTO feedback (appointment_id, rating, "
                    + "comments) VALUES (?, ?, ?)";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, rating);
                ps.setString(3, comments == null || comments.isBlank()
                        ? null : comments.trim());
                ps.executeUpdate();
                return null;

            } catch (SQLException e) {
                if (isDuplicate(e)) {
                    return "You have already given feedback for this "
                            + "appointment.";
                }
                throw e;
            }

        } catch (SQLException e) {
            throw failure(e);
        }
    }

    // ------------------------------------------------------------------
    // Profile and account
    // ------------------------------------------------------------------
    /**
     * Saves name, email and phone to the users table.
     *
     * @return an error message, or null when it was saved
     */
    public static String updateProfile(int userId, String fullName,
            String email, String phone) {

        String cleanEmail = email.trim().toLowerCase();
        String cleanPhone = phone.replaceAll("[\\s-]", "");

        if (fullName.length() > 100 || cleanEmail.length() > 100) {
            return "The name or email is too long.";
        }
        if (cleanPhone.length() > 20) {
            return "The phone number is too long.";
        }

        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? "
                + "WHERE user_id = ?";

        try (Connection c = open();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, cleanEmail);
            ps.setString(3, cleanPhone.isEmpty() ? null : cleanPhone);
            ps.setInt(4, userId);

            if (ps.executeUpdate() == 0) {
                return "Your account could not be found.";
            }
            return null;

        } catch (SQLException e) {
            if (isDuplicate(e)) {
                return "That email is already registered.";
            }
            throw failure(e);
        }
    }

    /**
     * Permanently deletes the student: their appointments (feedback and
     * session notes go with them), then the user row (the students row goes
     * with it). All or nothing.
     *
     * @return how many appointments were removed
     */
    public static int deleteAccount(int userId) {

        String deleteAppointments = "DELETE a FROM appointments a "
                + "JOIN students s ON s.student_id = a.student_id "
                + "WHERE s.user_id = ?";

        try (Connection c = open()) {

            c.setAutoCommit(false);

            try {
                int removed;
                try (PreparedStatement ps
                        = c.prepareStatement(deleteAppointments)) {
                    ps.setInt(1, userId);
                    removed = ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM users WHERE user_id = ? "
                        + "AND role = 'STUDENT'")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                c.commit();
                return removed;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw failure(e);
        }
    }
}
