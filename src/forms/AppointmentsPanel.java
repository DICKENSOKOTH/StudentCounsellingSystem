package forms;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * UI-only AppointmentsPanel for the USIU Counselling System.
 * JPanel for the group's MainFrame/CardLayout; sample data only.
 */
public class AppointmentsPanel extends JPanel {
    private static final Color NAVY = new Color(8, 28, 55);
    private static final Color TEAL = new Color(39, 190, 174);
    private static final Color GOLD = new Color(255, 185, 48);
    private static final Color PAGE = new Color(242, 246, 250);
    private static final Color INK = new Color(24, 39, 61);
    private static final Color MUTED = new Color(112, 126, 145);
    private static final Color LINE = new Color(224, 231, 239);

    public AppointmentsPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE);
        setPreferredSize(new Dimension(1100, 700));
        add(sidebar(), BorderLayout.WEST);
        add(content(), BorderLayout.CENTER);
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setBackground(NAVY);
        p.setPreferredSize(new Dimension(218, 700));
        p.setBorder(BorderFactory.createEmptyBorder(25, 18, 20, 18));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("USIU-AFRICA");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 19));
        JLabel sub = new JLabel("STUDENT COUNSELLING CENTRE");
        sub.setForeground(new Color(178, 199, 218));
        sub.setFont(new Font("SansSerif", Font.BOLD, 8));
        p.add(logo); p.add(Box.createVerticalStrut(5)); p.add(sub);
        p.add(Box.createVerticalStrut(42));
        p.add(menu("MAIN MENU", false));
        p.add(Box.createVerticalStrut(14));
        p.add(menu("Dashboard", false));
        p.add(Box.createVerticalStrut(7));
        p.add(menu("Appointments", true));
        p.add(Box.createVerticalStrut(7));
        p.add(menu("Counsellors", false));
        p.add(Box.createVerticalStrut(7));
        p.add(menu("Session Records", false));
        p.add(Box.createVerticalStrut(7));
        p.add(menu("Feedback", false));
        p.add(Box.createVerticalGlue());
        JLabel help = new JLabel("<html><b>We're here for you</b><br><font color='#B6C9DA'>Confidential student support</font></html>");
        help.setForeground(Color.WHITE);
        help.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(help);
        p.add(Box.createVerticalStrut(20));
        JLabel foot = new JLabel("USIU-AFRICA | STUDENT PORTAL");
        foot.setForeground(new Color(157, 178, 199));
        foot.setFont(new Font("SansSerif", Font.PLAIN, 8));
        p.add(foot);
        return p;
    }

    private JLabel menu(String text, boolean active) {
        JLabel l = new JLabel(text);
        l.setForeground(active ? Color.WHITE : new Color(204, 217, 229));
        l.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 12));
        if (active) {
            l.setOpaque(true);
            l.setBackground(new Color(17, 48, 82));
            l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, TEAL),
                BorderFactory.createEmptyBorder(11, 12, 11, 8)));
        } else l.setBorder(BorderFactory.createEmptyBorder(11, 15, 11, 8));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return l;
    }

    private JPanel content() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(PAGE);
        p.setBorder(BorderFactory.createEmptyBorder(24, 27, 23, 27));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel crumb = new JLabel("STUDENT COUNSELLING CENTRE / MANAGEMENT");
        crumb.setForeground(MUTED);
        crumb.setFont(new Font("SansSerif", Font.BOLD, 9));
        JLabel title = new JLabel("Appointments");
        title.setForeground(INK);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel subtitle = new JLabel("View and manage counselling appointment requests.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titles.add(crumb); titles.add(Box.createVerticalStrut(6));
        titles.add(title); titles.add(Box.createVerticalStrut(4)); titles.add(subtitle);
        JLabel profile = new JLabel("<html><div style='text-align:right'><b>USIU Counselling</b><br><font color='#718096'>Appointments workspace</font></div></html>");
        profile.setForeground(INK);
        head.add(titles, BorderLayout.WEST); head.add(profile, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(stats());
        body.add(Box.createVerticalStrut(17));
        body.add(schedule());
        p.add(head, BorderLayout.NORTH); p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel stats() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));
        p.add(stat("TOTAL APPOINTMENTS", "24", "All records", TEAL));
        p.add(stat("PENDING REQUESTS", "06", "Awaiting review", GOLD));
        p.add(stat("APPROVED", "12", "Scheduled sessions", new Color(55,151,119)));
        p.add(stat("COMPLETED", "06", "Sessions finished", new Color(91,137,201)));
        return p;
    }

    private JPanel stat(String name, String value, String note, Color accent) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
            BorderFactory.createEmptyBorder(11, 13, 9, 13)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(name); a.setForeground(MUTED); a.setFont(new Font("SansSerif", Font.BOLD, 8));
        JLabel b = new JLabel(value); b.setForeground(accent); b.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel c = new JLabel(note); c.setForeground(MUTED); c.setFont(new Font("SansSerif", Font.PLAIN, 9));
        p.add(a); p.add(Box.createVerticalStrut(3)); p.add(b); p.add(c);
        return p;
    }

    private JPanel schedule() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
            BorderFactory.createEmptyBorder(15, 15, 13, 15)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Appointment Schedule");
        h.setForeground(INK); h.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel d = new JLabel("Fictional sample records for interface demonstration");
        d.setForeground(MUTED); d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        heading.add(h); heading.add(Box.createVerticalStrut(4)); heading.add(d);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        filters.setOpaque(false);
        JTextField search = new JTextField(" Search by student");
        search.setFont(new Font("SansSerif", Font.PLAIN, 10));
        search.setPreferredSize(new Dimension(145, 30));
        JComboBox<String> status = new JComboBox<>(new String[]{"All statuses","Pending","Approved","Completed","Cancelled"});
        status.setFont(new Font("SansSerif", Font.PLAIN, 10));
        filters.add(search); filters.add(status);
        top.add(heading, BorderLayout.WEST); top.add(filters, BorderLayout.EAST);

        String[] cols = {"STUDENT","COUNSELLOR","DATE","TIME","SESSION","STATUS"};
        Object[][] rows = {
            {"Amina Hassan","Dr. Sarah Kimani","18 Nov 2026","10:00 AM","In-person","Approved"},
            {"Brian Otieno","Mr. David Mwangi","18 Nov 2026","11:30 AM","Online","Pending"},
            {"Grace Wanjiku","Dr. Sarah Kimani","19 Nov 2026","02:00 PM","In-person","Approved"},
            {"Daniel Peter","Ms. Faith Njeri","20 Nov 2026","09:00 AM","Online","Completed"},
            {"Mary Akello","Mr. David Mwangi","21 Nov 2026","01:00 PM","In-person","Cancelled"},
            {"Peter Maina","Ms. Faith Njeri","23 Nov 2026","03:00 PM","Online","Pending"}
        };
        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(39);
        table.setFont(new Font("SansSerif", Font.PLAIN, 10));
        table.setForeground(INK);
        table.setGridColor(new Color(237,241,246));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 8));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(246,249,252));
        table.getTableHeader().setPreferredSize(new Dimension(0,34));
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel count = new JLabel("Showing 6 fictional sample records");
        count.setForeground(MUTED); count.setFont(new Font("SansSerif", Font.PLAIN, 9));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        actions.add(button("View details", false));
        actions.add(button("Approve", true));
        actions.add(button("Decline", false));
        bottom.add(count, BorderLayout.WEST); bottom.add(actions, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH); p.add(scroll, BorderLayout.CENTER); p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 9));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(text.length() > 9 ? 118 : 82, 30));
        if (primary) {
            b.setBackground(TEAL); b.setForeground(NAVY);
            b.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
        } else {
            b.setBackground(Color.WHITE); b.setForeground(INK);
            b.setBorder(BorderFactory.createLineBorder(LINE));
        }
        return b;
    }
}
