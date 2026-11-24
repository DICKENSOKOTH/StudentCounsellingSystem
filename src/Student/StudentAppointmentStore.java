/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Student;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The single entry point the student dashboard uses for appointment data.
 *
 * By default every call goes to the MySQL database through
 * StudentAppointmentDao. The old in-memory store is kept behind
 * setUseDatabase(false) for offline demos and automated tests; its data is
 * lost when the app closes. The studentId arguments are always the logged-in
 * user's ID (users.user_id).
 *
 * @author user
 */
public class StudentAppointmentStore {

    // Booking rules (kept here so every screen enforces the same ones)
    public static final LocalTime FIRST_SLOT = LocalTime.of(9, 0);
    public static final LocalTime LAST_SLOT = LocalTime.of(16, 30);
    public static final int SLOT_MINUTES = 30;

    private static final List<StudentAppointment> appointments
            = new ArrayList<>();

    private static int nextAppointmentId = 1;

    private static boolean useDatabase = true;

    /** true (default): MySQL. false: temporary in-memory data (demos/tests). */
    public static void setUseDatabase(boolean enabled) {
        useDatabase = enabled;
    }

    public static boolean isUsingDatabase() {
        return useDatabase;
    }

    // in-memory only: placeholder counsellors and feedback
    private static final StudentCounsellor[] MEMORY_COUNSELLORS = {
        new StudentCounsellor(101, "Dr. Grace Wanjiru", "Academic support"),
        new StudentCounsellor(102, "Mr. David Kimani", "Mental wellbeing"),
        new StudentCounsellor(103, "Ms. Sarah Mwangi", "Career guidance")
    };

    // ------------------------------------------------------------------
    // Rules / helpers
    // ------------------------------------------------------------------

