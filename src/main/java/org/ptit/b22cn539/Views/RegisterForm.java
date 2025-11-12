package org.ptit.b22cn539.Views;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.json.JSONObject;

public class RegisterForm extends JFrame implements ActionListener {

    private final JTextField txtFullName;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JButton btnRegister;
    private final JButton btnClear;

    public RegisterForm() {
        setTitle("User Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // giữa màn hình

        // Panel chính
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        txtFullName = new JTextField(20);
        panel.add(txtFullName, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(70, 130, 180));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(this);
        panel.add(btnRegister, gbc);

        gbc.gridx = 1;
        btnClear = new JButton("Clear");
        btnClear.addActionListener(this);
        panel.add(btnClear, gbc);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) {
            try {
                String fullName = txtFullName.getText().trim();
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Sau này có thể thêm logic lưu vào CSDL ở đây
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                    jsonObject.put("fullName", fullName);
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                            .uri(URI.create("http://localhost:8080/users/register"))
                            .header("Content-Type", "application/json")
                            .build();
                    HttpClient httpClient = HttpClient.newHttpClient();
                    var response = httpClient.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Failed to register user");
                    }
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    this.dispose();
                    new LoginView().setVisible(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnClear) {
            txtFullName.setText("");
            txtUsername.setText("");
            txtPassword.setText("");
        }
    }
}