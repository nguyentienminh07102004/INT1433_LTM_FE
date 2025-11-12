package org.ptit.b22cn539.Views;

import io.socket.client.Socket;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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

            if (obj instanceof JSONObject json) {
                Map<String, Long> ranking = new LinkedHashMap<>();
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    ranking.put(k, json.optLong(k));
                }
                // Cập nhật JTable trên EDT
                SwingUtilities.invokeLater(() -> updateTable(ranking));
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