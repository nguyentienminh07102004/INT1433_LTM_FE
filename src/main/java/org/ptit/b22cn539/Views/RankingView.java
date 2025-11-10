package org.ptit.b22cn539.Views;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RankingView extends JFrame {

    public RankingView() {
        FlatLightLaf.setup();
        setTitle("🏆 Bảng Xếp Hạng");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Bảng Xếp Hạng Toàn Hệ Thống", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0x0078D7));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Thứ hạng", "Tên người chơi", "Điểm tích lũy", "Số trận thắng"};
        Object[][] data = {
                {"1", "Thảo", "210", "15"},
                {"2", "Minh", "180", "13"},
                {"3", "An", "170", "12"},
                {"4", "Hà", "150", "10"},
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnBack = new JButton("⬅ Quay lại sảnh chờ");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnBack.setBackground(new Color(0x0078D7));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel bottom = new JPanel();
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);
    }
}