package org.ptit.b22cn539.Views;

import io.socket.client.Socket;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ptit.b22cn539.DTO.MusicResponse;
import org.ptit.b22cn539.DTO.AnswerResponse;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * GameView: màn hình chơi game đoán nhạc realtime
 */
public class GameView extends JFrame {

    private final Socket socket;
    private final Long gameId;
    private final JSONArray musicIds;

    private int currentQuestionIndex = 0;
    private int score = 0;

    private final JButton[] answerButtons = new JButton[4];
    private final JLabel lblQuestion = new JLabel();
    private final JLabel lblTimer = new JLabel();
    private final JLabel lblScore = new JLabel();
    private final JButton btnPlay = new JButton("▶ Nghe");

    private Timer timer;
    private int timeLeft = 10;
    private volatile boolean answeredThisRound = false;
    private MusicResponse currentMusic;

    public GameView(Socket socket, Long gameId, JSONArray musicIds) {
        this.socket = socket;
        this.gameId = gameId;
        this.musicIds = musicIds;

        setTitle("🎵 Trận đấu âm thanh");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel: câu hỏi, timer, điểm
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        lblQuestion.setHorizontalAlignment(SwingConstants.LEFT);
        lblTimer.setHorizontalAlignment(SwingConstants.CENTER);
        lblScore.setHorizontalAlignment(SwingConstants.RIGHT);
        lblScore.setText("Điểm: 0");
        topPanel.add(lblQuestion);
        topPanel.add(lblTimer);
        topPanel.add(lblScore);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: 4 đáp án
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < 4; i++) {
            int index = i;
            answerButtons[i] = new JButton("Đáp án " + (i + 1));
            answerButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            answerButtons[i].addActionListener(e -> sendAnswer(index));
            answersPanel.add(answerButtons[i]);
        }
        add(answersPanel, BorderLayout.CENTER);

        // Bottom panel: nút nghe nhạc
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPlay.setEnabled(false);
        btnPlay.addActionListener(e -> playMusic());
        bottomPanel.add(btnPlay);
        add(bottomPanel, BorderLayout.SOUTH);

        registerSocketEvents();
        sendChangeQuestion(); // load câu hỏi đầu tiên

        setVisible(true);
    }

    /** Gửi yêu cầu server chuyển sang câu hỏi hiện tại */
    private void sendChangeQuestion() {
        try {
            if (musicIds == null || currentQuestionIndex >= musicIds.length()) {
                finishGame();
                return;
            }

            long musicId = musicIds.getLong(currentQuestionIndex);
            socket.emit("topic/changeQuestion", java.util.Map.of(
                    "musicId", String.valueOf(musicId),
                    "gameId", String.valueOf(gameId)
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerSocketEvents() {
        socket.on("topic/changeQuestion", args -> {
            JSONObject data = (JSONObject) args[0];
            SwingUtilities.invokeLater(() -> updateQuestionUI(data));
        });

        socket.on("topic/answerResult", args -> {
            JSONObject data = (JSONObject) args[0];
            SwingUtilities.invokeLater(() -> {
                score += data.optInt("score", 0);
                lblScore.setText("Điểm: " + score);
            });
        });

        socket.on("topic/finishGame", args -> {
            JSONObject data = (JSONObject) args[0];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Kết thúc trò chơi!\nTổng điểm của bạn: " + data.optInt("totalScore", score),
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                try {
                    new HomeView(socket).setVisible(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    /** Cập nhật UI dựa trên MusicResponse server trả về */
    private void updateQuestionUI(JSONObject data) {
        try {
            // Chuyển JSONObject thành MusicResponse
            Long id = data.getLong("id");
            String title = data.optString("title");
            String description = data.optString("description");
            String url = data.optString("url");

            List<AnswerResponse> answers = new ArrayList<>();
            JSONArray arr = data.optJSONArray("answers");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject ans = arr.getJSONObject(i);
                    answers.add(new AnswerResponse(ans.getLong("id"), ans.getString("description")));
                }
            }
            System.out.println(url);
            currentMusic = new MusicResponse(id, title, description, url, answers);

            lblQuestion.setText("Câu " + (currentQuestionIndex + 1) + "/" + musicIds.length() + ": " + title);
            btnPlay.setEnabled(url != null && !url.isEmpty());

            // Đặt các nút đáp án
            answeredThisRound = false;
            timeLeft = 10;
            for (int i = 0; i < 4; i++) {
                if (i < answers.size()) {
                    answerButtons[i].setText(answers.get(i).getDescription());
                    answerButtons[i].setEnabled(true);
                } else {
                    answerButtons[i].setText("Đáp án " + (i + 1));
                    answerButtons[i].setEnabled(false);
                }
            }

            startCountdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdown() {
        if (timer != null) timer.stop();
        lblTimer.setText("⏳ " + timeLeft + "s");

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                lblTimer.setText("⏳ " + timeLeft + "s");

                if (timeLeft <= 0) {
                    timer.stop();
                    disableButtons();
                    if (!answeredThisRound) autoSendAnswer();
                    currentQuestionIndex++;
                    sendChangeQuestion();
                }
            }
        });
        timer.start();
    }

    private void sendAnswer(int index) {
        if (answeredThisRound) return;
        answeredThisRound = true;
        disableButtons();

        try {
            long answerId = -1;
            if (currentMusic.getAnswers() != null && index < currentMusic.getAnswers().size()) {
                answerId = currentMusic.getAnswers().get(index).getId();
            }

            socket.emit("topic/handleAnswer", java.util.Map.of(
                    "musicId", String.valueOf(currentMusic.getId()),
                    "answerId", String.valueOf(answerId),
                    "gameId", String.valueOf(gameId)
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoSendAnswer() {
        answeredThisRound = true;
        try {
            socket.emit("topic/handleAnswer", java.util.Map.of(
                    "musicId", String.valueOf(currentMusic.getId()),
                    "answerId", "-1",
                    "gameId", String.valueOf(gameId)
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishGame() {
        try {
            socket.emit("topic/finishGame", java.util.Map.of(
                    "gameId", String.valueOf(gameId),
                    "score", String.valueOf(score)
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableButtons() {
        for (JButton btn : answerButtons) btn.setEnabled(false);
    }

    /** Nghe nhạc từ url */
    private void playMusic() {
        if (currentMusic == null || currentMusic.getUrl() == null || currentMusic.getUrl().isEmpty()) return;
        new Thread(() -> {
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new URL(currentMusic.getUrl()));
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}