package org.ptit.b22cn539;

import javax.swing.SwingUtilities;
import org.ptit.b22cn539.Views.LoginView;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}