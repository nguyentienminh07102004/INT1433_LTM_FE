package org.ptit.b22cn539.Views;

import com.formdev.flatlaf.FlatLightLaf;
import io.socket.client.Socket;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptit.b22cn539.Handler.ClientHandler;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeView extends JFrame {
    final Socket socket;

    public HomeView(String token) throws IOException {
        FlatLightLaf.setup();
        setTitle("Sảnh Chờ - Game Đoán Âm Thanh");
        this.setSize(700, 450);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(10, 10));
        JLabel title = new JLabel("Danh sách người chơi trực tuyến", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0x0078D7));
        this.add(title, BorderLayout.NORTH);
        String[] header = {"ID", "Tên người chơi", "Họ tên", "Trạng thái"};
        ClientHandler clientHandler = new ClientHandler(token);
        this.socket = clientHandler.getSocket();
        Object[][] data = {};
        JTable table = new JTable(new DefaultTableModel(data, header));
        this.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnInvite = new JButton("Mời chơi");
        JButton btnRanking = new JButton("Xếp hạng");
        JButton btnLogout = new JButton("Đăng xuất");
        bottom.add(btnInvite);
        bottom.add(btnRanking);
        bottom.add(btnLogout);
        this.add(bottom, BorderLayout.SOUTH);
        btnInvite.addActionListener((e) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng chọn một người chơi để mời!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            String invitedUsername = (String) table.getValueAt(selectedRow, 1);
            this.socket.emit("topic/inviteUser", invitedUsername);
            JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi lời mời chơi đến " + invitedUsername,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        this.socket.on("topic/getAllUsersResponse", objects -> {
            try {
                JSONArray jsonArray = (JSONArray) objects[0];
                DefaultTableModel defaultTableModel = (DefaultTableModel) table.getModel();
                for (int i = 0; i < jsonArray.length(); i++) {
                    var jsonObject = jsonArray.getJSONObject(i);
                    long id = jsonObject.getLong("id");
                    String fullName = jsonObject.getString("fullName");
                    String username = jsonObject.getString("username");
                    String status = jsonObject.getString("status");
                    defaultTableModel.addRow(new Object[]{id, username, fullName, status});
                }
            } catch (JSONException e) {
                System.out.println("Error parsing JSON: " + e.getMessage());
            }
        });
        this.socket.on("topic/initGame", (objects) -> {
            try {
                JSONObject jsonObject = (JSONObject) objects[0];

                // Lấy data từ server
                JSONArray musicIds = jsonObject.getJSONArray("musicIds");
                JSONArray gameItemIds = jsonObject.getJSONArray("gameItemIds");
                long gameId = jsonObject.getLong("gameId");

                System.out.println("Game initialized with " + musicIds.length() + " questions");
                System.out.println("Game ID: " + gameId);

                SwingUtilities.invokeLater(() -> {
                    // Đóng màn hình hiện tại
                    this.dispose();

                    // Tạo GameView với đầy đủ thông tin
                    GameView gameView = new GameView(
                            this.socket,
                            musicIds,
                            gameItemIds,
                            gameId
                    );
                    gameView.setVisible(true);

                    // Emit để lấy câu hỏi đầu tiên (câu 0)
                    try {
                        JSONObject changeQuestionData = new JSONObject();
                        changeQuestionData.put("musicId", musicIds.get(0));
                        this.socket.emit("topic/changeQuestion", changeQuestionData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Lỗi khi tải câu hỏi: " + e.getMessage());
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Lỗi khởi tạo game: " + e.getMessage());
                });
            }
        });
        this.socket.on("topic/inviteUser", objects -> {
            try {
                JSONObject jsonObject = (JSONObject) objects[0];
                String fromUser = jsonObject.getString("from");
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "Người chơi " + fromUser + " đang mời bạn chơi!",
                        "Lời mời chơi",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    this.socket.emit("topic/acceptInvite", fromUser);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
