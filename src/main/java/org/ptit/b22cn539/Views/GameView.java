package org.ptit.b22cn539.Views;

import com.formdev.flatlaf.FlatLightLaf;
import io.socket.client.Socket;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameView extends JFrame {
    private final Socket socket;
    private JLabel lblTimer;
    private JLabel lblScore;
    private JLabel lblQuestion; // Thêm label hiển thị số câu
    private final JButton[] answerButtons = new JButton[4];
    private Timer timer;
    private int timeLeft = 10;
    private int score = 0;
    private long gameItemId;


    private final JSONArray musicIds;
    private final JSONArray gameItemIds;
    private final long gameId;
    private int currentQuestionIndex = 0;

    public GameView(Socket socket, JSONArray musicIds, JSONArray gameItemIds, long gameId) {
        this.socket = socket;
        this.musicIds = musicIds;
        this.gameItemIds = gameItemIds;
        this.gameId = gameId;

        FlatLightLaf.setup();
        initUI();
        registerSocketEvents();
    }

    private void initUI() {
        setTitle("Thi Đấu - Game Đoán Âm Thanh");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top panel với timer, score và câu hỏi hiện tại
        JPanel topPanel = new JPanel(new BorderLayout());

        lblTimer = new JLabel("⏳ 10s", SwingConstants.LEFT);
        lblScore = new JLabel("Điểm: 0", SwingConstants.RIGHT);
        lblQuestion = new JLabel("Câu 1/" + musicIds.length(), SwingConstants.CENTER);

        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 16));

        topPanel.add(lblTimer, BorderLayout.WEST);
        topPanel.add(lblQuestion, BorderLayout.CENTER);
        topPanel.add(lblScore, BorderLayout.EAST);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(topPanel, BorderLayout.NORTH);

        JButton btnPlaySound = new JButton("🔊 Nghe âm thanh");
        btnPlaySound.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnPlaySound.setBackground(new Color(0x0078D7));
        btnPlaySound.setForeground(Color.WHITE);
        btnPlaySound.setFocusPainted(false);
        btnPlaySound.setBorder(new EmptyBorder(15, 20, 15, 20));
        btnPlaySound.addActionListener(e -> playSoundFromServer());
        add(btnPlaySound, BorderLayout.CENTER);

        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        answersPanel.setBorder(new EmptyBorder(20, 30, 30, 30));
        add(answersPanel, BorderLayout.SOUTH);

        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton("Đáp án " + (i + 1));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btn.setBackground(new Color(240, 240, 240));
            btn.setFocusPainted(false);
            final int index = i;
            btn.addActionListener(e -> sendAnswer(index));
            answerButtons[i] = btn;
            answersPanel.add(btn);
        }
    }

    private void updateQuestionUI(JSONObject data) {
        try {
            this.gameItemId = data.getLong("id");
            JSONArray answers = data.getJSONArray("answers");

            // Update câu hỏi hiện tại
            lblQuestion.setText("Câu " + (currentQuestionIndex + 1) + "/" + musicIds.length());

            for (int i = 0; i < answers.length(); i++) {
                JSONObject ans = answers.getJSONObject(i);
                answerButtons[i].setText(ans.getString("description"));
                answerButtons[i].setEnabled(true);
                answerButtons[i].setBackground(new Color(240, 240, 240));
            }

            // Reset timer
            if (timer != null) timer.stop();
            timeLeft = 10;
            lblTimer.setText("⏳ " + timeLeft + "s");
            timer = new Timer(1000, e -> {
                try {
                    timeLeft--;
                    lblTimer.setText("⏳ " + timeLeft + "s");
                    if (timeLeft <= 0) {
                        timer.stop();
                        disableButtons();
                        // Tự động submit với answerId = -1 (không chọn)
                        socket.emit("topic/handleAnswer", new JSONObject()
                                .put("gameItemId", gameItemIds.getLong(currentQuestionIndex))
                                .put("answerId", -1)
                                .toString());
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            });
            timer.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswer(int index) {
        try {
            if (timer != null) timer.stop();
            disableButtons();

            // Lấy answerId thực từ data
            JSONObject answerData = new JSONObject(answerButtons[index].getText());
            long answerId = answerData.optLong("id", index + 1);

            JSONObject payload = new JSONObject();
            payload.put("gameItemId", gameItemIds.getLong(currentQuestionIndex));
            payload.put("answerId", answerId);

            socket.emit("topic/handleAnswer", payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void registerSocketEvents() {
        socket.on("topic/changeQuestion", args -> {
            JSONObject data = (JSONObject) args[0];
            SwingUtilities.invokeLater(() -> updateQuestionUI(data));
        });

        socket.on("topic/answerResult", args -> {
            try {
                JSONObject res = new JSONObject(args[0].toString());
                int point = res.optInt("score", 0);
                SwingUtilities.invokeLater(() -> {
                    updateScore(point);

                    // Chuyển câu hỏi tiếp theo sau 1.5 giây
                    Timer delayTimer = new Timer(1500, e -> {
                        currentQuestionIndex++;

                        if (currentQuestionIndex < musicIds.length()) {
                            // Còn câu hỏi -> emit changeQuestion
                            try {
                                JSONObject changeData = new JSONObject();
                                changeData.put("musicId", musicIds.getLong(currentQuestionIndex));
                                socket.emit("topic/changeQuestion", changeData.toString());
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            // Hết câu hỏi -> emit finishGame
                            try {
                                JSONObject finishData = new JSONObject();
                                finishData.put("gameId", gameId);
                                socket.emit("topic/finishGame", finishData.toString());
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    delayTimer.setRepeats(false);
                    delayTimer.start();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("topic/finishGame", args -> {
            try {
                JSONObject res = new JSONObject(args[0].toString());
                int totalScore = res.optInt("totalScore", 0);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Trận đấu kết thúc!\nTổng điểm của bạn: " + totalScore,
                            "Kết quả", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateScore(int point) {
        score += point;
        lblScore.setText("Điểm: " + score);
    }

    private void disableButtons() {
        for (JButton btn : answerButtons) {
            btn.setEnabled(false);
        }
    }

    private void playSoundFromServer() {
        try {
            File file = new File("sound.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể phát âm thanh: " + e.getMessage());
        }
    }
}