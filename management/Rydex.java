//javac -cp "libs/mysql-connector-j-9.5.0.jar" -d out management\*.java
//java -cp "out;libs/mysql-connector-j-9.5.0.jar" Rydex 

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class Rydex extends JFrame {

    // Colors & Fonts
    private static final Color PRIMARY_COLOR = new Color(0x6A, 0x1B, 0x9A);
    private static final Color PRIMARY_LIGHT = new Color(0x9C, 0x4D, 0xCC);
    private static final Color PRIMARY_DARK = new Color(0x4A, 0x14, 0x8C);
    private static final Color BACKGROUND_COLOR = new Color(0xFA, 0xFA, 0xFB);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = PRIMARY_DARK;
    private static final Color TEXT_SECONDARY = PRIMARY_COLOR;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private java.util.Map<String, JPanel> namedCards = new java.util.HashMap<>();

    // DAOs
    private UserDAO userDAO = new UserDAO();
    private RideDAO rideDAO = new RideDAO();
    private RouteDAO routeDAO = new RouteDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private FeedbackDAO feedbackDAO = new FeedbackDAO();
    private DriverShiftDAO shiftDAO = new DriverShiftDAO();
    private RideAssistantDAO assistantDAO = new RideAssistantDAO();

    public Rydex() {
        setTitle("Rydex");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BACKGROUND_COLOR);

        JPanel welcome = createWelcomePanel();
        JPanel loading = createLoadingPanel();
        mainCardPanel.add(welcome, "WELCOME"); namedCards.put("WELCOME", welcome);
        mainCardPanel.add(loading, "LOADING"); namedCards.put("LOADING", loading);

        setContentPane(mainCardPanel);
        testDatabaseConnection();
        setVisible(true);
    }

    // ----------------- Base UI (Welcome + Loading + helpers) -----------------

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel header = createGradientHeader("Rydex", "Your reliable campus transportation partner");
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BACKGROUND_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(30, 24, 30, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        center.add(createFeatureCards(), gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        JPanel leftButtons = createButtonGroup("Driver",
            new String[]{"Register as Driver", "Login as Driver"},
            new Runnable[]{() -> showRegistrationForm("Driver"), () -> showLoginForm("Driver")},
            PRIMARY_COLOR);
        center.add(leftButtons, gbc);

        gbc.gridx = 1;
        JPanel rightButtons = createButtonGroup("Rider",
            new String[]{"Register as Rider", "Login as Rider"},
            new Runnable[]{() -> showRegistrationForm("Rider"), () -> showLoginForm("Rider")},
            PRIMARY_LIGHT);
        center.add(rightButtons, gbc);

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(createFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createGradientHeader(String title, String subtitle) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(1000, 120));
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_BODY);
        subLbl.setForeground(new Color(255, 255, 255, 200));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(titleLbl);
        left.add(subLbl);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JPanel createFeatureCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 18, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createFeatureCard("Quick Rides", "Get to classes on time with reliable campus drivers"));
        panel.add(createFeatureCard("Affordable", "Student-friendly pricing and flexible payments"));
        panel.add(createFeatureCard("Rate Drivers", "Share feedback to improve service"));

        return panel;
    }

    private JPanel createFeatureCard(String title, String desc) {
        JPanel card = new RoundedPanel(12, CARD_COLOR);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(FONT_SUBHEADER);
        t.setForeground(PRIMARY_COLOR);

        JTextArea d = new JTextArea(desc);
        d.setFont(FONT_BODY);
        d.setEditable(false);
        d.setOpaque(false);
        d.setLineWrap(true);
        d.setWrapStyleWord(true);

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        return card;
    }

    private JPanel createButtonGroup(String title, String[] buttonTexts, Runnable[] actions, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND_COLOR);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(FONT_SUBHEADER);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel btns = new JPanel(new GridLayout(buttonTexts.length, 1, 10, 10));
        btns.setBackground(BACKGROUND_COLOR);

        for (int i = 0; i < buttonTexts.length; i++) {
            JButton b = createModernButton(buttonTexts[i], color);
            final Runnable action = actions[i];
            b.addActionListener(e -> action.run());
            btns.add(b);
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(btns, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PRIMARY_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        footer.setPreferredSize(new Dimension(1000, 52));

        JLabel c = new JLabel("© 2025 Rydex - Made by Jawad Ahmad");
        c.setFont(FONT_BODY);
        c.setForeground(new Color(255, 255, 255, 180));

        JButton exit = createModernButton("Exit", PRIMARY_DARK);
        exit.setPreferredSize(new Dimension(90, 36));
        exit.addActionListener(e -> {
            DatabaseConfig.closeConnection();
            System.exit(0);
        });

        footer.add(c, BorderLayout.WEST);
        footer.add(exit, BorderLayout.EAST);
        return footer;
    }

    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JLabel loading = new JLabel("Connecting to Database...", SwingConstants.CENTER);
        loading.setFont(FONT_HEADER);
        loading.setForeground(TEXT_PRIMARY);

        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        pb.setPreferredSize(new Dimension(320, 18));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridy = 0;
        center.add(loading, gbc);
        gbc.gridy = 1;
        center.add(pb, gbc);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void testDatabaseConnection() {
        showLoadingScreen();
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Class<?> cfg = Class.forName("DatabaseConfig");
                    try {
                        java.lang.reflect.Method testM = cfg.getMethod("testConnection");
                        Object res = testM.invoke(null);
                        if (res instanceof Boolean) return (Boolean) res;
                    } catch (NoSuchMethodException ns) {
                        try {
                            java.lang.reflect.Method getConn = cfg.getMethod("getConnection");
                            Object conn = getConn.invoke(null);
                            return conn != null;
                        } catch (NoSuchMethodException ns2) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) showDatabaseError();
                    else showWelcomeScreen();
                } catch (Exception e) {
                    showDatabaseError();
                }
            }
        };
        worker.execute();
    }

    private void showLoadingScreen() { cardLayout.show(mainCardPanel, "LOADING"); }
    private void showWelcomeScreen() { cardLayout.show(mainCardPanel, "WELCOME"); }

    private void showDatabaseError() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='text-align:center;'><b style='color:#D32F2F'>Database Connection Failed</b><br>Please check your DB and DatabaseConfig.java</div></html>",
            "Connection Error", JOptionPane.ERROR_MESSAGE);
        showWelcomeScreen();
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(color.darker());
                else if (getModel().isRollover()) g2.setColor(color.brighter());
                else g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    // Large rounded primary-style button (used in dashboards to match landing)
    private JButton createLargePrimaryButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, 0, getHeight(), color.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(340, 48));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = new RoundedPanel(12, CARD_COLOR);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel t = new JLabel(title, SwingConstants.LEFT);
        t.setFont(FONT_SUBHEADER);
        t.setForeground(accent);
        JLabel v = new JLabel(value, SwingConstants.LEFT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(TEXT_PRIMARY);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    // ----------------- Registration & Login (unchanged except small improvements) -----------------

    private void showRegistrationForm(String userType) {
        JDialog dialog = createCenteredDialog("Register as " + userType, 420, 520);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel("Create " + userType + " Account", SwingConstants.CENTER);
        header.setFont(FONT_HEADER);
        header.setForeground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_COLOR);
        form.setBorder(new LineBorder(new Color(0,0,0,30), 1, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

        JTextField nameField = createStyledTextField();
        form.add(createFormRow("Full Name", nameField), gbc);

        gbc.gridy++;
        JTextField emailField = createStyledTextField();
        form.add(createFormRow("Email", emailField), gbc);

        gbc.gridy++;
        JPasswordField passwordField = createStyledPasswordField();
        form.add(createFormRow("Password", passwordField), gbc);

        final JTextField[] licenseField = new JTextField[1];
        if (userType.equals("Driver")) {
            gbc.gridy++;
            licenseField[0] = createStyledTextField();
            form.add(createFormRow("License Number", licenseField[0]), gbc);
        }

        gbc.gridy++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setBackground(CARD_COLOR);
        JButton registerBtn = createModernButton("Register", PRIMARY_LIGHT);
        JButton cancelBtn = createModernButton("Cancel", PRIMARY_DARK);
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showMessage("Error", "All fields are required", PRIMARY_DARK); return;
            }
            if (!isValidEmail(email)) {
                showMessage("Invalid Email", "Please enter a valid email address (e.g. user@example.com)", PRIMARY_DARK); return;
            }
            if (!isValidPassword(password)) {
                showMessage("Weak Password", "Password must be at least 6 characters long.", PRIMARY_DARK); return;
            }
            if (userType.equals("Driver") && (licenseField[0] == null || licenseField[0].getText().trim().isEmpty())) {
                showMessage("Error", "License number required for drivers", PRIMARY_DARK); return;
            }
            boolean success;
            if (userType.equals("Driver")) success = userDAO.registerDriver(name, email, password, licenseField[0].getText().trim());
            else success = userDAO.registerRider(name, email, password);
            if (success) showMessage("Success", "Registration successful", PRIMARY_COLOR);
            else showMessage("Error", "Registration failed (email may exist)", PRIMARY_DARK);
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        btns.add(registerBtn); btns.add(cancelBtn);
        form.add(btns, gbc);

        content.add(header, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        dialog.add(content);
        dialog.setVisible(true);
    }

    // IMPORTANT: login dialog is intentionally left unchanged (your preferred design)
    private void showLoginForm(String userType) {
        JDialog dialog = createCenteredDialog("Login as " + userType, 400, 380);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel(userType + " Login", SwingConstants.CENTER);
        header.setFont(FONT_HEADER); header.setForeground(PRIMARY_COLOR); header.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_COLOR);
        form.setBorder(new LineBorder(new Color(0,0,0,30), 1, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

        JTextField emailField = createStyledTextField();
        form.add(createFormRow("Email", emailField), gbc);
        gbc.gridy++;
        JPasswordField passwordField = createStyledPasswordField();
        form.add(createFormRow("Password", passwordField), gbc);
        gbc.gridy++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setBackground(CARD_COLOR);
        JButton loginBtn = createModernButton("Login", PRIMARY_COLOR);
        JButton cancelBtn = createModernButton("Cancel", PRIMARY_DARK);
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (email.isEmpty() || password.isEmpty()) { showMessage("Error", "Email and password required", PRIMARY_DARK); return; }
            if (!isValidEmail(email)) { showMessage("Invalid Email", "Please enter a valid email address (e.g. user@example.com)", PRIMARY_DARK); return; }
            if (!isValidPassword(password)) { showMessage("Weak Password", "Password must be at least 6 characters long.", PRIMARY_DARK); return; }
            if (userType.equals("Driver")) {
                Driver driver = userDAO.loginDriver(email, password);
                if (driver != null) {
                    Vehicle v = vehicleDAO.getVehicleByDriverId(driver.getUserId());
                    if (v != null) driver.setVehicle(v);
                    showDriverDashboard(driver);
                    dialog.dispose();
                } else showMessage("Error", "Invalid credentials", PRIMARY_DARK);
            } else {
                Rider rider = userDAO.loginRider(email, password);
                if (rider != null) {
                    showRiderDashboard(rider);
                    dialog.dispose();
                } else showMessage("Error", "Invalid credentials", PRIMARY_DARK);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        btns.add(loginBtn); btns.add(cancelBtn);
        form.add(btns, gbc);

        content.add(header, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 6));
        p.setBackground(CARD_COLOR);
        JLabel l = new JLabel(label);
        l.setFont(FONT_BODY);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setPreferredSize(new Dimension(420, 36));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,40), 1, true), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    // Apply a lightweight placeholder to a text field: gray text shown when empty,
    // cleared when the field receives focus, and restored on focus lost if empty.
    private void applyPlaceholder(JTextField f, String placeholder) {
        Color placeholderColor = new Color(140, 140, 140);
        f.setText(placeholder);
        f.setForeground(placeholderColor);
        f.putClientProperty("placeholder", placeholder);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().trim().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(placeholderColor);
                }
            }
        });
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setPreferredSize(new Dimension(420, 36));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,40), 1, true), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private void showMessage(String title, String message, Color color) {
        JDialog d = createCenteredDialog(title, 380, 180);
        JPanel c = new JPanel(new BorderLayout(12, 12));
        c.setBackground(BACKGROUND_COLOR);
        c.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel t = new JLabel(title, SwingConstants.CENTER); t.setFont(FONT_SUBHEADER); t.setForeground(color);
        JLabel m = new JLabel("<html><div style='text-align:center;'>" + message + "</div></html>", SwingConstants.CENTER); m.setFont(FONT_BODY);
        JButton ok = createModernButton("OK", color);
        ok.addActionListener(e -> d.dispose());
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER)); btn.setBackground(BACKGROUND_COLOR); btn.add(ok);
        c.add(t, BorderLayout.NORTH); c.add(m, BorderLayout.CENTER); c.add(btn, BorderLayout.SOUTH);
        d.add(c); d.setVisible(true);
    }

    private JDialog createCenteredDialog(String title, int w, int h) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(w, h);
        d.setLocationRelativeTo(this);
        d.setResizable(false);
        return d;
    }

    // Simple validators for email and password used in registration/login dialogs.
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return java.util.regex.Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email);
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= 6; // require minimum length
    }

    // ----------------- Dashboards (Driver & Rider) - Enhanced UI to match landing page -----------------

    private void showDriverDashboard(Driver driver) {
        if (namedCards.containsKey("DRIVER_DASH")) {
            mainCardPanel.remove(namedCards.get("DRIVER_DASH"));
            namedCards.remove("DRIVER_DASH");
        }
        JPanel panel = createDriverDashboardPanel(driver);
        mainCardPanel.add(panel, "DRIVER_DASH");
        namedCards.put("DRIVER_DASH", panel);
        cardLayout.show(mainCardPanel, "DRIVER_DASH");
    }

    private JPanel createDriverDashboardPanel(Driver driver) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Top: Large gradient header (similar to landing)
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 110));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Driver Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Welcome, " + driver.getName());
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(new Color(255,255,255,200));

        JPanel text = new JPanel(new GridLayout(2,1));
        text.setOpaque(false);
        text.add(title); text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        // Center: stats row + two-column action area like landing boxes
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        int ridesCompleted = 0;
        try {
            List<String> rides = rideDAO.getRidesByDriver(driver.getUserId());
            ridesCompleted = rides == null ? 0 : rides.size();
        } catch (Exception ignored) {}
        String earnings = String.format("PKR %.2f", driver.getTotalEarnings());
        statsRow.add(createStatCard("Earnings", earnings, PRIMARY_COLOR));
        statsRow.add(createStatCard("Rides Completed", String.valueOf(ridesCompleted), PRIMARY_LIGHT));
        statsRow.add(createStatCard("Vehicle", driver.getVehicle() != null ? driver.getVehicle().getModel() : "No vehicle", PRIMARY_DARK));

        center.add(statsRow, BorderLayout.NORTH);

        // Action area (two columns)
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 18, 18);
        gbc.fill = GridBagConstraints.BOTH;

        // Left: quick action boxes (cards)
        JPanel leftCol = new JPanel();
        leftCol.setBackground(BACKGROUND_COLOR);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JPanel quickCard = new RoundedPanel(12, CARD_COLOR);
        quickCard.setLayout(new GridBagLayout());
        quickCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        GridBagConstraints q = new GridBagConstraints();
        q.insets = new Insets(10,10,10,10);
        q.gridx = 0; q.gridy = 0; q.fill = GridBagConstraints.HORIZONTAL;

        JLabel quickTitle = new JLabel("Quick Actions");
        quickTitle.setFont(FONT_SUBHEADER);
        quickTitle.setForeground(TEXT_PRIMARY);
        q.gridwidth = 2; quickCard.add(quickTitle, q);

        q.gridy++; q.gridwidth = 1;
        JButton startBtn = createLargePrimaryButton("Start Ride (Prompt)", PRIMARY_COLOR);
        startBtn.addActionListener(e -> startRideAction(driver));
        quickCard.add(startBtn, q);

        q.gridx = 1;
        JButton completeBtn = createLargePrimaryButton("Complete Ride (Prompt)", PRIMARY_LIGHT);
        completeBtn.addActionListener(e -> completeRideAction(driver));
        quickCard.add(completeBtn, q);

        q.gridx = 0; q.gridy++;
        q.gridwidth = 1;
        JButton shiftsBtn = createLargePrimaryButton("View Shifts", PRIMARY_DARK);
        shiftsBtn.addActionListener(e -> viewDriverShifts(driver));
        quickCard.add(shiftsBtn, q);

        q.gridx = 1;
        JButton addShiftQuick = createLargePrimaryButton("Add Shift", PRIMARY_LIGHT);
        addShiftQuick.addActionListener(e -> addDriverShift(driver));
        quickCard.add(addShiftQuick, q);

        leftCol.add(quickCard);

        // Right: profile & vehicle card
        JPanel rightCol = new JPanel();
        rightCol.setBackground(BACKGROUND_COLOR);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JPanel profileCard = new RoundedPanel(12, CARD_COLOR);
        profileCard.setLayout(new BorderLayout(8,8));
        profileCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel pfTitle = new JLabel("Profile");
        pfTitle.setFont(FONT_SUBHEADER);
        pfTitle.setForeground(TEXT_PRIMARY);
        profileCard.add(pfTitle, BorderLayout.NORTH);
        JTextArea profileArea = new JTextArea();
        profileArea.setEditable(false);
        profileArea.setOpaque(false);
        profileArea.setText("Name: " + driver.getName() + "\nEmail: " + driver.getEmail() + "\nLicense: " + driver.getLicenseNumber());
        profileArea.setFont(FONT_BODY);
        profileCard.add(profileArea, BorderLayout.CENTER);
        JPanel profBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); profBtns.setOpaque(false);
        JButton manageVeh = createModernButton("Manage Vehicle", PRIMARY_LIGHT);
        manageVeh.addActionListener(e -> addOrUpdateVehicle(driver));
        profBtns.add(manageVeh);
        profileCard.add(profBtns, BorderLayout.SOUTH);

        rightCol.add(profileCard);

        // Layout placements
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.6; actions.add(leftCol, gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; actions.add(rightCol, gbc);

        center.add(actions, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        // Footer area with logout/delete buttons
        JPanel footerArea = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerArea.setBackground(BACKGROUND_COLOR);
        JButton delete = createModernButton("Delete Profile", new Color(0xAA,0x11,0x11));
        delete.addActionListener(ev -> {
            int res = JOptionPane.showConfirmDialog(this, "Delete your profile and all related data? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                boolean ok = userDAO.deleteUser(driver.getUserId());
                if (ok) {
                    showMessage("Deleted", "Profile deleted.", PRIMARY_COLOR);
                    showWelcomeScreen();
                } else showMessage("Error", "Failed to delete profile.", PRIMARY_DARK);
            }
        });
        JButton logout = createModernButton("Logout", PRIMARY_DARK);
        logout.addActionListener(ev -> showWelcomeScreen());
        JButton viewRidesBtn = createModernButton("View My Rides", PRIMARY_LIGHT);
        viewRidesBtn.addActionListener(ev -> viewDriverRides(driver));

        footerArea.add(delete);
        footerArea.add(viewRidesBtn);
        footerArea.add(logout);

        panel.add(footerArea, BorderLayout.SOUTH);
        return panel;
    }

    private void showRiderDashboard(Rider rider) {
        if (namedCards.containsKey("RIDER_DASH")) {
            mainCardPanel.remove(namedCards.get("RIDER_DASH"));
            namedCards.remove("RIDER_DASH");
        }
        JPanel riderPanel = createRiderDashboardPanel(rider);
        mainCardPanel.add(riderPanel, "RIDER_DASH");
        namedCards.put("RIDER_DASH", riderPanel);
        cardLayout.show(mainCardPanel, "RIDER_DASH");
    }

    private JPanel createRiderDashboardPanel(Rider rider) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Header similar to landing
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 110));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Rider Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Welcome, " + rider.getName());
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(new Color(255,255,255,200));

        JPanel text = new JPanel(new GridLayout(2,1));
        text.setOpaque(false);
        text.add(title); text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        // Center: stats and actions laid out like landing page boxes
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BACKGROUND_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        int ridesCount = 0;
        try {
            List<String> rides = rideDAO.getRidesByRider(rider.getUserId());
            ridesCount = rides == null ? 0 : rides.size();
        } catch (Exception ignored) {}
        statsRow.add(createStatCard("Wallet", String.format("PKR %.2f", rider.getBalance()), PRIMARY_COLOR));
        statsRow.add(createStatCard("My Rides", String.valueOf(ridesCount), PRIMARY_LIGHT));
        statsRow.add(createStatCard("Saved Routes", String.valueOf(routeDAO.getAllRoutes().size()), PRIMARY_DARK));

        center.add(statsRow, BorderLayout.NORTH);

        // Action area: three big boxes (Book, Wallet, Feedback)
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 18, 18);
        gbc.fill = GridBagConstraints.BOTH;

        // Book card
        JPanel bookCard = new RoundedPanel(12, CARD_COLOR);
        bookCard.setLayout(new BorderLayout(12,12));
        bookCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel bkTitle = new JLabel("Book a Ride");
        bkTitle.setFont(FONT_SUBHEADER);
        bkTitle.setForeground(TEXT_PRIMARY);
        JTextArea bkDesc = new JTextArea("Find a nearby driver, choose a route and pay with wallet, card or cash. Fast and secure.");
        bkDesc.setEditable(false); bkDesc.setOpaque(false); bkDesc.setLineWrap(true); bkDesc.setWrapStyleWord(true);
        JButton bookBtn = createLargePrimaryButton("Start Booking (Wizard)", PRIMARY_COLOR);
        bookBtn.addActionListener(e -> openBookingWizard(rider));
        bookCard.add(bkTitle, BorderLayout.NORTH);
        bookCard.add(bkDesc, BorderLayout.CENTER);
        bookCard.add(bookBtn, BorderLayout.SOUTH);

        // Wallet card
        JPanel walletCard = new RoundedPanel(12, CARD_COLOR);
        walletCard.setLayout(new BorderLayout(12,12));
        walletCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel wTitle = new JLabel("Wallet");
        wTitle.setFont(FONT_SUBHEADER);
        wTitle.setForeground(TEXT_PRIMARY);
        JTextArea wDesc = new JTextArea("Top up your wallet for faster, cashless payments.");
        wDesc.setEditable(false); wDesc.setOpaque(false); wDesc.setLineWrap(true); wDesc.setWrapStyleWord(true);
        JButton topUpBtn = createLargePrimaryButton("Top-up Wallet", PRIMARY_LIGHT);
        topUpBtn.addActionListener(e -> addMoneyToWallet(rider));
        walletCard.add(wTitle, BorderLayout.NORTH);
        walletCard.add(wDesc, BorderLayout.CENTER);
        walletCard.add(topUpBtn, BorderLayout.SOUTH);

        // Feedback card
        JPanel fbCard = new RoundedPanel(12, CARD_COLOR);
        fbCard.setLayout(new BorderLayout(12,12));
        fbCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel fTitle = new JLabel("Feedback");
        fTitle.setFont(FONT_SUBHEADER);
        fTitle.setForeground(TEXT_PRIMARY);
        JTextArea fDesc = new JTextArea("Rate drivers and submit feedback to improve service quality.");
        fDesc.setEditable(false); fDesc.setOpaque(false); fDesc.setLineWrap(true); fDesc.setWrapStyleWord(true);
        JButton fbBtn = createLargePrimaryButton("Submit Feedback", PRIMARY_LIGHT);
        fbBtn.addActionListener(e -> submitFeedback(rider));
        fbCard.add(fTitle, BorderLayout.NORTH);
        fbCard.add(fDesc, BorderLayout.CENTER);
        fbCard.add(fbBtn, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        actions.add(bookCard, gbc);
        gbc.gridx = 1;
        actions.add(walletCard, gbc);
        gbc.gridx = 2;
        actions.add(fbCard, gbc);

        center.add(actions, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        // Footer: quick links + logout
        JPanel footerArea = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerArea.setBackground(BACKGROUND_COLOR);
        JButton viewRides = createModernButton("View My Rides", PRIMARY_LIGHT);
        viewRides.addActionListener(e -> viewRiderRides(rider));
        JButton delete = createModernButton("Delete Profile", new Color(0xAA,0x11,0x11));
        delete.addActionListener(ev -> {
            int res = JOptionPane.showConfirmDialog(this, "Delete your profile and all related data? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                boolean ok = userDAO.deleteUser(rider.getUserId());
                if (ok) {
                    showMessage("Deleted", "Profile deleted.", PRIMARY_COLOR);
                    showWelcomeScreen();
                } else showMessage("Error", "Failed to delete profile.", PRIMARY_DARK);
            }
        });
        JButton logout = createModernButton("Logout", PRIMARY_DARK);
        logout.addActionListener(ev -> showWelcomeScreen());
        footerArea.add(delete);
        footerArea.add(viewRides);
        footerArea.add(logout);

        panel.add(footerArea, BorderLayout.SOUTH);
        return panel;
    }

    // ----------------- Booking Wizard (4-step) -----------------

    private void openBookingWizard(Rider rider) {
        BookingWizard wizard = new BookingWizard(this, rider);
        wizard.showDialog();
    }

    // BookingWizard inner class encapsulates the multi-step flow
    private class BookingWizard {
        private final JDialog dialog;
        private final CardLayout stepsLayout = new CardLayout();
        private final JPanel stepsPanel = new JPanel(stepsLayout);
        private final Rider rider;

        // Step components
        private JList<String> driversList;
        private DefaultListModel<String> driversModel;

        private JList<String> routesList;
        private DefaultListModel<String> routesModel;
        private JLabel farePreview;

        private JRadioButton rbCash, rbCard, rbWallet;
        private ButtonGroup paymentGroup;
        private JTextField cardField;
        private JLabel walletBalanceLabel;

        private JCheckBox assistantCheck;
        private JTextField assistantNameField;

        private JButton backBtn, nextBtn, cancelBtn;

        // internal selections
        private int selectedDriverId = -1;
        private Route selectedRoute = null;
        private String selectedPaymentMethod = "Cash";
        private double computedFare = 0.0;
        private boolean walletOk = false;

        BookingWizard(JFrame owner, Rider rider) {
            this.rider = rider;
            dialog = createCenteredDialog("Book a Ride - Wizard", 620, 520);
            dialog.setLayout(new BorderLayout());
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            initSteps();
            dialog.add(stepsPanel, BorderLayout.CENTER);
            dialog.add(createWizardControls(), BorderLayout.SOUTH);
        }

        private void initSteps() {
            stepsPanel.setBackground(BACKGROUND_COLOR);
            stepsPanel.add(createStep1_SelectDriver(), "STEP1");
            stepsPanel.add(createStep2_SelectRoute(), "STEP2");
            stepsPanel.add(createStep3_Payment(), "STEP3");
            stepsPanel.add(createStep4_AssistantReview(), "STEP4");

            stepsLayout.show(stepsPanel, "STEP1");
        }

        private JPanel createStep1_SelectDriver() {
            JPanel p = new RoundedPanel(10, CARD_COLOR);
            p.setLayout(new BorderLayout(12, 12));
            p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

            JLabel title = new JLabel("Step 1: Select Driver");
            title.setFont(FONT_SUBHEADER);
            title.setForeground(TEXT_PRIMARY);
            p.add(title, BorderLayout.NORTH);

            driversModel = new DefaultListModel<>();
            driversList = new JList<>(driversModel);
            driversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            driversList.setFont(FONT_BODY);
            JScrollPane sp = new JScrollPane(driversList);
            p.add(sp, BorderLayout.CENTER);

            // load drivers
            List<String> drivers = vehicleDAO.getAllDriversWithVehicles();
            if (drivers.isEmpty()) driversModel.addElement("No drivers available");
            else drivers.forEach(driversModel::addElement);

            driversList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String sel = driversList.getSelectedValue();
                    selectedDriverId = extractDriverIdFromListItem(sel);
                }
            });

            return p;
        }

        private JPanel createStep2_SelectRoute() {
            JPanel p = new RoundedPanel(10, CARD_COLOR);
            p.setLayout(new BorderLayout(12,12));
            p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

            JLabel title = new JLabel("Step 2: Select Route & Preview Fare");
            title.setFont(FONT_SUBHEADER);
            title.setForeground(TEXT_PRIMARY);
            p.add(title, BorderLayout.NORTH);

            routesModel = new DefaultListModel<>();
            routesList = new JList<>(routesModel);
            routesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            routesList.setFont(FONT_BODY);

            // ensure routes seeded
            List<Route> currentRoutes = routeDAO.getAllRoutes();
            if (currentRoutes.isEmpty()) {
                routeDAO.addRoute("Main Campus", "Engineering Block", 2.5);
                routeDAO.addRoute("Library", "Student Center", 1.8);
                routeDAO.addRoute("Hostel A", "Science Building", 3.2);
            }
            final List<Route> routes = routeDAO.getAllRoutes();
            for (Route r : routes) routesModel.addElement(r.toString());

            JScrollPane sp = new JScrollPane(routesList);
            p.add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout(8,8));
            farePreview = new JLabel("Fare: PKR 0.00");
            farePreview.setFont(FONT_BODY);
            farePreview.setForeground(TEXT_SECONDARY);

            routesList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int idx = routesList.getSelectedIndex();
                    if (idx >= 0 && idx < routes.size()) {
                        selectedRoute = routes.get(idx);
                        computedFare = calculateFare(selectedRoute.getDistanceKm());
                        farePreview.setText(String.format("Fare: PKR %.2f", computedFare));
                    } else {
                        selectedRoute = null;
                        computedFare = 0;
                        farePreview.setText("Fare: PKR 0.00");
                    }
                }
            });

            bottom.add(farePreview, BorderLayout.WEST);
            p.add(bottom, BorderLayout.SOUTH);
            return p;
        }

        private JPanel createStep3_Payment() {
            JPanel p = new RoundedPanel(10, CARD_COLOR);
            p.setLayout(new GridBagLayout());
            p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8,8,8,8);
            gbc.gridx=0; gbc.gridy=0; gbc.anchor = GridBagConstraints.WEST;

            JLabel title = new JLabel("Step 3: Payment");
            title.setFont(FONT_SUBHEADER);
            title.setForeground(TEXT_PRIMARY);
            gbc.gridwidth = 2; p.add(title, gbc);

            gbc.gridwidth=1; gbc.gridy++;
            rbCash = new JRadioButton("Cash");
            rbCard = new JRadioButton("Card");
            rbWallet = new JRadioButton("Wallet");
            rbCash.setFont(FONT_BODY); rbCard.setFont(FONT_BODY); rbWallet.setFont(FONT_BODY);
            paymentGroup = new ButtonGroup();
            paymentGroup.add(rbCash); paymentGroup.add(rbCard); paymentGroup.add(rbWallet);
            rbCash.setSelected(true);

            JPanel pmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pmPanel.setBackground(CARD_COLOR);
            pmPanel.add(rbCash); pmPanel.add(rbCard); pmPanel.add(rbWallet);
            gbc.gridy++; gbc.gridwidth=2; p.add(pmPanel, gbc);

            gbc.gridwidth=1; gbc.gridy++;
            JLabel cardLbl = new JLabel("Card Number:");
            cardLbl.setFont(FONT_BODY);
            cardField = new JTextField(22);
            cardField.setFont(FONT_BODY);
            cardField.setEnabled(false);
            p.add(cardLbl, gbc); gbc.gridx=1; p.add(cardField, gbc);

            gbc.gridx=0; gbc.gridy++; JLabel walletLbl = new JLabel("Wallet Balance:");
            walletLbl.setFont(FONT_BODY);
            walletBalanceLabel = new JLabel("PKR " + String.format("%.2f", rider.getBalance()));
            walletBalanceLabel.setFont(FONT_BODY);
            JButton topUpBtn = createModernButton("Top-up", PRIMARY_LIGHT);
            topUpBtn.setPreferredSize(new Dimension(90, 30));
            JPanel walletPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            walletPanel.setBackground(CARD_COLOR);
            walletPanel.add(walletBalanceLabel); walletPanel.add(topUpBtn);
            p.add(walletLbl, gbc); gbc.gridx=1; p.add(walletPanel, gbc);

            // listeners
            rbCard.addActionListener(e -> cardField.setEnabled(true));
            rbCash.addActionListener(e -> cardField.setEnabled(false));
            rbWallet.addActionListener(e -> cardField.setEnabled(false));

            topUpBtn.addActionListener(e -> {
                String amt = JOptionPane.showInputDialog(dialog, "Enter amount to add to wallet (PKR):", "Top-up", JOptionPane.PLAIN_MESSAGE);
                if (amt == null) return;
                try {
                    double a = Double.parseDouble(amt.trim());
                    if (a <= 0) { showMessage("Error", "Invalid amount", PRIMARY_DARK); return; }
                    rider.addBalance(a);
                    userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
                    walletBalanceLabel.setText("PKR " + String.format("%.2f", rider.getBalance()));
                    showMessage("Success", "Wallet topped up", PRIMARY_COLOR);
                    // refresh rider dashboard if currently visible so overview reflects new balance
                    try { showRiderDashboard(rider); } catch (Exception ignored) {}
                } catch (Exception ex) { showMessage("Error", "Invalid number", PRIMARY_DARK); }
            });

            return p;
        }

        private JPanel createStep4_AssistantReview() {
            JPanel p = new RoundedPanel(10, CARD_COLOR);
            p.setLayout(new BorderLayout(12,12));
            p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
            JLabel title = new JLabel("Step 4: Assistant & Review");
            title.setFont(FONT_SUBHEADER); title.setForeground(TEXT_PRIMARY);
            p.add(title, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridBagLayout());
            center.setBackground(CARD_COLOR);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

            assistantCheck = new JCheckBox("Add an assistant/companion");
            assistantCheck.setBackground(CARD_COLOR);
            assistantCheck.setFont(FONT_BODY);
            center.add(assistantCheck, gbc);

            gbc.gridy++;
            assistantNameField = new JTextField();
            assistantNameField.setEnabled(false);
            center.add(assistantNameField, gbc);

            assistantCheck.addActionListener(e -> assistantNameField.setEnabled(assistantCheck.isSelected()));

            gbc.gridy++;
            JTextArea reviewArea = new JTextArea(8, 44);
            reviewArea.setEditable(false);
            reviewArea.setFont(FONT_BODY);
            reviewArea.setLineWrap(true);
            reviewArea.setWrapStyleWord(true);

            // update review when this step becomes visible
            stepsPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    // no-op
                }
            });

            // We'll update review when Next/Back pressed in controller
            JPanel revWrap = new JPanel(new BorderLayout());
            revWrap.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
            p.add(center, BorderLayout.WEST);
            p.add(revWrap, BorderLayout.CENTER);

            // store reviewArea for later update
            reviewTextAreaRef = reviewArea;
            return p;
        }

        private JTextArea reviewTextAreaRef;

        private JPanel createWizardControls() {
            JPanel controls = new JPanel(new BorderLayout());
            controls.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            controls.setBackground(BACKGROUND_COLOR);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
            left.setBackground(BACKGROUND_COLOR);
            JLabel stepHint = new JLabel("Wizard: 1 → 2 → 3 → 4");
            stepHint.setFont(FONT_SMALL);
            left.add(stepHint);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setBackground(BACKGROUND_COLOR);

            backBtn = createModernButton("Back", PRIMARY_LIGHT);
            nextBtn = createModernButton("Next", PRIMARY_COLOR);
            cancelBtn = createModernButton("Cancel", PRIMARY_DARK);

            backBtn.setEnabled(false);

            backBtn.addActionListener(e -> stepBack());
            nextBtn.addActionListener(e -> stepNext());
            cancelBtn.addActionListener(e -> dialog.dispose());

            right.add(backBtn);
            right.add(nextBtn);
            right.add(cancelBtn);

            controls.add(left, BorderLayout.WEST);
            controls.add(right, BorderLayout.EAST);
            return controls;
        }

        void showDialog() {
            dialog.setVisible(true);
        }

        private void stepBack() {
            if (getCurrentStepName().equals("STEP2")) {
                stepsLayout.show(stepsPanel, "STEP1");
                backBtn.setEnabled(false);
                nextBtn.setText("Next");
            } else if (getCurrentStepName().equals("STEP3")) {
                stepsLayout.show(stepsPanel, "STEP2");
            } else if (getCurrentStepName().equals("STEP4")) {
                stepsLayout.show(stepsPanel, "STEP3");
                nextBtn.setText("Next");
            }
            updateReviewIfNeeded();
        }

        private void stepNext() {
            String step = getCurrentStepName();
            if (step.equals("STEP1")) {
                // validate driver selection
                if (driversList.getSelectedIndex() < 0 || driversModel.isEmpty() || driversList.getSelectedValue().startsWith("No drivers")) {
                    showMessage("Error", "Please select a driver.", PRIMARY_DARK);
                    return;
                }
                selectedDriverId = extractDriverIdFromListItem(driversList.getSelectedValue());
                // Availability checks: vehicle, shift, and in-progress ride
                if (!driverHasVehicle(selectedDriverId)) {
                    showMessage("Unavailable", "Selected driver has no vehicle registered. Please choose another driver.", PRIMARY_DARK);
                    return;
                }
                if (!driverHasActiveShiftNow(selectedDriverId)) {
                    showMessage("Unavailable", "Selected driver is not on shift right now. Please choose another driver.", PRIMARY_DARK);
                    return;
                }
                if (driverHasInProgressRide(selectedDriverId)) {
                    showMessage("Busy", "Selected driver currently has an in-progress ride. Please choose another driver.", PRIMARY_DARK);
                    return;
                }
                stepsLayout.show(stepsPanel, "STEP2");
                backBtn.setEnabled(true);
            } else if (step.equals("STEP2")) {
                if (routesList.getSelectedIndex() < 0) {
                    showMessage("Error", "Please select a route.", PRIMARY_DARK);
                    return;
                }
                // selectedRoute and computedFare set by selection listener
                stepsLayout.show(stepsPanel, "STEP3");
            } else if (step.equals("STEP3")) {
                // validate payment
                if (rbCard.isSelected()) {
                    selectedPaymentMethod = "Card";
                    String cardNum = cardField.getText().trim();
                    if (cardNum.isEmpty()) { showMessage("Error", "Enter card number.", PRIMARY_DARK); return; }
                } else if (rbWallet.isSelected()) {
                    selectedPaymentMethod = "Wallet";
                    walletOk = rider.getBalance() >= computedFare;
                    if (!walletOk) {
                        int res = JOptionPane.showConfirmDialog(dialog, "Insufficient wallet balance. Top-up now?", "Wallet Low", JOptionPane.YES_NO_OPTION);
                        if (res == JOptionPane.YES_OPTION) {
                            String amt = JOptionPane.showInputDialog(dialog, "Amount to add (PKR):");
                            if (amt == null) return;
                            try {
                                double a = Double.parseDouble(amt.trim());
                                if (a <= 0) { showMessage("Error", "Invalid amount", PRIMARY_DARK); return; }
                                rider.addBalance(a);
                                userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
                                walletBalanceLabel.setText("PKR " + String.format("%.2f", rider.getBalance()));
                                walletOk = rider.getBalance() >= computedFare;
                                if (!walletOk) { showMessage("Error", "Still insufficient balance", PRIMARY_DARK); return; }
                            } catch (Exception ex) { showMessage("Error", "Invalid number", PRIMARY_DARK); return; }
                        } else return;
                    }
                } else {
                    selectedPaymentMethod = "Cash";
                }
                stepsLayout.show(stepsPanel, "STEP4");
                nextBtn.setText("Confirm");
                updateReviewIfNeeded();
            } else if (step.equals("STEP4")) {
                // Confirm booking
                // final validations
                if (selectedDriverId <= 0 || selectedRoute == null || computedFare <= 0) {
                    showMessage("Error", "Invalid booking state", PRIMARY_DARK); return;
                }
                // Re-check availability just before booking to avoid race conditions
                if (!isDriverAvailableNow(selectedDriverId)) {
                    showMessage("Unavailable", "Driver is not available at the moment. Please choose another driver.", PRIMARY_DARK);
                    return;
                }
                String assistantName = assistantCheck.isSelected() ? assistantNameField.getText().trim() : null;

                int rideId = rideDAO.createRide(rider.getUserId(), selectedDriverId, selectedRoute.getRouteId(), computedFare);
                if (rideId <= 0) {
                    showMessage("Error", "Failed to create ride", PRIMARY_DARK); return;
                }

                boolean paymentSuccess = false;
                if ("Wallet".equals(selectedPaymentMethod)) {
                    if (walletOk && rider.deductBalance(computedFare)) {
                        userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
                        paymentSuccess = true;
                    } else paymentSuccess = false;
                } else if ("Card".equals(selectedPaymentMethod)) {
                    paymentSuccess = true; // simulation
                } else paymentSuccess = true; // cash

                String payStatus = paymentSuccess ? "Completed" : "Failed";
                int payId = paymentDAO.createPayment(rideId, computedFare, selectedPaymentMethod, payStatus);

                if (assistantName != null && !assistantName.isEmpty()) {
                    assistantDAO.addAssistant(rideId, rider.getUserId(), assistantName);
                }

                String msg = String.format("Ride booked successfully! ID: %d\nPayment status: %s\nPayment record: %s", rideId, payStatus, payId > 0 ? ("ID " + payId) : "Not recorded");
                showMessage("Success", msg, PRIMARY_COLOR);
                dialog.dispose();
            }
        }

        private void updateReviewIfNeeded() {
            if (reviewTextAreaRef == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Driver: ").append(selectedDriverId > 0 ? selectedDriverId : "Not selected").append("\n");
            sb.append("Route: ").append(selectedRoute != null ? selectedRoute.toString() : "Not selected").append("\n");
            sb.append(String.format("Fare: PKR %.2f\n", computedFare));
            sb.append("Payment: ").append(selectedPaymentMethod).append("\n");
            sb.append("Wallet Balance: PKR ").append(String.format("%.2f", rider.getBalance())).append("\n");
            sb.append("Assistant: ").append((assistantCheck != null && assistantCheck.isSelected() && assistantNameField != null && !assistantNameField.getText().trim().isEmpty()) ? assistantNameField.getText().trim() : "None").append("\n");
            reviewTextAreaRef.setText(sb.toString());
        }

        private String getCurrentStepName() {
            for (Component c : stepsPanel.getComponents()) {
                if (c.isVisible()) {
                    // find which card is visible by comparing bounds - fallback is to track current step
                }
            }
            // CardLayout doesn't expose currently visible card name; we can track via next/back changes,
            // but for brevity use checking of enabled states/buttons:
            // We'll infer by checking if backBtn is disabled => STEP1, nextBtn's text => confirm => STEP4 else find selection
            if (!backBtn.isEnabled()) return "STEP1";
            if ("Confirm".equals(nextBtn.getText())) return "STEP4";
            // fallback: detect via which component visible
            for (Component comp : stepsPanel.getComponents()) {
                if (comp.isVisible()) {
                    if (comp == stepsPanel.getComponent(0)) return "STEP1";
                    if (comp == stepsPanel.getComponent(1)) return "STEP2";
                    if (comp == stepsPanel.getComponent(2)) return "STEP3";
                    if (comp == stepsPanel.getComponent(3)) return "STEP4";
                }
            }
            return "STEP1";
        }
    }

    // utility: extract driver id from string returned by vehicleDAO.getAllDriversWithVehicles
    private int extractDriverIdFromListItem(String item) {
        if (item == null) return -1;
        // try to find digits at start or first number
        try {
            String digits = item.replaceAll("^\\D*(\\d+).*", "$1");
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return -1;
        }
    }

    private double calculateFare(double km) {
        return 100 + km * 50;
    }

    // ----------------- Availability helpers -----------------
    private boolean driverHasVehicle(int driverId) {
        try {
            Vehicle v = vehicleDAO.getVehicleByDriverId(driverId);
            return v != null;
        } catch (Exception e) { return false; }
    }

    private boolean driverHasInProgressRide(int driverId) {
        try {
            return rideDAO.hasInProgressRideForDriver(driverId);
        } catch (Exception e) { return false; }
    }

    private boolean driverHasActiveShiftNow(int driverId) {
        try {
            var shifts = shiftDAO.getShiftsByDriver(driverId);
            if (shifts == null || shifts.isEmpty()) return false;
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            for (Object o : shifts) {
                try {
                    DriverShift s = (DriverShift) o;
                    java.sql.Date sd = s.getShiftDate();
                    java.sql.Time st = s.getStartTime();
                    java.sql.Time et = s.getEndTime();
                    if (sd == null || st == null || et == null) continue;
                    LocalDate shiftDate = sd.toLocalDate();
                    LocalTime start = st.toLocalTime();
                    LocalTime end = et.toLocalTime();
                    if (today.equals(shiftDate) && (!now.isBefore(start) && !now.isAfter(end))) return true;
                } catch (Exception ignored) {}
            }
            return false;
        } catch (Exception e) { return false; }
    }

    private boolean isDriverAvailableNow(int driverId) {
        if (!driverHasVehicle(driverId)) return false;
        if (!driverHasActiveShiftNow(driverId)) return false;
        if (driverHasInProgressRide(driverId)) return false;
        return true;
    }

    // ----------------- GUI equivalents of CLI actions -----------------

    private void showLargeText(String title, String text) {
        JDialog d = createCenteredDialog(title, 640, 480);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND_COLOR);
        JTextArea ta = new JTextArea(text);
        ta.setEditable(false);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        p.add(new JScrollPane(ta), BorderLayout.CENTER);
        JButton ok = createModernButton("Close", PRIMARY_DARK);
        ok.addActionListener(e -> d.dispose());
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER)); btn.setBackground(BACKGROUND_COLOR); btn.add(ok);
        p.add(btn, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }

    private void addOrUpdateVehicle(Driver driver) {
        JDialog d = createCenteredDialog("Add / Update Vehicle", 460, 380);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(BACKGROUND_COLOR);
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

        JTextField modelF = createStyledTextField();
        JTextField plateF = createStyledTextField();
        JTextField capF = createStyledTextField();
        JTextField colorF = createStyledTextField();

        p.add(new JLabel("Model:"), gbc); gbc.gridy++; p.add(modelF, gbc);
        gbc.gridy++; p.add(new JLabel("Plate Number:"), gbc); gbc.gridy++; p.add(plateF, gbc);
        gbc.gridy++; p.add(new JLabel("Capacity:"), gbc); gbc.gridy++; p.add(capF, gbc);
        gbc.gridy++; p.add(new JLabel("Color:"), gbc); gbc.gridy++; p.add(colorF, gbc);

        gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(BACKGROUND_COLOR);
        JButton save = createModernButton("Save Vehicle", PRIMARY_COLOR);
        JButton cancel = createModernButton("Cancel", PRIMARY_DARK);
        save.addActionListener(e -> {
            try {
                String model = modelF.getText().trim();
                String plate = plateF.getText().trim();
                int cap = Integer.parseInt(capF.getText().trim());
                String color = colorF.getText().trim();
                if (model.isEmpty() || plate.isEmpty() || color.isEmpty()) { showMessage("Error", "All fields required", PRIMARY_DARK); return; }
                int vid = vehicleDAO.addVehicle(driver.getUserId(), model, plate, cap, color);
                if (vid > 0) {
                    driver.setVehicle(new Vehicle(vid, model, plate, cap, color));
                    showMessage("Success", "Vehicle saved.", PRIMARY_COLOR);
                    d.dispose();
                    // refresh driver dashboard if visible
                    try { showDriverDashboard(driver); } catch (Exception ignored) {}
                } else showMessage("Error", "Failed to save vehicle.", PRIMARY_DARK);
            } catch (NumberFormatException nfe) { showMessage("Error", "Capacity must be a number", PRIMARY_DARK); }
        });
        cancel.addActionListener(e -> d.dispose());
        btns.add(save); btns.add(cancel);
        gbc.gridy++; p.add(btns, gbc);

        d.add(p); d.setVisible(true);
    }

    private void startRideAction(Driver driver) {
        // Validate driver can start rides: must have vehicle, be on shift now, and not already have another in-progress ride
        int did = driver.getUserId();
        if (!driverHasVehicle(did)) { showMessage("Unavailable", "You must add a vehicle before starting rides.", PRIMARY_DARK); return; }
        if (!driverHasActiveShiftNow(did)) { showMessage("Unavailable", "You are not scheduled for a shift right now. Add a shift or try within your shift times.", PRIMARY_DARK); return; }
        if (driverHasInProgressRide(did)) { showMessage("Busy", "You already have an in-progress ride. Complete it before starting another.", PRIMARY_DARK); return; }

        // Present list of pending/confirmed rides assigned to this driver
        var candidates = rideDAO.getPendingOrConfirmedRidesForDriver(did);
        if (candidates == null || candidates.isEmpty()) {
            showMessage("Info", "No pending/confirmed rides available to start.", PRIMARY_DARK);
            return;
        }
        JList<String> list = new JList<>(candidates.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Select ride to start", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String sel = list.getSelectedValue();
        if (sel == null) { showMessage("Error", "No ride selected.", PRIMARY_DARK); return; }
        int rideId = extractDriverIdFromListItem(sel);
        boolean ok = rideDAO.startRideTransaction(rideId, driver.getUserId());
        showMessage(ok ? "Success" : "Error", ok ? "Ride marked In Progress." : "Failed to mark ride as In Progress.", ok ? PRIMARY_COLOR : PRIMARY_DARK);
        try { showDriverDashboard(driver); } catch (Exception ignored) {}
    }

    private void completeRideAction(Driver driver) {
        // Show in-progress rides for this driver
        int did = driver.getUserId();
        var inProg = rideDAO.getInProgressRidesForDriver(did);
        if (inProg == null || inProg.isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this, "No started rides found. Start a ride now?", "No In-Progress Rides", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                startRideAction(driver);
            }
            return;
        }
        JList<String> list = new JList<>(inProg.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Select in-progress ride to complete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String sel = list.getSelectedValue();
        if (sel == null) { showMessage("Error", "No ride selected.", PRIMARY_DARK); return; }
        int rideId = extractDriverIdFromListItem(sel);
        double fare = rideDAO.getFareByRideId(rideId);
        if (fare < 0) { showMessage("Error", "Could not determine fare for ride.", PRIMARY_DARK); return; }
        boolean ok = rideDAO.completeRideTransaction(rideId, driver.getUserId(), fare);
        if (ok) {
            try { driver.addEarnings(fare); } catch (Exception ignored) {}
            showMessage("Success", "Ride completed and earnings updated.", PRIMARY_COLOR);
            try { showDriverDashboard(driver); } catch (Exception ignored) {}
        } else showMessage("Error", "Failed to complete ride.", PRIMARY_DARK);
    }

    private void viewDriverShifts(Driver driver) {
        var shifts = shiftDAO.getShiftsByDriver(driver.getUserId());
        if (shifts == null || shifts.isEmpty()) {
            showMessage("Shifts", "No shifts set.", PRIMARY_DARK);
            return;
        }

        JDialog d = createCenteredDialog("Your Shifts", 700, 420);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND_COLOR);

        String[] cols = new String[]{"ID", "Date", "Start", "End"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Object o : shifts) {
            DriverShift ds = (DriverShift) o;
            model.addRow(new Object[]{ds.getShiftId(), ds.getShiftDate(), ds.getStartTime(), ds.getEndTime()});
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(BACKGROUND_COLOR);
        JButton endBtn = createModernButton("End Selected Shift Now", PRIMARY_COLOR);
        JButton closeBtn = createModernButton("Close", PRIMARY_DARK);

        endBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { showMessage("Error", "Please select a shift to end.", PRIMARY_DARK); return; }
            int shiftId = (int) model.getValueAt(sel, 0);
            int confirm = JOptionPane.showConfirmDialog(d, "End the selected shift now? This will set its end time to the current time.", "Confirm End Shift", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            java.sql.Time now = java.sql.Time.valueOf(LocalTime.now());
            boolean ok = shiftDAO.endShift(shiftId, now);
            if (ok) {
                showMessage("Success", "Shift ended at " + now.toString(), PRIMARY_COLOR);
                // refresh table
                var refreshed = shiftDAO.getShiftsByDriver(driver.getUserId());
                model.setRowCount(0);
                for (Object o2 : refreshed) {
                    DriverShift ds2 = (DriverShift) o2;
                    model.addRow(new Object[]{ds2.getShiftId(), ds2.getShiftDate(), ds2.getStartTime(), ds2.getEndTime()});
                }
            } else {
                showMessage("Error", "Failed to end shift.", PRIMARY_DARK);
            }
        });

        closeBtn.addActionListener(e -> d.dispose());
        btns.add(endBtn); btns.add(closeBtn);
        p.add(btns, BorderLayout.SOUTH);

        d.add(p); d.setVisible(true);
    }

    private void addDriverShift(Driver driver) {
        JDialog d = createCenteredDialog("Add Shift", 420, 300);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(BACKGROUND_COLOR); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
        JTextField dateF = createStyledTextField(); applyPlaceholder(dateF, "YYYY-MM-DD");
        JTextField startF = createStyledTextField(); applyPlaceholder(startF, "HH:MM:SS");
        JTextField endF = createStyledTextField(); applyPlaceholder(endF, "HH:MM:SS");
        p.add(new JLabel("Shift Date (YYYY-MM-DD):"), gbc); gbc.gridy++; p.add(dateF, gbc);
        gbc.gridy++; p.add(new JLabel("Start Time (HH:MM:SS):"), gbc); gbc.gridy++; p.add(startF, gbc);
        gbc.gridy++; p.add(new JLabel("End Time (HH:MM:SS):"), gbc); gbc.gridy++; p.add(endF, gbc);
        gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(BACKGROUND_COLOR);
        JButton add = createModernButton("Add", PRIMARY_COLOR); JButton cancel = createModernButton("Cancel", PRIMARY_DARK);
        add.addActionListener(e -> {
            try {
                java.sql.Date date = java.sql.Date.valueOf(dateF.getText().trim());
                java.sql.Time start = java.sql.Time.valueOf(startF.getText().trim());
                java.sql.Time end = java.sql.Time.valueOf(endF.getText().trim());
                boolean ok = shiftDAO.addShift(driver.getUserId(), date, start, end);
                showMessage(ok ? "Success" : "Error", ok ? "Shift added." : "Failed to add shift.", ok ? PRIMARY_COLOR : PRIMARY_DARK);
                if (ok) { d.dispose(); try { showDriverDashboard(driver); } catch (Exception ignored) {} }
            } catch (Exception ex) {
                // Fallback: ask for combined input to reduce parsing errors
                String combined = JOptionPane.showInputDialog(d, "Invalid format. Enter shift as: YYYY-MM-DD HH:MM:SS-HH:MM:SS", "Fallback Input", JOptionPane.PLAIN_MESSAGE);
                if (combined == null) return;
                try {
                    // expected: 2025-11-26 09:00:00-17:00:00
                    String[] parts = combined.trim().split("\\s+");
                    if (parts.length != 2) throw new IllegalArgumentException("Bad format");
                    java.sql.Date date = java.sql.Date.valueOf(parts[0]);
                    String[] times = parts[1].split("-");
                    if (times.length != 2) throw new IllegalArgumentException("Bad time range");
                    java.sql.Time start = java.sql.Time.valueOf(times[0]);
                    java.sql.Time end = java.sql.Time.valueOf(times[1]);
                    boolean ok = shiftDAO.addShift(driver.getUserId(), date, start, end);
                    showMessage(ok ? "Success" : "Error", ok ? "Shift added." : "Failed to add shift.", ok ? PRIMARY_COLOR : PRIMARY_DARK);
                    if (ok) { d.dispose(); try { showDriverDashboard(driver); } catch (Exception ignored) {} }
                } catch (Exception ex2) { showMessage("Error", "Invalid date/time format.", PRIMARY_DARK); }
            }
        });
        cancel.addActionListener(e -> d.dispose()); btns.add(add); btns.add(cancel); gbc.gridy++; p.add(btns, gbc);
        d.add(p); d.setVisible(true);
    }

    private void addMoneyToWallet(Rider rider) {
        String amt = JOptionPane.showInputDialog(this, "Enter amount to add (PKR):", "Top-up Wallet", JOptionPane.PLAIN_MESSAGE);
        if (amt == null) return;
        try {
            double a = Double.parseDouble(amt.trim());
            if (a <= 0) { showMessage("Error", "Invalid amount", PRIMARY_DARK); return; }
            rider.addBalance(a);
            userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
            showMessage("Success", "Added successfully. New balance: PKR " + String.format("%.2f", rider.getBalance()), PRIMARY_COLOR);
            // refresh rider dashboard so overview wallet balance updates
            try { showRiderDashboard(rider); } catch (Exception ignored) {}
        } catch (Exception e) { showMessage("Error", "Invalid number", PRIMARY_DARK); }
    }

    private void viewRiderRides(Rider rider) {
        List<String> rides = rideDAO.getRidesByRider(rider.getUserId());
        if (rides == null || rides.isEmpty()) { showMessage("My Rides", "No rides yet!", PRIMARY_DARK); return; }

        JDialog d = createCenteredDialog("My Rides", 760, 420);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BACKGROUND_COLOR);

        String[] cols = new String[]{"Ride ID", "Route", "Driver/Rider", "Fare", "Status", "Time"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        var table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        Runnable populate = () -> {
            model.setRowCount(0);
            for (String s : rideDAO.getRidesByRider(rider.getUserId())) {
                try {
                    String[] parts = s.split(" \\| ");
                    if (parts.length < 6) continue;
                    String id = parts[0].replace("Ride#", "").trim();
                    String route = parts[1].trim();
                    String other = parts[2].contains(":") ? parts[2].split(":",2)[1].trim() : parts[2].trim();
                    String fare = parts[3].replace("Fare: PKR", "").trim();
                    String status = parts[4].replace("Status:", "").trim();
                    String time = parts[5].replace("Time:", "").trim();
                    model.addRow(new Object[]{id, route, other, "PKR " + fare, status, time});
                } catch (Exception ex) { /* skip malformed */ }
            }
        };

        populate.run();

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(BACKGROUND_COLOR);
        JButton cancelBtn = createModernButton("Cancel Selected Ride", PRIMARY_DARK);
        JButton close = createModernButton("Close", PRIMARY_DARK);

        cancelBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showMessage("Error", "Please select a ride to cancel.", PRIMARY_DARK); return; }
            int modelRow = table.convertRowIndexToModel(r);
            String idS = model.getValueAt(modelRow, 0).toString();
            String status = model.getValueAt(modelRow, 4).toString();
            if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("In Progress")) {
                showMessage("Cannot Cancel", "Ride is already In Progress or Completed.", PRIMARY_DARK); return;
            }
            int conf = JOptionPane.showConfirmDialog(d, "Cancel ride #" + idS + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
            try {
                int id = Integer.parseInt(idS);
                boolean ok = rideDAO.cancelRide(id);
                if (ok) { showMessage("Cancelled", "Ride cancelled.", PRIMARY_COLOR); populate.run(); }
                else showMessage("Error", "Failed to cancel ride (it may be already in-progress/completed).", PRIMARY_DARK);
            } catch (Exception ex) { showMessage("Error", "Invalid ride id.", PRIMARY_DARK); }
        });

        close.addActionListener(e -> d.dispose());
        btns.add(cancelBtn); btns.add(close);
        p.add(btns, BorderLayout.SOUTH);
        d.add(p); d.setVisible(true);
    }

    private void viewDriverRides(Driver driver) {
        List<String> rides = rideDAO.getRidesByDriver(driver.getUserId());
        if (rides == null || rides.isEmpty()) { showMessage("My Rides", "No rides yet!", PRIMARY_DARK); return; }

        JDialog d = createCenteredDialog("My Rides", 820, 460);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BACKGROUND_COLOR);

        String[] cols = new String[]{"Ride ID", "Route", "Rider", "Fare", "Status", "Time"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        Runnable populate = () -> {
            model.setRowCount(0);
            for (String s : rideDAO.getRidesByDriver(driver.getUserId())) {
                try {
                    String[] parts = s.split(" \\| ");
                    if (parts.length < 6) continue;
                    String id = parts[0].replace("Ride#", "").trim();
                    String route = parts[1].trim();
                    String other = parts[2].contains(":") ? parts[2].split(":",2)[1].trim() : parts[2].trim();
                    String fare = parts[3].replace("Fare: PKR", "").trim();
                    String status = parts[4].replace("Status:", "").trim();
                    String time = parts[5].replace("Time:", "").trim();
                    model.addRow(new Object[]{id, route, other, "PKR " + fare, status, time});
                } catch (Exception ex) { /* skip malformed */ }
            }
        };

        populate.run();

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setBackground(BACKGROUND_COLOR);
        JButton confirmBtn = createModernButton("Confirm Selected Ride", PRIMARY_COLOR);
        JButton cancelBtn = createModernButton("Cancel Selected Ride", new Color(0xAA,0x11,0x11));
        JButton close = createModernButton("Close", PRIMARY_DARK);

        confirmBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showMessage("Error", "Please select a ride to confirm.", PRIMARY_DARK); return; }
            int modelRow = table.convertRowIndexToModel(r);
            String idS = model.getValueAt(modelRow, 0).toString();
            String status = model.getValueAt(modelRow, 4).toString();
            if (!status.equalsIgnoreCase("Pending")) { showMessage("Cannot Confirm", "Only Pending rides can be confirmed.", PRIMARY_DARK); return; }
            try {
                int id = Integer.parseInt(idS);
                boolean ok = rideDAO.confirmRide(id);
                if (ok) { showMessage("Confirmed", "Ride confirmed.", PRIMARY_COLOR); populate.run(); }
                else showMessage("Error", "Failed to confirm ride.", PRIMARY_DARK);
            } catch (Exception ex) { showMessage("Error", "Invalid ride id.", PRIMARY_DARK); }
        });

        cancelBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showMessage("Error", "Please select a ride to cancel.", PRIMARY_DARK); return; }
            int modelRow = table.convertRowIndexToModel(r);
            String idS = model.getValueAt(modelRow, 0).toString();
            String status = model.getValueAt(modelRow, 4).toString();
            if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("In Progress")) {
                showMessage("Cannot Cancel", "Ride is already In Progress or Completed.", PRIMARY_DARK); return;
            }
            int conf = JOptionPane.showConfirmDialog(d, "Cancel ride #" + idS + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
            try {
                int id = Integer.parseInt(idS);
                boolean ok = rideDAO.cancelRide(id);
                if (ok) { showMessage("Cancelled", "Ride cancelled.", PRIMARY_COLOR); populate.run(); }
                else showMessage("Error", "Failed to cancel ride.", PRIMARY_DARK);
            } catch (Exception ex) { showMessage("Error", "Invalid ride id.", PRIMARY_DARK); }
        });

        close.addActionListener(e -> d.dispose());
        btns.add(confirmBtn); btns.add(cancelBtn); btns.add(close);
        p.add(btns, BorderLayout.SOUTH);
        d.add(p); d.setVisible(true);
    }

    private void submitFeedback(Rider rider) {
        // show rider rides and prompt for ride id, rating and comments
        List<String> rides = rideDAO.getRidesByRider(rider.getUserId());
        if (rides == null || rides.isEmpty()) { showMessage("Feedback", "No rides to provide feedback for.", PRIMARY_DARK); return; }
        showLargeText("Your Rides", String.join("\n", rides));
        String rid = JOptionPane.showInputDialog(this, "Enter Ride ID to submit feedback for:", "Submit Feedback", JOptionPane.PLAIN_MESSAGE);
        if (rid == null) return;
        try {
            int rideId = Integer.parseInt(rid.trim());
            String ratingS = JOptionPane.showInputDialog(this, "Rating (1-5):", "Rating", JOptionPane.PLAIN_MESSAGE);
            if (ratingS == null) return;
            int rating = Integer.parseInt(ratingS.trim());
            if (rating < 1 || rating > 5) { showMessage("Error", "Rating must be between 1 and 5", PRIMARY_DARK); return; }
            String comments = JOptionPane.showInputDialog(this, "Comments:", "Comments", JOptionPane.PLAIN_MESSAGE);
            if (comments == null) comments = "";
            int fbId = feedbackDAO.createFeedback(rideId, rating, comments);
            if (fbId > 0) showMessage("Success", "Feedback submitted. ID: " + fbId, PRIMARY_COLOR);
            else showMessage("Error", "Failed to submit feedback.", PRIMARY_DARK);
        } catch (Exception e) { showMessage("Error", "Invalid input.", PRIMARY_DARK); }
    }

    private void viewAllRoutes() {
        List<Route> routes = routeDAO.getAllRoutes();
        if (routes == null || routes.isEmpty()) showMessage("Routes", "No routes!", PRIMARY_DARK);
        else {
            StringBuilder sb = new StringBuilder();
            for (Route r : routes) sb.append(r.toString()).append("\n");
            showLargeText("All Routes", sb.toString());
        }
    }

    private void viewAllDrivers() {
        List<String> drivers = vehicleDAO.getAllDriversWithVehicles();
        if (drivers == null || drivers.isEmpty()) showMessage("Drivers", "No drivers!", PRIMARY_DARK);
        else showLargeText("All Drivers", String.join("\n", drivers));
    }

    // ----------------- Small custom rounded panel -----------------

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color bg) {
            super();
            this.radius = radius;
            this.backgroundColor = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ----------------- Main -----------------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Rydex());
    }
}