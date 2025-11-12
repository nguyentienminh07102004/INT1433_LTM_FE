package org.ptit.b22cn539.Views;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.json.JSONObject;
import org.ptit.b22cn539.Handler.ClientHandler;

public class LoginView extends JFrame {

    public LoginView() {
        FlatLightLaf.setup();

        this.setTitle("Game Đoán Âm Thanh - Đăng Nhập");
        this.setSize(450, 350);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        JLabel title = new JLabel("🎧 Đăng Nhập Hệ Thống", SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font: 200% $semibold.font; foreground: #0078D7;");
        this.add(title, BorderLayout.NORTH);
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        JTextField txtUser = new JTextField();
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên đăng nhập");
        JPasswordField txtPass = new JPasswordField();
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");
        panel.add(txtUser);
        panel.add(txtPass);
        this.add(panel, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnRegister = new JButton("Đăng ký");
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 30, 50));
        this.add(btnPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener((event) -> {
            try {
                String username = txtUser.getText();
                String password = new String(txtPass.getPassword());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                        .uri(URI.create("http://localhost:8080/users/login"))
                        .header("Content-Type", "application/json")
                        .build();
                HttpClient httpClient = HttpClient.newHttpClient();
                var response = httpClient.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
                String token = response.body();
                String pathTokenFile = System.getProperty("user.dir") + "/token.txt";
                Path filePath = Paths.get(pathTokenFile);
                if (Files.notExists(filePath)) {
                    Files.createFile(filePath);
                }
                Files.writeString(filePath, token, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                this.dispose();
                ClientHandler clientHandler = new ClientHandler(token);
                new HomeView(clientHandler.getSocket()).setVisible(true);
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });

        btnRegister.addActionListener((event) -> {
            this.dispose();
            new RegisterForm().setVisible(true);
        });
    }
}
