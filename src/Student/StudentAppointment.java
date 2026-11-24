/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Student;
/**
 *
 * @author user
 */
import java.time.LocalDate;
import java.time.LocalTime;

public class StudentAppointment {

    private int appointmentId;
    private int studentId;
    private String studentName;
    private int counsellorId;
    private String counsellorName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private String status;
    private String studentRegNo = "";
    private int rating = 0;              // 0 = no feedback given yet
    private String feedbackComment = "";

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    // Original 8-argument constructor (kept so existing callers still work)
    public StudentAppointment(
            int appointmentId,
            int studentId,
            int counsellorId,
            String counsellorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason,
            String status) {

        this(appointmentId, studentId, "", counsellorId, counsellorName,
                appointmentDate, appointmentTime, reason, status);
    }

    public StudentAppointment(
            int appointmentId,
            int studentId,
            String studentName,
            int counsellorId,
            String counsellorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason,
            String status) {

        this.appointmentId = appointmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.counsellorId = counsellorId;
        this.counsellorName = counsellorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    /** Name the student gave when booking (empty for older records). */
    public String getStudentName() {
        return studentName == null ? "" : studentName;
    }

    public int getCounsellorId() {
        return counsellorId;
    }

    public String getCounsellorName() {
        return counsellorName;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    /** The student's registration number (their Student ID). */
    public String getStudentRegNo() {
        return studentRegNo == null ? "" : studentRegNo;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentRegNo = studentRegNo;
    }

    /** Feedback rating from 1 to 5, or 0 when none has been given. */
    public int getRating() {
        return rating;
    }

    public String getFeedbackComment() {
        return feedbackComment == null ? "" : feedbackComment;
    }

    public void setFeedback(int rating, String comment) {
        this.rating = rating;
        this.feedbackComment = comment;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
