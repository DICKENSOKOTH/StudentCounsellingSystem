

package forms;

public class RegistrationPanel extends javax.swing.JPanel {

    private javax.swing.ComboBoxModel<String> studentMajorModel, studentYearModel;

    /**
     * Creates new form RegistrationPanel
     */
    public RegistrationPanel() {
        initComponents();

        // underline "Log In" on hover
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                java.awt.Font f = jLabel5.getFont();
                java.util.Map<java.awt.font.TextAttribute, Object> a = new java.util.HashMap<>(f.getAttributes());
                a.put(java.awt.font.TextAttribute.UNDERLINE, java.awt.font.TextAttribute.UNDERLINE_ON);
                jLabel5.setFont(f.deriveFont(a));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                java.awt.Font f = jLabel5.getFont();
                java.util.Map<java.awt.font.TextAttribute, Object> a = new java.util.HashMap<>(f.getAttributes());
                a.put(java.awt.font.TextAttribute.UNDERLINE, -1);
                jLabel5.setFont(f.deriveFont(a));
            }
        });

        // role toggle (Student / Counsellor)
        studentMajorModel = cmbMajor.getModel();
        studentYearModel  = cmbYear.getModel();
        ((RoleToggle) roleToggle).setOnChange(this::applyRole);

        // confirm-password check: live while typing, shake when leaving the box
        javax.swing.event.DocumentListener live = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { checkMatch(false, false); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { checkMatch(false, false); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkMatch(false, false); }
        };
        txtPassword.getDocument().addDocumentListener(live);
        txtConfirm.getDocument().addDocumentListener(live);

        txtConfirm.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { checkMatch(true, false); }
        });
    }

    /**
     * Shows or clears the "Passwords don't match" state on the confirm box.
     * shakeIfWrong: shake the box when they don't match.
     * evenIfEmpty: treat an empty confirm box as a mismatch (used on Submit).
     * Returns true if the passwords match.
     */
    private boolean checkMatch(boolean shakeIfWrong, boolean evenIfEmpty) {
        String p = new String(txtPassword.getPassword());
        String c = new String(txtConfirm.getPassword());
        boolean mismatch = (evenIfEmpty || !c.isEmpty()) && !p.equals(c);

        Glass.PasswordField box = (Glass.PasswordField) txtConfirm;
        box.setError(mismatch);
        ((Glass.Panel) jPanel1).setNote(txtConfirm, mismatch ? "Passwords don't match" : null);
        if (mismatch && shakeIfWrong) box.shake();
        return !mismatch;
    }

    private void applyRole(String role) {
        boolean c = role.equals("COUNSELLOR");

        lblId.setText(c ? "Qualification" : "Student Id");
        lblMajor.setText(c ? "Religion" : "Major");
        lblYear.setText(c ? "Gender" : "Year of study");

        cmbMajor.setModel(c
            ? new javax.swing.DefaultComboBoxModel<>(new String[]{
                " ", "Christian", "Muslim", "Hindu", "Other"})
            : studentMajorModel);

        cmbYear.setModel(c
            ? new javax.swing.DefaultComboBoxModel<>(new String[]{
                " ", "Female", "Male", "Other", "Prefer not to say"})
            : studentYearModel);

        txtId.setText("");            // don't carry a student id into the counsellor form
    }

    private static int yearToNumber(String s) {
        if (s == null) return 1;
        switch (s.trim().toLowerCase()) {
            case "freshman":  return 1;
            case "sophmore":  return 2;      // matches the spelling in the dropdown
            case "junior":    return 3;
            case "senior":    return 4;
            case "masters":   return 5;
            default:
                try { return Integer.parseInt(s.replaceAll("\\D", "")); }
                catch (NumberFormatException e) { return 1; }
        }
    }

    private final java.awt.Image bgImage =
        new javax.swing.ImageIcon(getClass().getResource("/images/javabg6.jpg")).getImage();

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();
        int imgW = bgImage.getWidth(this), imgH = bgImage.getHeight(this);

        // keep the image's proportions and fill the panel
        double scale = Math.max((double) w / imgW, (double) h / imgH);
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);

        g2.drawImage(bgImage, (w - drawW) / 2, (h - drawH) / 2, drawW, drawH, this);
        g2.dispose();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new Glass.Panel();
        jLabel1 = new javax.swing.JLabel();
        txtPhone = new Glass.TextField();
        txtEmail = new Glass.TextField();
        txtName = new Glass.TextField();
        txtId = new Glass.TextField();
        jLabel2 = new javax.swing.JLabel();
        lblId = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblMajor = new javax.swing.JLabel();
        lblYear = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtConfirm = new Glass.PasswordField();
        txtPassword = new Glass.PasswordField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cmbMajor = new Glass.ComboBox();
        cmbYear = new Glass.ComboBox();
        btnSubmit = new Glass.Button();
        roleToggle = new RoleToggle();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(153, 153, 0));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setText("Sign Up");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel2.setText("Full Name");

        lblId.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblId.setText("Student Id");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setText("School Email");

        lblMajor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblMajor.setText("Major");

        lblYear.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblYear.setText("Year of study");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel7.setText("Phone Number");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel8.setText("Password");

        jLabel9.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel9.setText("Confirm Password");

        cmbMajor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cmbMajor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DSA", "SWE", "APT", "IST", "Pharmacy", "IBA", "IR", "Cinematics", "Nursing", "Robotics", " " }));
        cmbMajor.setSelectedItem(" ");

        cmbYear.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cmbYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Freshman", "Sophmore", "Junior", "Senior", "Masters" }));
        cmbYear.setSelectedItem(" ");

        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(this::btnSubmitActionPerformed);

        javax.swing.GroupLayout roleToggleLayout = new javax.swing.GroupLayout(roleToggle);
        roleToggle.setLayout(roleToggleLayout);
        roleToggleLayout.setHorizontalGroup(
            roleToggleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 69, Short.MAX_VALUE)
        );
        roleToggleLayout.setVerticalGroup(
            roleToggleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        jLabel3.setText("Already have an account?");

        jLabel5.setFont(new java.awt.Font("Calibri", 1, 12)); // NOI18N
        jLabel5.setText("Log In");
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(116, 116, 116)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtPhone)
                    .addComponent(txtId, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(roleToggle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbMajor, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbYear, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel7)
                            .addComponent(lblYear)
                            .addComponent(lblMajor)
                            .addComponent(jLabel4)
                            .addComponent(lblId)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(txtConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(119, 119, 119))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(193, 193, 193)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(roleToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(3, 3, 3)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblId)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMajor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbMajor, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblYear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbYear, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(253, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(301, 301, 301))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        ((studentcounsellingsystem.MainFrame) javax.swing.SwingUtilities.getWindowAncestor(this)).showScreen("LOGIN");
    }//GEN-LAST:event_jLabel5MouseClicked

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
boolean counsellor = ((RoleToggle) roleToggle).isCounsellor();

        String name     = txtName.getText().trim();
        String idOrQual = txtId.getText().trim();
        String email    = txtEmail.getText().trim();
        String phone    = txtPhone.getText().trim();
        String password = new String(txtPassword.getPassword());
        String combo1   = (String) cmbMajor.getSelectedItem();   // Major (student) or Religion (counsellor)
        String combo2   = (String) cmbYear.getSelectedItem();

