package org.ptit.b22cn539.Views;

import com.formdev.flatlaf.FlatLightLaf;
import io.socket.client.Socket;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeView extends JFrame {
    final Socket socket;
    final DefaultTableModel tableModel;
    final JTable table;

    public HomeView(Socket socket) throws IOException {
        FlatLightLaf.setup();
        setTitle("Sảnh Chờ - Game Đoán Âm Thanh");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Tiêu đề
        JLabel title = new JLabel("Danh sách người chơi trực tuyến", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0x0078D7));
        add(title, BorderLayout.NORTH);

        // Bảng người chơi
        String[] header = {"ID", "Tên người chơi", "Họ tên", "Trạng thái"};
        tableModel = new DefaultTableModel(header, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        this.socket = socket;

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnInvite = new JButton("Mời chơi");
        JButton btnRanking = new JButton("Xếp hạng");
        JButton btnLogout = new JButton("Đăng xuất");
        bottom.add(btnInvite);
        bottom.add(btnRanking);
        bottom.add(btnLogout);
        add(bottom, BorderLayout.SOUTH);

        btnInvite.addActionListener((e) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một người chơi để mời!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String invitedUsername = (String) table.getValueAt(selectedRow, 1);
            System.out.println("[HomeView] Emitting topic/inviteUser -> " + invitedUsername);
            socket.emit("topic/inviteUser", invitedUsername);
            JOptionPane.showMessageDialog(this,
                    "Đã gửi lời mời chơi đến " + invitedUsername,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRanking.addActionListener(e -> {
            RankingView topRankingView = new RankingView(socket);
            topRankingView.setVisible(true);
        });
        socket.on("topic/getAllUsersResponse", args -> {
            System.out.println("[HomeView] Received topic/getAllUsersResponse -> " + (args != null && args.length > 0 ? args[0] : "null"));
            Object obj = args[0];
            if (obj instanceof JSONArray) {
                JSONArray users = (JSONArray) obj;
                SwingUtilities.invokeLater(() -> {
                    try {
                        tableModel.setRowCount(0);
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject u = users.getJSONObject(i);
                            tableModel.addRow(new Object[]{
                                    u.optLong("id", -1L),
                                    u.optString("username", ""),
                                    u.optString("fullName", ""),
                                    u.optString("status", "")
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } else if (obj instanceof JSONObject) {
                JSONObject wrapper = (JSONObject) obj;
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    try {
                        if (wrapper.has("users")) {
                            JSONArray users = wrapper.getJSONArray("users");
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject u = users.getJSONObject(i);
                                tableModel.addRow(new Object[]{
                                        u.optLong("id", -1L),
                                        u.optString("username", ""),
                                        u.optString("fullName", ""),
                                        u.optString("status", "")
                                });
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                System.out.println("[HomeView] Unexpected data type for getAllUsersResponse: " + (obj != null ? obj.getClass() : "null"));
            }
        });

        socket.on("topic/changeStatus", args -> {
            try {
                JSONObject json = (JSONObject) args[0];
                String username = json.getString("username");
                String status = json.getString("status");
                String id = json.getString("id");
                String fullName = json.getString("fullName");
                System.out.println(username);
                SwingUtilities.invokeLater(() -> {
                    boolean isExists = true;
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        System.out.println(tableModel.getValueAt(i, 1));
                        if (String.valueOf(tableModel.getValueAt(i, 1)).trim().equalsIgnoreCase(username.trim())) {
                            tableModel.setValueAt(status, i, 3);
                            isExists = false;
                            break;
                        }
                    }
                    if (isExists) {
                        tableModel.addRow(new Object[]{id, username, fullName, status});
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        socket.on("topic/inviteUser", args -> {
            try {
                JSONObject json = (JSONObject) args[0];
                String fromUser = json.getString("from");

                SwingUtilities.invokeLater(() -> {
                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            "Người chơi " + fromUser + " đang mời bạn chơi!\nBạn có chấp nhận không?",
                            "Lời mời chơi",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        System.out.println("[HomeView] Accepting invite from " + fromUser);
                        socket.emit("topic/acceptInvite", fromUser);
                    }
                });
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        });

        socket.on("topic/initGame", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONArray musicIds = data.getJSONArray("musicIds");
                long gameId = data.getLong("gameId");

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    GameView gameView = new GameView(socket, gameId, musicIds);
                    gameView.setVisible(true);
                });
            } catch (JSONException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this, "Lỗi khởi tạo game: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        });

        btnLogout.addActionListener(e -> {
            socket.disconnect();
            dispose();
            new LoginView().setVisible(true);
        });
        socket.on("error", o -> {
            socket.emit("topic/getAllUsersResponse");
        });
        socket.emit("topic/getAllUsersResponse");
    }
}