    /**
     * Checks a date/time pair against the booking rules.
     *
     * @return an error message, or null when the slot is acceptable
     */
    public static String validateSlot(LocalDate date, LocalTime time) {

        if (date == null) {
            return "Please select an appointment date.";
        }
        if (time == null) {
            return "Please select an appointment time.";
        }
        if (!date.isAfter(LocalDate.now())) {
            return "Please select a future date. "
                    + "Same-day bookings are not available.";
        }
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return "Appointments are only available on weekdays.";
        }
        if (time.isBefore(FIRST_SLOT) || time.isAfter(LAST_SLOT)
                || time.getMinute() % SLOT_MINUTES != 0
                || time.getSecond() != 0 || time.getNano() != 0) {
            return "Appointments run from 09:00 to 17:00 in "
                    + SLOT_MINUTES + "-minute slots.";
        }
        return null;
    }

    /** PENDING or APPROVED: the appointment still occupies a slot. */
    public static boolean isActive(StudentAppointment appointment) {
        return appointment.getStatus().equals(StudentAppointment.PENDING)
                || appointment.getStatus().equals(StudentAppointment.APPROVED);
    }

    /** Active and not in the past. Single definition used for the counts. */
    public static boolean isUpcoming(StudentAppointment appointment) {
        return isActive(appointment)
                && !appointment.getAppointmentDate().isBefore(LocalDate.now());
    }

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    // Book a new appointment (name-less version kept for older callers)
    public static synchronized StudentAppointment bookAppointment(
            int studentId,
            int counsellorId,
            String counsellorName,
            LocalDate date,
            LocalTime time,
            String reason) {

        return bookAppointment(studentId, "", counsellorId, counsellorName,
                date, time, reason);
    }

    // Book a new appointment, recording the name the student typed
    public static synchronized StudentAppointment bookAppointment(
            int studentId,
            String studentName,
            int counsellorId,
            String counsellorName,
            LocalDate date,
            LocalTime time,
            String reason) {

        if (date == null || time == null || reason == null
                || reason.isBlank()
                || (!useDatabase
                && (counsellorName == null || counsellorName.isBlank()))) {
            throw new IllegalArgumentException(
                    "Please complete all appointment details.");
        }

        if (studentName == null) {
            studentName = "";
        }

        String slotError = validateSlot(date, time);
        if (slotError != null) {
            throw new IllegalArgumentException(slotError);
        }

        if (useDatabase) {
            // the name is the account's name; the database stores no copy
            return StudentAppointmentDao.bookAppointment(studentId,
                    counsellorId, date, time, reason.trim());
        }

        if (isSlotTaken(counsellorId, date, time)) {
            throw new IllegalArgumentException(
                    "This counsellor already has an appointment at that time.");
        }

        if (hasStudentConflict(studentId, date, time, -1)) {
            throw new IllegalArgumentException(
                    "You already have an appointment at that time.");
        }

        StudentAppointment appointment = new StudentAppointment(
                nextAppointmentId++,
                studentId,
                studentName.trim(),
                counsellorId,
                counsellorName,
                date,
                time,
                reason.trim(),
                StudentAppointment.PENDING
        );

        appointment.setStudentRegNo("STU-DEMO");
        appointments.add(appointment);

        return appointment;
    }

    // Retrieve appointments belonging to one student
    public static synchronized List<StudentAppointment>
            getAppointmentsForStudent(int studentId) {

        if (useDatabase) {
            return StudentAppointmentDao.getAppointmentsForStudent(studentId);
        }


        List<StudentAppointment> result = new ArrayList<>();

        for (StudentAppointment appointment : appointments) {
            if (appointment.getStudentId() == studentId) {
                result.add(appointment);
            }
        }

        return result;
    }

    // Find one appointment that belongs to the given student (or null)
    public static synchronized StudentAppointment getAppointmentById(
            int appointmentId, int studentId) {

        if (useDatabase) {
            return StudentAppointmentDao.getAppointmentById(
                    appointmentId, studentId);
        }


        for (StudentAppointment appointment : appointments) {
            if (appointment.getAppointmentId() == appointmentId
                    && appointment.getStudentId() == studentId) {
                return appointment;
            }
        }
        return null;
    }

    // Check whether a counsellor's time slot is occupied
    public static synchronized boolean isSlotTaken(
            int counsellorId,
            LocalDate date,
            LocalTime time) {

        if (useDatabase) {
            return StudentAppointmentDao.isSlotTaken(
                    counsellorId, date, time);
        }


        for (StudentAppointment appointment : appointments) {

            boolean sameCounsellor
                    = appointment.getCounsellorId() == counsellorId;

            boolean sameDate
                    = appointment.getAppointmentDate().equals(date);

            boolean sameTime
                    = appointment.getAppointmentTime().equals(time);

            if (sameCounsellor && sameDate && sameTime
                    && isActive(appointment)) {
                return true;
            }
        }

        return false;
    }

    // Check whether the STUDENT already has an active appointment then
    // (with any counsellor). excludeAppointmentId lets a reschedule ignore
    // the appointment being moved; pass -1 otherwise.
    public static synchronized boolean hasStudentConflict(
            int studentId,
            LocalDate date,
            LocalTime time,
            int excludeAppointmentId) {

        if (useDatabase) {
            return StudentAppointmentDao.hasStudentConflict(
                    studentId, date, time, excludeAppointmentId);
        }


        for (StudentAppointment appointment : appointments) {
            if (appointment.getStudentId() == studentId
                    && appointment.getAppointmentId() != excludeAppointmentId
                    && appointment.getAppointmentDate().equals(date)
                    && appointment.getAppointmentTime().equals(time)
                    && isActive(appointment)) {
                return true;
            }
        }
        return false;
    }

    // Cancel an appointment
    public static synchronized boolean cancelAppointment(
            int appointmentId, int studentId) {

        if (useDatabase) {
            return StudentAppointmentDao.cancelAppointment(
                    appointmentId, studentId);
        }


        StudentAppointment appointment
                = getAppointmentById(appointmentId, studentId);

        if (appointment == null || !isActive(appointment)) {
            return false;
        }

        appointment.setStatus(StudentAppointment.CANCELLED);
        return true;
    }

    /**
     * Explains why an appointment cannot be moved to the given slot.
     *
     * @return an error message, or null when rescheduling is allowed
     */
    public static synchronized String checkReschedule(
            int appointmentId,
            int studentId,
            LocalDate newDate,
            LocalTime newTime) {

        if (useDatabase) {
            return StudentAppointmentDao.checkReschedule(
                    appointmentId, studentId, newDate, newTime);
        }


        StudentAppointment appointment
                = getAppointmentById(appointmentId, studentId);

        if (appointment == null) {
            return "That appointment could not be found.";
        }
        if (!isActive(appointment)) {
            return "Only pending or approved appointments can be rescheduled.";
        }

        String slotError = validateSlot(newDate, newTime);
        if (slotError != null) {
            return slotError;
        }

        if (appointment.getAppointmentDate().equals(newDate)
                && appointment.getAppointmentTime().equals(newTime)) {
            return "That is already the time of this appointment.";
        }
        if (isSlotTaken(appointment.getCounsellorId(), newDate, newTime)) {
            return "This counsellor already has an appointment at that time.";
        }
        if (hasStudentConflict(studentId, newDate, newTime, appointmentId)) {
            return "You already have an appointment at that time.";
        }
        return null;
    }

    // Reschedule an appointment (it returns to PENDING for re-approval)
    public static synchronized boolean rescheduleAppointment(
            int appointmentId,
            int studentId,
            LocalDate newDate,
            LocalTime newTime) {

        if (useDatabase) {
            return StudentAppointmentDao.rescheduleAppointment(
                    appointmentId, studentId, newDate, newTime);
        }


        if (checkReschedule(appointmentId, studentId, newDate, newTime)
                != null) {
            return false;
        }

        StudentAppointment appointment
                = getAppointmentById(appointmentId, studentId);

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(StudentAppointment.PENDING);

        return true;
    }

    // Count appointments by status
    public static int countByStatus(int studentId, String status) {

        int count = 0;

        for (StudentAppointment appointment
                : getAppointmentsForStudent(studentId)) {
            if (appointment.getStatus().equals(status)) {
                count++;
            }
        }

        return count;
    }

    // Count active appointments that are today or later
    public static int countUpcoming(int studentId) {

        int count = 0;

        for (StudentAppointment appointment
                : getAppointmentsForStudent(studentId)) {
            if (isUpcoming(appointment)) {
                count++;
            }
        }

        return count;
    }

    // In-memory only: remove a student's appointments (use deleteAccount)
    public static synchronized int deleteAllForStudent(int studentId) {

        int before = appointments.size();
        appointments.removeIf(a -> a.getStudentId() == studentId);
        return before - appointments.size();
    }

    // Clear temporary data (useful for testing)
    public static synchronized void clearAll() {
        // memory only: this never touches the database
        appointments.clear();
        nextAppointmentId = 1;
    }

    // ------------------------------------------------------------------
    // Counsellors, student ID, feedback, profile, account
    // ------------------------------------------------------------------

    /** The counsellors a student can book. */
    public static List<StudentCounsellor> getCounsellors() {

        if (useDatabase) {
            return StudentAppointmentDao.getCounsellors();
        }
        return new ArrayList<>(java.util.Arrays.asList(MEMORY_COUNSELLORS));
    }

    /** The student's registration number (Student ID); "" when unknown. */
    public static String getRegistrationNumber(int studentId) {

        if (useDatabase) {
            return StudentAppointmentDao.getRegistrationNumber(studentId);
        }
        return studentId < 0 ? "" : "STU-DEMO";
    }

    /**
     * Times on that day the counsellor or the student is already booked.
     * excludeAppointmentId is the appointment being moved (or -1).
     */
    public static java.util.Set<LocalTime> getUnavailableTimes(
            int counsellorId, int studentId, LocalDate date,
            int excludeAppointmentId) {

        if (useDatabase) {
            return StudentAppointmentDao.getUnavailableTimes(counsellorId,
                    studentId, date, excludeAppointmentId);
        }

        java.util.Set<LocalTime> busy = new java.util.HashSet<>();
        for (LocalTime t = FIRST_SLOT; !t.isAfter(LAST_SLOT);
                t = t.plusMinutes(SLOT_MINUTES)) {
            if (isSlotTaken(counsellorId, date, t)
                    || hasStudentConflict(studentId, date, t,
                            excludeAppointmentId)) {
                busy.add(t);
            }
        }
        return busy;
    }

    /** @return an error message, or null when the feedback was saved */
    public static synchronized String saveFeedback(int appointmentId,
            int studentId, int rating, String comments) {

        if (useDatabase) {
            return StudentAppointmentDao.saveFeedback(appointmentId,
                    studentId, rating, comments);
        }

        if (rating < 1 || rating > 5) {
            return "Please choose a rating from 1 to 5 stars.";
        }
        StudentAppointment a = getAppointmentById(appointmentId, studentId);
        if (a == null) {
            return "That appointment could not be found.";
        }
        if (!StudentAppointment.COMPLETED.equals(a.getStatus())) {
            return "Feedback can only be given for completed appointments.";
        }
        if (a.getRating() > 0) {
            return "You have already given feedback for this appointment.";
        }
        a.setFeedback(rating, comments == null ? "" : comments.trim());
        return null;
    }

    /** @return an error message, or null when the profile was saved */
    public static String updateProfile(int studentId, String fullName,
            String email, String phone) {

        if (useDatabase) {
            return StudentAppointmentDao.updateProfile(studentId, fullName,
                    email, phone);
        }
        return null;
    }

    /**
     * Permanently deletes the student's account and appointments.
     *
     * @return how many appointments were removed
     */
    public static int deleteAccount(int studentId) {

        if (useDatabase) {
            return StudentAppointmentDao.deleteAccount(studentId);
        }
        return deleteAllForStudent(studentId);
    }

    /**
     * TESTING ONLY. Adds one appointment of every status for a student so the
     * counts, history and cancelled records can be demonstrated (students
     * cannot create APPROVED or COMPLETED appointments themselves). Does
     * nothing if the student already has appointments.
     */
    public static synchronized void seedDemoData(int studentId) {

        if (useDatabase) {
            return;   // never write demo rows into the real database
        }

        if (!getAppointmentsForStudent(studentId).isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        addRaw(studentId, 101, "Dr. Grace Wanjiru",
                today.minusDays(14), LocalTime.of(10, 0),
                "Exam stress and study planning", StudentAppointment.COMPLETED);
        addRaw(studentId, 102, "Mr. David Kimani",
                today.minusDays(5), LocalTime.of(14, 30),
                "Question about changing my major", StudentAppointment.CANCELLED);
        addRaw(studentId, 103, "Ms. Sarah Mwangi",
                nextWeekday(today.plusDays(3)), LocalTime.of(11, 0),
                "Follow-up session", StudentAppointment.APPROVED);
        addRaw(studentId, 101, "Dr. Grace Wanjiru",
                nextWeekday(today.plusDays(7)), LocalTime.of(15, 0),
                "Feeling overwhelmed this semester", StudentAppointment.PENDING);
    }

    private static void addRaw(int studentId, int counsellorId, String name,
            LocalDate date, LocalTime time, String reason, String status) {

        StudentAppointment a = new StudentAppointment(nextAppointmentId++,
                studentId, counsellorId, name, date, time, reason, status);
        a.setStudentRegNo("STU-DEMO");
        appointments.add(a);
    }

    private static LocalDate nextWeekday(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
