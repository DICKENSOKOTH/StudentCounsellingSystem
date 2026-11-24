/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Student;

/**
 * A counsellor the student can book (read from the counsellors table).
 */
public class StudentCounsellor {

    private final int counsellorId;
    private final String name;
    private final String specialization;

    public StudentCounsellor(int counsellorId, String name,
            String specialization) {
        this.counsellorId = counsellorId;
        this.name = name;
        this.specialization = specialization;
    }

    /** counsellors.counsellor_id (the value appointments store). */
    public int getCounsellorId() {
        return counsellorId;
    }

    public String getName() {
        return name;
    }

    /** May be empty when the counsellor did not give one. */
    public String getSpecialization() {
        return specialization == null || specialization.isBlank()
                ? "Counsellor" : specialization;
    }
}
