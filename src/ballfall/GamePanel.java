
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements MouseListener, KeyListener, ActionListener {
    public static final int WIDTH = 480;
    public static final int HEIGHT = 800;

    private Scene scene = Scene.MENU;
    private final SettingsData settings = new SettingsData();
    private final ProgressData progress = new ProgressData();
    private final List<Layer> layers = new ArrayList<>();
    private final Random random = new Random();

    private int currentLevel = 1;
    private final int ballX = WIDTH / 2;
    private final int ballY = HEIGHT - 100;
    private final int ballR = 16;
    private int layersBroken = 0;
    private int combo = 0;
    private long lastBreakMs = 0;
    private long invincibleUntil = 0;
    private boolean paused = false, gameOver = false, win = false;

    private final GameButton pauseBtn = new GameButton(WIDTH - 105, 18, 85, 38, "Pause");
    private final GameButton resumeBtn = new GameButton(WIDTH / 2 - 100, 300, 200, 48, "Resume");
    private final GameButton pauseSettingBtn = new GameButton(WIDTH / 2 - 100, 360, 200, 48, "Setting");
    private final GameButton pauseExitBtn = new GameButton(WIDTH / 2 - 100, 420, 200, 48, "Exit");

    private final GameButton menuPlay = new GameButton(140, 260, 200, 52, "Play");
    private final GameButton menuEnter = new GameButton(140, 330, 200, 52, "Vao game");
    private final GameButton menuLevel = new GameButton(140, 400, 200, 52, "Menu Level");
    private final GameButton menuSetting = new GameButton(140, 470, 200, 52, "Setting");

    private final GameButton backBtn = new GameButton(20, 20, 90, 38, "Back");
    private final List<GameButton> levelButtons = new ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        SaveManager.load(settings, progress);
        currentLevel = Math.max(1, Math.min(10, progress.lastLevel));
        resetLevel(currentLevel);

        for (int i = 0; i < 10; i++) {
            int y = 140 + i * 58;
            levelButtons.add(new GameButton(130 + (i % 2) * 120, y - (i % 2) * 24, 100, 44, "Level " + (i + 1)));
        }

        new Timer(16, this).start();
    }

    private void resetLevel(int lv) {
        currentLevel = lv;
        progress.lastLevel = lv;
        SaveManager.save(settings, progress);

        layers.clear();
        for (int i = 0; i < 12 + lv * 4; i++) {
            int y = HEIGHT - 160 - i * 52;
            int pb = random.nextBoolean() ? 110 : 250;
            int db = pb == 110 ? 250 : 110;
            layers.add(new Layer(y, pb, db));
        }

        layersBroken = 0;
        combo = 0;
        lastBreakMs = 0;
        invincibleUntil = 0;
        paused = false;
        gameOver = false;
        win = false;
    }

    private Layer nearestLayer() {
        Layer best = null;
        for (Layer l : layers) if (l.y < ballY && (best == null || l.y > best.y)) best = l;
        return (best == null && !layers.isEmpty()) ? layers.get(0) : best;
    }

    private void hitAction() {
        if (gameOver || paused || win) return;
        Layer l = nearestLayer();
        if (l == null) return;

        long now = System.currentTimeMillis();
        Rectangle pb = new Rectangle(l.pbX, l.y, 120, 22);
        Rectangle db = new Rectangle(l.dbX, l.y, 120, 22);

        if (pb.contains(ballX, ballY) || now < invincibleUntil) {
            layers.remove(l);
            layersBroken++;
            combo = (now - lastBreakMs <= 450) ? combo + 1 : 1;
            lastBreakMs = now;

            if (combo >= 10) {
                invincibleUntil = now + 5000;
                combo = 0;
            }

            if (layers.isEmpty()) {
                win = true;
                if (currentLevel < 10) {
                    progress.lastLevel = currentLevel + 1;
                    SaveManager.save(settings, progress);
                }
            }
        } else if (db.contains(ballX, ballY)) {
            gameOver = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch (scene) {
            case MENU -> drawMenu(g2);
            case LEVEL_MENU -> drawLevelMenu(g2);
            case SETTING -> drawSetting(g2);
            case GAME -> drawGame(g2);
        }
    }

    private void drawMenu(Graphics2D g2) { /* unchanged drawing */
        g2.setColor(new Color(12, 14, 24)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 48)); g2.drawString("BALL FALL", 120, 150);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        menuPlay.draw(g2); menuEnter.draw(g2); menuLevel.draw(g2); menuSetting.draw(g2);
    }

    private void drawLevelMenu(Graphics2D g2) {
        g2.setColor(new Color(20, 22, 35)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("Level Menu", 170, 80);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        backBtn.draw(g2);
        for (GameButton b : levelButtons) b.draw(g2);
    }

    private void drawSlider(Graphics2D g2, String label, int y, float value) {
        g2.setColor(Color.WHITE); g2.drawString(label, 100, y - 10);
        g2.setColor(new Color(65, 65, 90)); g2.fillRoundRect(100, y, 280, 10, 5, 5);
        g2.setColor(new Color(130, 190, 255)); g2.fillRoundRect(100, y, (int) (280 * value), 10, 5, 5);
        g2.setColor(new Color(240, 240, 255)); g2.fillOval(100 + (int) (280 * value) - 8, y - 5, 16, 16);
    }

    private void drawSetting(Graphics2D g2) {
        g2.setColor(new Color(18, 16, 30)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("Setting", 190, 80);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        backBtn.draw(g2);
        drawSlider(g2, "Background sound", 200, settings.bgmVolume);
        drawSlider(g2, "Effect sound", 360, settings.sfxVolume);
    }

    private void drawGame(Graphics2D g2) {
        g2.setColor(new Color(15, 16, 28)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(new Color(50, 58, 80));
        for (int x : new int[]{90, 230, 370}) g2.fillRect(x, 70, 20, HEIGHT - 90);

        for (Layer l : layers) {
            g2.setColor(new Color(90, 200, 120)); g2.fillRoundRect(l.pbX, l.y, 120, 22, 5, 5);
            g2.setColor(new Color(200, 70, 80)); g2.fillRoundRect(l.dbX, l.y, 120, 22, 5, 5);
        }

        g2.setColor(System.currentTimeMillis() < invincibleUntil ? new Color(255, 210, 50) : new Color(240, 240, 245));
        g2.fillOval(ballX - ballR, ballY - ballR, ballR * 2, ballR * 2);
        g2.setColor(Color.WHITE);
        g2.drawString("Level " + currentLevel + " | Broken " + layersBroken, 20, 35);
        pauseBtn.draw(g2);

        if (System.currentTimeMillis() < invincibleUntil) {
            g2.setColor(new Color(255, 220, 40));
            g2.drawString("INVINCIBLE: " + String.format("%.1f", (invincibleUntil - System.currentTimeMillis()) / 1000.0) + "s", 20, 60);
        }

        if (gameOver) overlay(g2, "GAME OVER", "R: retry | M: level menu");
        if (win) overlay(g2, "YOU WIN", "N: next level | M: level menu");
        if (paused) {
            g2.setColor(new Color(0, 0, 0, 160)); g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setColor(Color.WHITE); g2.drawString("Paused", WIDTH / 2 - 30, 260);
            resumeBtn.draw(g2); pauseSettingBtn.draw(g2); pauseExitBtn.draw(g2);
        }
    }

    private void overlay(Graphics2D g2, String title, String hint) {
        g2.setColor(new Color(0, 0, 0, 140)); g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 50)); g2.drawString(title, WIDTH / 2 - 150, HEIGHT / 2 - 40);
        g2.setFont(new Font("Arial", Font.PLAIN, 20)); g2.drawString(hint, WIDTH / 2 - 120, HEIGHT / 2);
    }

    @Override public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        if (scene == Scene.MENU) {
            if (menuPlay.clicked(p)) { resetLevel(progress.lastLevel); scene = Scene.GAME; }
            else if (menuEnter.clicked(p)) { resetLevel(currentLevel); scene = Scene.GAME; }
            else if (menuLevel.clicked(p)) scene = Scene.LEVEL_MENU;
            else if (menuSetting.clicked(p)) scene = Scene.SETTING;
        } else if (scene == Scene.LEVEL_MENU) {
            if (backBtn.clicked(p)) scene = Scene.MENU;
            for (int i = 0; i < levelButtons.size(); i++) if (levelButtons.get(i).clicked(p)) { resetLevel(i + 1); scene = Scene.GAME; }
        } else if (scene == Scene.SETTING) {
            if (backBtn.clicked(p)) scene = Scene.MENU;
            updateSlider(p);
        } else {
            if (paused) {
                if (resumeBtn.clicked(p)) paused = false;
                else if (pauseSettingBtn.clicked(p)) scene = Scene.SETTING;
                else if (pauseExitBtn.clicked(p)) { paused = false; scene = Scene.LEVEL_MENU; }
            } else {
                if (pauseBtn.clicked(p)) paused = true;
                else hitAction();
            }
        }
        repaint();
    }

    private void updateSlider(Point p) {
        if (p.y >= 190 && p.y <= 220 && p.x >= 100 && p.x <= 380) settings.bgmVolume = (p.x - 100) / 280f;
        if (p.y >= 350 && p.y <= 380 && p.x >= 100 && p.x <= 380) settings.sfxVolume = (p.x - 100) / 280f;
        SaveManager.save(settings, progress);
    }

    @Override public void keyPressed(KeyEvent e) {
        if (scene == Scene.GAME) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) hitAction();
            else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) resetLevel(currentLevel);
            else if (e.getKeyCode() == KeyEvent.VK_M && (gameOver || win)) scene = Scene.LEVEL_MENU;
            else if (e.getKeyCode() == KeyEvent.VK_N && win) resetLevel(Math.min(10, currentLevel + 1));
        }
        repaint();
    }

    @Override public void actionPerformed(ActionEvent e) { repaint(); }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
