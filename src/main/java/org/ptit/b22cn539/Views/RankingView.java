package org.ptit.b22cn539.Views;

import io.socket.client.Socket;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

public class RankingView extends JFrame {

    private final Socket socket;
    private JTable rankingTable;

    public RankingView(Socket socket) {
        this.socket = socket;

        setTitle("🏆 Bảng xếp hạng");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Tiêu đề
        JLabel title = new JLabel("Bảng xếp hạng người chơi", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Tạo JTable
        String[] headers = {"Hạng", "Tên người chơi", "Điểm"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0);
        rankingTable = new JTable(tableModel);
        rankingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rankingTable.setRowHeight(25);
        add(new JScrollPane(rankingTable), BorderLayout.CENTER);

        // Đăng ký listener trước khi emit
        socket.on("topic/getTopRanking", args -> {
            Object obj = args[0];
            System.out.println("Received: " + obj + " | class: " + obj.getClass());
            try {
                if (obj instanceof JSONArray json) {
                    List<JSONArray> list = new ArrayList<>();
                    for (int i = 0; i < json.length(); i++) {
                        JSONArray jsonArray = json.getJSONArray(i);
                        list.add(jsonArray);
                    }
                    list = list.stream().sorted((o1, o2) -> o2.optInt(1) - o1.optInt(1)).toList();
                    Map<String, Long> ranking = new LinkedHashMap<>();
                    for (JSONArray jsonObject : list) {
                        String username = jsonObject.getString(0);
                        long score = jsonObject.optInt(1);
                        ranking.put(username, score);
                    }
                    System.out.println(ranking);
                    SwingUtilities.invokeLater(() -> updateTable(ranking));
                }
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
        socket.emit("topic/getTopRanking");
        setVisible(true);
    }

    /**
     * Cập nhật dữ liệu bảng xếp hạng
     */
    private void updateTable(Map<String, Long> ranking) {
        DefaultTableModel model = (DefaultTableModel) rankingTable.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ
        int rank = 1;
        for (Map.Entry<String, Long> entry : ranking.entrySet()) {
            model.addRow(new Object[]{rank++, entry.getKey(), entry.getValue()});
        }
    }
}