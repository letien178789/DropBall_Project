package ballfall;

import ballfall.ui.GamePanel;

import javax.swing.*;

public class BallFallGame extends JFrame {
    public BallFallGame() {
        setTitle("BALL FALL - Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(new GamePanel());
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BallFallGame().setVisible(true));
    }
}