// ---- validation ----
        String problem = null;
        if (name.isEmpty() || idOrQual.isEmpty() || email.isEmpty() || password.isEmpty()) {
            problem = "Please fill in all required fields.";
        } else if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            problem = "Enter a valid email address.";
        } else if (!phone.isEmpty() && !phone.matches("^\\+?[0-9 ]{9,15}$")) {
            problem = "Enter a valid phone number, or leave it blank.";
        } else if (password.length() < 8) {
            problem = "Password must be at least 8 characters.";
        }
        if (problem != null) {
            javax.swing.JOptionPane.showMessageDialog(this, problem, "Check your details",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // passwords must match: red outline, message and shake instead of a pop-up
        if (!checkMatch(true, true)) {
            txtConfirm.requestFocus();
            return;
        }

        // ---- save ----
        dao.UserDao userDao = new dao.UserDao();
        String error;
        try {
            error = counsellor
                ? userDao.registerCounsellor(name, email, phone, password, idOrQual, combo1, combo2)
                : userDao.registerStudent(name, email, phone, password, idOrQual, combo1, yearToNumber(combo2));
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Could not reach the database. Check that MySQL is running in XAMPP.");
            return;
        }

        if (error != null) {
            javax.swing.JOptionPane.showMessageDialog(this, error, "Registration failed",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

     
javax.swing.JOptionPane.showMessageDialog(this, "Account created. You can log in now.");
txtName.setText(""); txtId.setText(""); txtEmail.setText("");
txtPhone.setText(""); txtPassword.setText(""); txtConfirm.setText("");
((studentcounsellingsystem.MainFrame) javax.swing.SwingUtilities.getWindowAncestor(this)).showScreen("LOGIN");   
    }//GEN-LAST:event_btnSubmitActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox<String> cmbMajor;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblMajor;
    private javax.swing.JLabel lblYear;
    private javax.swing.JPanel roleToggle;
    private javax.swing.JPasswordField txtConfirm;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables
}
