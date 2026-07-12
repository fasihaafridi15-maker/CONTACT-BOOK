import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ContactBook extends JFrame {

    private List<Contact> contacts = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;

    public ContactBook() {
        super("Contact Book - Dark Theme");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== DARK THEME COLORS =====
        Color bg = new Color(30, 30, 30);
        Color panelBg = new Color(45, 45, 45);
        Color tableBg = new Color(35, 35, 35);
        Color rowAlt = new Color(50, 50, 50);
        Color accent = new Color(70, 130, 180);
        Color textColor = Color.WHITE;

        getContentPane().setBackground(bg);

        // ===== TABLE SETUP =====
        String[] columns = {"Name", "Phone / Roll No", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        table.setBackground(tableBg);
        table.setForeground(textColor);
        table.setGridColor(new Color(70, 70, 70));
        table.setSelectionBackground(accent);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Alternate row colors
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? tableBg : rowAlt);
                    c.setForeground(textColor);
                }
                return c;
            }
        };

        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(tableBg);
        add(scroll, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(panelBg);

        JButton addButton = styledButton("Add", accent);
        JButton updateButton = styledButton("Update", accent);
        JButton deleteButton = styledButton("Delete", accent);
        JButton searchButton = styledButton("Search", accent);
        JButton sortButton = styledButton("Sort by Name", accent);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(sortButton);

        add(buttonPanel, BorderLayout.NORTH);

        // ===== BUTTON ACTIONS =====
        addButton.addActionListener(e -> addContact());
        updateButton.addActionListener(e -> updateContact());
        deleteButton.addActionListener(e -> deleteContact());
        searchButton.addActionListener(e -> searchContact());
        sortButton.addActionListener(e -> sortContacts());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===== STYLED BUTTON =====
    private JButton styledButton(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setBackground(accent);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    // ===== DUPLICATE CHECK =====
    private boolean isDuplicate(String name, String phone, int ignoreIndex) {
        for (int i = 0; i < contacts.size(); i++) {
            if (i == ignoreIndex) continue;

            Contact c = contacts.get(i);

            if (c.getName().equalsIgnoreCase(name) ||
                    (!phone.isEmpty() && c.getPhone().equalsIgnoreCase(phone))) {
                return true;
            }
        }
        return false;
    }

    // ===== REFRESH TABLE =====
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Contact c : contacts) {
            tableModel.addRow(new Object[]{
                    c.getName(), c.getPhone(), c.getEmail()
            });
        }
    }

    // ===== ADD CONTACT =====
    private void addContact() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] msg = {
                "Name:", nameField,
                "Phone / Roll No:", phoneField,
                "Email:", emailField
        };

        if (JOptionPane.showConfirmDialog(this, msg, "Add Contact",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                return;
            }

            if (isDuplicate(name, phone, -1)) {
                JOptionPane.showMessageDialog(this,
                        "Duplicate contact!\nSame name or phone exists.");
                return;
            }

            contacts.add(new Contact(name, phone, email));
            sortContacts();
        }
    }

    // ===== UPDATE CONTACT =====
    private void updateContact() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a contact first!");
            return;
        }

        Contact c = contacts.get(row);

        JTextField nameField = new JTextField(c.getName());
        JTextField phoneField = new JTextField(c.getPhone());
        JTextField emailField = new JTextField(c.getEmail());

        Object[] msg = {
                "Name:", nameField,
                "Phone / Roll No:", phoneField,
                "Email:", emailField
        };

        if (JOptionPane.showConfirmDialog(this, msg, "Update Contact",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                return;
            }

            if (isDuplicate(newName, newPhone, row)) {
                JOptionPane.showMessageDialog(this,
                        "Duplicate contact!\nSame name or phone exists.");
                return;
            }

            c.setName(newName);
            c.setPhone(newPhone);
            c.setEmail(emailField.getText().trim());

            sortContacts();
        }
    }

    // ===== DELETE CONTACT =====
    private void deleteContact() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            contacts.remove(row);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Select a contact to delete!");
        }
    }

    // ===== SEARCH CONTACT (PARTIAL + CASE INSENSITIVE) =====
    private void searchContact() {
        String query = JOptionPane.showInputDialog(this,
                "Search by name, phone, or email:");

        if (query == null || query.trim().isEmpty()) return;

        query = query.toLowerCase();
        table.clearSelection();
        boolean found = false;

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);

            if (c.getName().toLowerCase().contains(query) ||
                    c.getPhone().toLowerCase().contains(query) ||
                    c.getEmail().toLowerCase().contains(query)) {

                table.addRowSelectionInterval(i, i);
                found = true;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "No matching contact found!");
        }
    }

    // ===== SORT CONTACTS =====
    private void sortContacts() {
        contacts.sort(Comparator.comparing(
                Contact::getName, String.CASE_INSENSITIVE_ORDER));
        refreshTable();
    }

    // ===== CONTACT CLASS =====
    private static class Contact {
        private String name, phone, email;

        Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }

        String getName() { return name; }
        String getPhone() { return phone; }
        String getEmail() { return email; }

        void setName(String name) { this.name = name; }
        void setPhone(String phone) { this.phone = phone; }
        void setEmail(String email) { this.email = email; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContactBook::new);
    }
}