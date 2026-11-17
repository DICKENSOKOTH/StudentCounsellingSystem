/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  d
 * Created: Nov 8, 2026
 */



CREATE DATABASE IF NOT EXISTS student_counselling_db;

USE student_counselling_db;

-- ==========================================
-- 1. USERS: Common account information
-- ==========================================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,

    role ENUM('ADMIN', 'COUNSELLOR', 'STUDENT')
        NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 2. STUDENTS: Student-specific information
-- ==========================================

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,

    registration_number VARCHAR(50) NOT NULL UNIQUE,
    course VARCHAR(100),
    year_of_study INT,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- ==========================================
-- 3. COUNSELLORS: Counsellor-specific information
-- ==========================================

CREATE TABLE counsellors (
    counsellor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,

    specialization VARCHAR(100),
    qualification VARCHAR(150),
    gender VARCHAR(50),
    religion VARCHAR(100),

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- ==========================================
-- 4. APPOINTMENTS
-- ==========================================

CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,

    student_id INT NOT NULL,
    counsellor_id INT NOT NULL,

    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,

    reason VARCHAR(255),

    status ENUM(
        'PENDING',
        'APPROVED',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Active bookings occupy a time slot.
    -- Cancelled and completed bookings release it.
    active_slot VARCHAR(120)
        GENERATED ALWAYS AS (
            CASE
                WHEN status IN ('PENDING', 'APPROVED')
                THEN CONCAT(
                    counsellor_id, '-',
                    appointment_date, '-',
                    appointment_time
                )
                ELSE NULL
            END
        ) STORED,

    UNIQUE KEY unique_active_slot (active_slot),

    FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    FOREIGN KEY (counsellor_id)
        REFERENCES counsellors(counsellor_id)
);


-- ==========================================
-- 5. COUNSELLING SESSIONS
-- ==========================================

CREATE TABLE counselling_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,

    appointment_id INT NOT NULL UNIQUE,

    presenting_concern TEXT,
    counselling_goals TEXT,
    session_notes TEXT,
    action_plan TEXT,

    duration_minutes INT,
    session_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    follow_up_date DATE,

    FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id)
        ON DELETE CASCADE
);


-- ==========================================
-- 6. FEEDBACK
-- ==========================================

CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,

    appointment_id INT NOT NULL UNIQUE,

    rating INT NOT NULL,
    comments TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_feedback_rating
        CHECK (rating BETWEEN 1 AND 5),

    FOREIGN KEY (appointment_id)
        REFERENCES appointments(appointment_id)
        ON DELETE CASCADE
);
