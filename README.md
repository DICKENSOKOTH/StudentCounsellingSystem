# Student Counselling System - USIU-Africa

A Java Swing desktop application (Java + Ant, built with Apache NetBeans) where students book counselling sessions, counsellors manage requests and session notes, and administrators manage the whole system.

## Run it

1. Start **Apache** and **MySQL** in XAMPP.
2. In phpMyAdmin open **Import** and import, in this order:
   1. `src/database/schema.sql` (creates the database and tables; drop `student_counselling_db` first if it already exists)
   2. `src/database/sample_data.sql` (demo people and appointments; dates are relative to today)
3. Open the project in NetBeans and press **F6** (Run Project). Or from a terminal:
   ```bash
   ant clean jar
   java -jar dist/StudentCounsellingSystem.jar
   ```
4. Check the connection any time: right-click `database/DBConnection.java` > **Run File** (prints "Connected to database").

The JDBC driver is bundled in `lib/` (MariaDB Connector/J), so nothing else needs to be installed. Database user/password are the XAMPP defaults (`root`, empty) in `database/DBConnection.java`.

## Demo logins

| Role | Email | Password |
|------|-------|----------|
| Administrator | `admin@example.com` | `Admin@123` |
| Counsellor | `grace@example.com` (also `samuel@`, `fatuma@`) | `Counsel@123` |
| Student | `amina@example.com` (also `brian@`, `chloe@`, `david@`) | `Student@123` |

## What each role can do

All CRUD operations are available.

| | Create | Read | Update | Delete |
|---|--------|------|--------|--------|
| **Student** | register, book appointment, give feedback | dashboard, own appointments, counsellor details | edit profile, change password, reschedule / cancel, edit feedback | delete cancelled appointments, remove own feedback, delete own account |
| **Counsellor** | register, record session notes | requests, today's sessions, ratings received | approve / decline, edit profile and notes | delete session notes, delete own account |
| **Administrator** | add any user | overview with charts, workload report, print reports and lists | edit users / appointments / feedback, reset passwords | delete users, appointments, feedback |

### Business rules

Enforced in `dao/AppointmentDAO`:

- Monday to Friday, one-hour slots 08:00-16:00
- No double-booking for a counsellor or a student
- Future dates only
- Fixed status flow: `PENDING` -> `APPROVED` -> `COMPLETED`, or `CANCELLED` from `PENDING` / `APPROVED`

## Object-oriented design

- **Encapsulation** - models hold private fields with getters/setters; SQL only lives in `dao`.
- **Inheritance + abstraction** - `Student` and `Counsellor` extend the abstract class `Person`.
- **Polymorphism** - each person answers `getRole()` in its own way; dashboards extend `DashboardBase`.
- **Interface** - every DAO that does CRUD implements `CrudDAO<T>`.
- **DAO pattern** - `UserDAO`, `StudentDAO`, `CounsellorDAO`, `AppointmentDAO`, `SessionDAO`, `FeedbackDAO`, `ReportDAO`.
- **One window** - `MainFrame` is the only `JFrame`; every screen is a 1100 x 700 `JPanel` it switches between.

## Screens

Designed in the NetBeans **Design** tab, code in the **Source** tab. Open any of these in NetBeans: the Design tab shows the whole screen (every label, field, table and button), the Source tab shows the code behind each button (outside the grey generated block).

| Form (`forms` package) | What its Design tab contains |
|------------------------|------------------------------|
| `WelcomePanel` | landing page: animated background, role cards, sign-in / register links |
| `LoginPanel` | sign-in card with Student / Counsellor / Admin tabs |
| `RegisterPanel` | create a student or counsellor account |
| `StudentPanel` | student portal: sidebar menu + Dashboard, Book Appointment, My Appointments, My Profile |
| `CounsellorPanel` | counsellor portal: sidebar menu + Dashboard, Appointments (+ session notes), Feedback, My Profile |
| `AdminPanel` | admin portal: sidebar menu + Overview, Users, Appointments, Feedback, with Print buttons |

The administrator can print the counsellor workload report, the users list, all appointments and all feedback (Swing's print dialog; choose "Microsoft Print to PDF" to save a PDF).

## Packages

| Package | Contents |
|---------|----------|
| `studentcounsellingsystem` | `Main`, `MainFrame` |
| `forms` | the six designed forms above (each has a `.form` file for the Design tab) and four small pop-up helpers: `SessionForm`, `FeedbackForm`, `UserForm`, `AppointmentForm` (plus `SlotPicker`) |
| `models` | `Person`, `Student`, `Counsellor`, `User`, `Appointment`, `CounsellingSession`, `Feedback` |
| `dao` | data access classes (see above) |
| `database` | `DBConnection`, `schema.sql`, `sample_data.sql` |
| `utils` | `Theme`, `Ui` (shared look), `AnimatedBackground`, `GlassPanel`, `BarChart`, `StarRating`, and small look-and-feel components for the Design tab (`StatCard`, `NavButton`, `Avatar`, `GradientPanel`, `TopBar`, `WindowButtons`), `PasswordUtil`, `Session`, `Validate` |
| `images` | logo |

## Security

Passwords are never stored in plain text: they are hashed with PBKDF2 (`utils/PasswordUtil`).