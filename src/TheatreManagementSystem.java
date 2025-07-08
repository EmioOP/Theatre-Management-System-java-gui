// Theatre Management System
// Main Application Class
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class TheatreManagementSystem extends JFrame {
    private Connection connection;
    private JTabbedPane tabbedPane;
    
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/theatre_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";
    
    public TheatreManagementSystem() {
        initializeDatabase();
        setupUI();
        setTitle("Theatre Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeDatabase() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        String[] tables = {
            // Customers table
            """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE,
                phone VARCHAR(15),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Theatres table
            """
            CREATE TABLE IF NOT EXISTS theatres (
                theatre_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                location VARCHAR(200),
                total_seats INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Shows table
            """
            CREATE TABLE IF NOT EXISTS shows (
                show_id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(150) NOT NULL,
                description TEXT,
                genre VARCHAR(50),
                duration_minutes INT,
                ticket_price DECIMAL(10,2),
                theatre_id INT,
                show_date DATE,
                show_time TIME,
                available_seats INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (theatre_id) REFERENCES theatres(theatre_id)
            )
            """,
            
            // Bookings table
            """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_id INT AUTO_INCREMENT PRIMARY KEY,
                customer_id INT,
                show_id INT,
                seats_booked INT,
                total_amount DECIMAL(10,2),
                booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('CONFIRMED', 'CANCELLED') DEFAULT 'CONFIRMED',
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                FOREIGN KEY (show_id) REFERENCES shows(show_id)
            )
            """
        };
        
        for (String table : tables) {
            try (PreparedStatement stmt = connection.prepareStatement(table)) {
                stmt.executeUpdate();
            }
        }
    }
    
    private void setupUI() {
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Customers", createCustomersPanel());
        tabbedPane.addTab("Theatres", createTheatresPanel());
        tabbedPane.addTab("Shows", createShowsPanel());
        tabbedPane.addTab("Bookings", createBookingsPanel());
        
        add(tabbedPane);
        
        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    
    // Dashboard Panel
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Theatre Management Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        
        JLabel totalCustomers = new JLabel("Total Customers: " + getCount("customers"));
        JLabel totalTheatres = new JLabel("Total Theatres: " + getCount("theatres"));
        JLabel totalShows = new JLabel("Total Shows: " + getCount("shows"));
        JLabel totalBookings = new JLabel("Total Bookings: " + getCount("bookings"));
        
        statsPanel.add(totalCustomers);
        statsPanel.add(totalTheatres);
        statsPanel.add(totalShows);
        statsPanel.add(totalBookings);
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh Statistics");
        refreshBtn.addActionListener(e -> {
            totalCustomers.setText("Total Customers: " + getCount("customers"));
            totalTheatres.setText("Total Theatres: " + getCount("theatres"));
            totalShows.setText("Total Shows: " + getCount("shows"));
            totalBookings.setText("Total Bookings: " + getCount("bookings"));
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private int getCount(String tableName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // Customers Panel
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Customer"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);
        
        JButton addBtn = new JButton("Add Customer");
        JButton updateBtn = new JButton("Update Customer");
        JButton deleteBtn = new JButton("Delete Customer");
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(addBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(updateBtn, gbc);
        gbc.gridx = 2;
        formPanel.add(deleteBtn, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Table
        DefaultTableModel customerModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Phone", "Created At"}, 0
        );
        JTable customerTable = new JTable(customerModel);
        refreshCustomerTable(customerModel);
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button actions
        addBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Name is required!");
                return;
            }
            addCustomer(nameField.getText(), emailField.getText(), phoneField.getText());
            refreshCustomerTable(customerModel);
            clearFields(nameField, emailField, phoneField);
        });
        
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                nameField.setText(customerModel.getValueAt(row, 1).toString());
                emailField.setText(customerModel.getValueAt(row, 2).toString());
                phoneField.setText(customerModel.getValueAt(row, 3).toString());
            }
        });
        
        updateBtn.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a customer to update!");
                return;
            }
            int customerId = (Integer) customerModel.getValueAt(selectedRow, 0);
            updateCustomer(customerId, nameField.getText(), emailField.getText(), phoneField.getText());
            refreshCustomerTable(customerModel);
            clearFields(nameField, emailField, phoneField);
        });
        
        deleteBtn.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a customer to delete!");
                return;
            }
            int customerId = (Integer) customerModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this customer?");
            if (confirm == JOptionPane.YES_OPTION) {
                deleteCustomer(customerId);
                refreshCustomerTable(customerModel);
                clearFields(nameField, emailField, phoneField);
            }
        });
        
        return panel;
    }
    
    // Theatres Panel
    private JPanel createTheatresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Theatre"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField seatsField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        formPanel.add(locationField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Total Seats:"), gbc);
        gbc.gridx = 1;
        formPanel.add(seatsField, gbc);
        
        JButton addBtn = new JButton("Add Theatre");
        JButton updateBtn = new JButton("Update Theatre");
        JButton deleteBtn = new JButton("Delete Theatre");
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(addBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(updateBtn, gbc);
        gbc.gridx = 2;
        formPanel.add(deleteBtn, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Table
        DefaultTableModel theatreModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Location", "Total Seats", "Created At"}, 0
        );
        JTable theatreTable = new JTable(theatreModel);
        refreshTheatreTable(theatreModel);
        
        JScrollPane scrollPane = new JScrollPane(theatreTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button actions
        addBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || seatsField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Name and Total Seats are required!");
                return;
            }
            try {
                int seats = Integer.parseInt(seatsField.getText());
                addTheatre(nameField.getText(), locationField.getText(), seats);
                refreshTheatreTable(theatreModel);
                clearFields(nameField, locationField, seatsField);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number for seats!");
            }
        });
        
        theatreTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && theatreTable.getSelectedRow() != -1) {
                int row = theatreTable.getSelectedRow();
                nameField.setText(theatreModel.getValueAt(row, 1).toString());
                locationField.setText(theatreModel.getValueAt(row, 2).toString());
                seatsField.setText(theatreModel.getValueAt(row, 3).toString());
            }
        });
        
        updateBtn.addActionListener(e -> {
            int selectedRow = theatreTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a theatre to update!");
                return;
            }
            try {
                int theatreId = (Integer) theatreModel.getValueAt(selectedRow, 0);
                int seats = Integer.parseInt(seatsField.getText());
                updateTheatre(theatreId, nameField.getText(), locationField.getText(), seats);
                refreshTheatreTable(theatreModel);
                clearFields(nameField, locationField, seatsField);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number for seats!");
            }
        });
        
        deleteBtn.addActionListener(e -> {
            int selectedRow = theatreTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a theatre to delete!");
                return;
            }
            int theatreId = (Integer) theatreModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this theatre?");
            if (confirm == JOptionPane.YES_OPTION) {
                deleteTheatre(theatreId);
                refreshTheatreTable(theatreModel);
                clearFields(nameField, locationField, seatsField);
            }
        });
        
        return panel;
    }
    
    // Shows Panel
    private JPanel createShowsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Show"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField titleField = new JTextField(20);
        JTextArea descriptionArea = new JTextArea(3, 20);
        JTextField genreField = new JTextField(20);
        JTextField durationField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JComboBox<String> theatreCombo = new JComboBox<>();
        JTextField dateField = new JTextField(20);
        JTextField timeField = new JTextField(20);
        
        // Populate theatre combo
        refreshTheatreCombo(theatreCombo);
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        formPanel.add(genreField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Duration (min):"), gbc);
        gbc.gridx = 1;
        formPanel.add(durationField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Ticket Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Theatre:"), gbc);
        gbc.gridx = 1;
        formPanel.add(theatreCombo, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        formPanel.add(timeField, gbc);
        
        JButton addBtn = new JButton("Add Show");
        JButton updateBtn = new JButton("Update Show");
        JButton deleteBtn = new JButton("Delete Show");
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(addBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(updateBtn, gbc);
        gbc.gridx = 2;
        formPanel.add(deleteBtn, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Table
        DefaultTableModel showModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Genre", "Duration", "Price", "Theatre", "Date", "Time", "Available Seats"}, 0
        );
        JTable showTable = new JTable(showModel);
        refreshShowTable(showModel);
        
        JScrollPane scrollPane = new JScrollPane(showTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button actions
        addBtn.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty() || theatreCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(panel, "Title and Theatre are required!");
                return;
            }
            try {
                String theatreInfo = (String) theatreCombo.getSelectedItem();
                int theatreId = Integer.parseInt(theatreInfo.split(" - ")[0]);
                
                int duration = durationField.getText().isEmpty() ? 0 : Integer.parseInt(durationField.getText());
                double price = priceField.getText().isEmpty() ? 0.0 : Double.parseDouble(priceField.getText());
                
                addShow(titleField.getText(), descriptionArea.getText(), genreField.getText(),
                       duration, price, theatreId, dateField.getText(), timeField.getText());
                refreshShowTable(showModel);
                clearShowFields(titleField, descriptionArea, genreField, durationField, 
                              priceField, dateField, timeField);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter valid numbers for duration and price!");
            }
        });
        
        return panel;
    }
    
    // Bookings Panel
    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Booking"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JComboBox<String> customerCombo = new JComboBox<>();
        JComboBox<String> showCombo = new JComboBox<>();
        JTextField seatsField = new JTextField(10);
        
        refreshCustomerCombo(customerCombo);
        refreshShowCombo(showCombo);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        formPanel.add(customerCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Show:"), gbc);
        gbc.gridx = 1;
        formPanel.add(showCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Seats:"), gbc);
        gbc.gridx = 1;
        formPanel.add(seatsField, gbc);
        
        JButton bookBtn = new JButton("Book Tickets");
        JButton cancelBtn = new JButton("Cancel Booking");
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(bookBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(cancelBtn, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Table
        DefaultTableModel bookingModel = new DefaultTableModel(
            new String[]{"ID", "Customer", "Show", "Seats", "Amount", "Date", "Status"}, 0
        );
        JTable bookingTable = new JTable(bookingModel);
        refreshBookingTable(bookingModel);
        
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button actions
        bookBtn.addActionListener(e -> {
            if (customerCombo.getSelectedItem() == null || showCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(panel, "Please select customer and show!");
                return;
            }
            try {
                String customerInfo = (String) customerCombo.getSelectedItem();
                String showInfo = (String) showCombo.getSelectedItem();
                int customerId = Integer.parseInt(customerInfo.split(" - ")[0]);
                int showId = Integer.parseInt(showInfo.split(" - ")[0]);
                int seats = Integer.parseInt(seatsField.getText());
                
                createBooking(customerId, showId, seats);
                refreshBookingTable(bookingModel);
                seatsField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number of seats!");
            }
        });
        
        cancelBtn.addActionListener(e -> {
            int selectedRow = bookingTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a booking to cancel!");
                return;
            }
            int bookingId = (Integer) bookingModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to cancel this booking?");
            if (confirm == JOptionPane.YES_OPTION) {
                cancelBooking(bookingId);
                refreshBookingTable(bookingModel);
            }
        });
        
        return panel;
    }
    
    // Database operations for Customers
    private void addCustomer(String name, String email, String phone) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, email.isEmpty() ? null : email);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding customer: " + e.getMessage());
        }
    }
    
    private void updateCustomer(int id, String name, String email, String phone) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE customers SET name=?, email=?, phone=? WHERE customer_id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, email.isEmpty() ? null : email);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setInt(4, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating customer: " + e.getMessage());
        }
    }
    
    private void deleteCustomer(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM customers WHERE customer_id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage());
        }
    }
    
    private void refreshCustomerTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM customers ORDER BY customer_id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Database operations for Theatres
    private void addTheatre(String name, String location, int seats) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO theatres (name, location, total_seats) VALUES (?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, location);
            stmt.setInt(3, seats);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Theatre added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding theatre: " + e.getMessage());
        }
    }
    
    private void updateTheatre(int id, String name, String location, int seats) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE theatres SET name=?, location=?, total_seats=? WHERE theatre_id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, location);
            stmt.setInt(3, seats);
            stmt.setInt(4, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Theatre updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating theatre: " + e.getMessage());
        }
    }
    
    private void deleteTheatre(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM theatres WHERE theatre_id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Theatre deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting theatre: " + e.getMessage());
        }
    }
    
    private void refreshTheatreTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM theatres ORDER BY theatre_id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("theatre_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getInt("total_seats"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Database operations for Shows
    private void addShow(String title, String description, String genre, int duration, 
                        double price, int theatreId, String date, String time) {
        try (PreparedStatement stmt = connection.prepareStatement(
                """
                INSERT INTO shows (title, description, genre, duration_minutes, ticket_price, 
                theatre_id, show_date, show_time, available_seats) 
                SELECT ?, ?, ?, ?, ?, ?, ?, ?, total_seats FROM theatres WHERE theatre_id=?
                """)) {
            stmt.setString(1, title);
            stmt.setString(2, description.isEmpty() ? null : description);
            stmt.setString(3, genre.isEmpty() ? null : genre);
            stmt.setInt(4, duration);
            stmt.setDouble(5, price);
            stmt.setInt(6, theatreId);
            stmt.setString(7, date.isEmpty() ? null : date);
            stmt.setString(8, time.isEmpty() ? null : time);
            stmt.setInt(9, theatreId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Show added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding show: " + e.getMessage());
        }
    }
    
    private void refreshShowTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (PreparedStatement stmt = connection.prepareStatement(
                """
                SELECT s.show_id, s.title, s.genre, s.duration_minutes, s.ticket_price, 
                       t.name as theatre_name, s.show_date, s.show_time, s.available_seats
                FROM shows s 
                JOIN theatres t ON s.theatre_id = t.theatre_id 
                ORDER BY s.show_id
                """);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("show_id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("duration_minutes"),
                    rs.getDouble("ticket_price"),
                    rs.getString("theatre_name"),
                    rs.getDate("show_date"),
                    rs.getTime("show_time"),
                    rs.getInt("available_seats")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Database operations for Bookings
    private void createBooking(int customerId, int showId, int seats) {
        try {
            connection.setAutoCommit(false);
            
            // Check available seats
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT available_seats, ticket_price FROM shows WHERE show_id=?");
            checkStmt.setInt(1, showId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int availableSeats = rs.getInt("available_seats");
                double ticketPrice = rs.getDouble("ticket_price");
                
                if (availableSeats < seats) {
                    JOptionPane.showMessageDialog(this, "Not enough seats available!");
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return;
                }
                
                // Create booking
                PreparedStatement bookStmt = connection.prepareStatement(
                    "INSERT INTO bookings (customer_id, show_id, seats_booked, total_amount) VALUES (?, ?, ?, ?)");
                bookStmt.setInt(1, customerId);
                bookStmt.setInt(2, showId);
                bookStmt.setInt(3, seats);
                bookStmt.setDouble(4, seats * ticketPrice);
                bookStmt.executeUpdate();
                
                // Update available seats
                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE shows SET available_seats = available_seats - ? WHERE show_id=?");
                updateStmt.setInt(1, seats);
                updateStmt.setInt(2, showId);
                updateStmt.executeUpdate();
                
                connection.commit();
                connection.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "Booking created successfully!");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error creating booking: " + e.getMessage());
        }
    }
    
    private void cancelBooking(int bookingId) {
        try {
            connection.setAutoCommit(false);
            
            // Get booking details
            PreparedStatement getBookingStmt = connection.prepareStatement(
                "SELECT show_id, seats_booked FROM bookings WHERE booking_id=? AND status='CONFIRMED'");
            getBookingStmt.setInt(1, bookingId);
            ResultSet rs = getBookingStmt.executeQuery();
            
            if (rs.next()) {
                int showId = rs.getInt("show_id");
                int seatsBooked = rs.getInt("seats_booked");
            
                // Update booking status
                PreparedStatement cancelStmt = connection.prepareStatement(
                    "UPDATE bookings SET status='CANCELLED' WHERE booking_id=?");
                cancelStmt.setInt(1, bookingId);
                cancelStmt.executeUpdate();
                
                // Restore available seats
                PreparedStatement restoreStmt = connection.prepareStatement(
                    "UPDATE shows SET available_seats = available_seats + ? WHERE show_id=?");
                restoreStmt.setInt(1, seatsBooked);
                restoreStmt.setInt(2, showId);
                restoreStmt.executeUpdate();
                
                connection.commit();
                connection.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");
            } else {
                connection.rollback();
                connection.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "Booking not found or already cancelled!");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error cancelling booking: " + e.getMessage());
        }
    }
    
    private void refreshBookingTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (PreparedStatement stmt = connection.prepareStatement(
                """
                SELECT b.booking_id, c.name as customer_name, s.title as show_title,
                       b.seats_booked, b.total_amount, b.booking_date, b.status
                FROM bookings b 
                JOIN customers c ON b.customer_id = c.customer_id
                JOIN shows s ON b.show_id = s.show_id
                ORDER BY b.booking_id DESC
                """);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("customer_name"),
                    rs.getString("show_title"),
                    rs.getInt("seats_booked"),
                    rs.getDouble("total_amount"),
                    rs.getTimestamp("booking_date"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Combo box refresh methods
    private void refreshTheatreCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT theatre_id, name FROM theatres ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(rs.getInt("theatre_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void refreshCustomerCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT customer_id, name FROM customers ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(rs.getInt("customer_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void refreshShowCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT show_id, title FROM shows WHERE available_seats > 0 ORDER BY title");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(rs.getInt("show_id") + " - " + rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Utility methods
    private void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }
    
    private void clearShowFields(JTextField titleField, JTextArea descriptionArea, 
                               JTextField genreField, JTextField durationField, 
                               JTextField priceField, JTextField dateField, JTextField timeField) {
        titleField.setText("");
        descriptionArea.setText("");
        genreField.setText("");
        durationField.setText("");
        priceField.setText("");
        dateField.setText("");
        timeField.setText("");
    }
    
    public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new TheatreManagementSystem();
        }
    });
}
}

// Database Setup Instructions
/*
To set up the MySQL database:

1. Install MySQL server and create a database named 'theatre_management'
2. Update the database connection details in the code:
   - DB_URL: Change localhost and port if needed
   - DB_USER: Your MySQL username
   - DB_PASSWORD: Your MySQL password

3. Add MySQL Connector/J to your classpath:
   Download from: https://dev.mysql.com/downloads/connector/j/
   
4. SQL Commands to create database (if needed):
   CREATE DATABASE theatre_management;
   USE theatre_management;

The application will automatically create all required tables on first run.

Features included:
- Dashboard with statistics
- Customer management (CRUD operations)
- Theatre management (CRUD operations)
- Show management with theatre association
- Booking system with seat availability tracking
- Data integrity with foreign key relationships
- Transaction handling for booking operations
- User-friendly Swing GUI with tabbed interface

Usage:
1. Start by adding theatres
2. Add customers
3. Create shows for theatres
4. Make bookings for customers
5. View statistics on dashboard
*/