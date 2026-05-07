import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements MouseListener, KeyListener, ActionListener {
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    private Scene scene = Scene.MENU;
    private final SettingsData settings = new SettingsData();
    private final ProgressData progress = new ProgressData();
    private final List<Layer> layers = new ArrayList<>();
    private final Random random = new Random();

    private int currentLevel = 1;
    private final int centerX = WIDTH / 2;
    private final int baseBallY = HEIGHT - 110;
    private final int ballR = 16;
    private int ballY = baseBallY;
    private double ballVelocity = 0;
    private boolean dropping = false;

    private int layersBroken = 0;
    private int combo = 0;
    private long lastBreakMs = 0;
    private long invincibleUntil = 0;
    private boolean paused = false, gameOver = false, win = false;

    private static final int RING_SIZE = 120;
    private static final int RING_STROKE = 16;
    private static final int HIT_ANGLE = 90; // phía dưới của vòng (tọa độ Swing)

    private final GameButton pauseBtn = new GameButton(WIDTH - 105, 18, 85, 38, "Pause");
    private final GameButton resumeBtn = new GameButton(WIDTH / 2 - 100, 300, 200, 48, "Resume");
    private final GameButton pauseSettingBtn = new GameButton(WIDTH / 2 - 100, 360, 200, 48, "Setting");
    private final GameButton pauseExitBtn = new GameButton(WIDTH / 2 - 100, 420, 200, 48, "Exit");

    private final GameButton menuPlay = new GameButton(WIDTH/2 - 140, 360, 280, 70, "Play");
    private final GameButton menuLevel = new GameButton(WIDTH/2 - 140, 450, 280, 70, "Level");
    private final GameButton menuSetting = new GameButton(WIDTH/2 - 140, 540, 280, 70, "Setting");
    private final GameButton replayBtn = new GameButton(WIDTH / 2 - 100, HEIGHT / 2 + 20, 200, 48, "Replay");
    private final GameButton nextLevelBtn = new GameButton(WIDTH / 2 - 120, HEIGHT / 2 + 20, 240, 48, "Next Level");

    private final GameButton backBtn = new GameButton(20, 20, 90, 38, "Back");
    private final List<GameButton> levelButtons = new ArrayList<>();

    private BufferedImage bgHome;
    private BufferedImage bgMenu;
    private BufferedImage bgGame;
    private final MusicManager musicManager = new MusicManager();


    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        loadBackgrounds();

        SaveManager.load(settings, progress);
        currentLevel = Math.max(1, Math.min(10, progress.lastLevel));
        resetLevel(currentLevel);

        for (int i = 0; i < 10; i++) {
            int y = 140 + i * 58;
            levelButtons.add(new GameButton(130 + (i % 2) * 120, y - (i % 2) * 24, 100, 44, "Level " + (i + 1)));
        }

        setScene(Scene.MENU);
        new Timer(16, this).start();
    }

    private void loadBackgrounds() {
        try { bgHome = ImageIO.read(new File("BG1.png")); } catch (Exception ignored) {}
        try { bgMenu = ImageIO.read(new File("BG2.png")); } catch (Exception ignored) {}
        try { bgGame = ImageIO.read(new File("BG3.png")); } catch (Exception ignored) {}
    }

    private void drawBackground(Graphics2D g2, BufferedImage img, Color fallback) {
        if (img != null) g2.drawImage(img, 0, 0, WIDTH, HEIGHT, null);
        else { g2.setColor(fallback); g2.fillRect(0, 0, WIDTH, HEIGHT); }
    }

    private Color randomPBColor() {
        Color[] colors = new Color[]{
                new Color(90, 200, 120),
                new Color(70, 175, 240),
                new Color(240, 200, 70),
                new Color(170, 120, 255),
                new Color(80, 220, 200)
        };
        return colors[random.nextInt(colors.length)];
    }

    private void resetLevel(int lv) {
        currentLevel = lv;
        progress.lastLevel = lv;
        SaveManager.save(settings, progress);

        layers.clear();
        int dangerArc = Math.min(36 + lv * 6, 120);
        for (int i = 0; i < 12 + lv * 4; i++) {
            int y = HEIGHT - 170 - i * 56;
            double startAngle = random.nextInt(360);
            double speed = 0.8 + (currentLevel * 0.12) + random.nextDouble() * 0.8;
            layers.add(new Layer(y, dangerArc, randomPBColor(), startAngle, speed));
        }

        layersBroken = 0;
        combo = 0;
        lastBreakMs = 0;
        invincibleUntil = 0;
        paused = false;
        gameOver = false;
        win = false;
        ballY = baseBallY;
        ballVelocity = 0;
        dropping = false;
    }

    private Layer nearestLayer() {
        Layer best = null;
        for (Layer l : layers) if (l.y < ballY && (best == null || l.y > best.y)) best = l;
        return (best == null && !layers.isEmpty()) ? layers.get(0) : best;
    }

    private boolean isDangerAtHit(Layer layer) {
        int start = ((int) layer.angle % 360 + 360) % 360;
        int end = (start + layer.dangerArc) % 360;
        if (start <= end) {
            return HIT_ANGLE >= start && HIT_ANGLE <= end;
        }
        return HIT_ANGLE >= start || HIT_ANGLE <= end;
    }

    private void hitAction() {
        if (gameOver || paused || win) return;
        dropping = true;
        ballVelocity = 13;

        Layer l = nearestLayer();
        if (l == null) return;

        long now = System.currentTimeMillis();
        boolean dangerHit = isDangerAtHit(l);

        if (!dangerHit || now < invincibleUntil) {
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
        } else {
            gameOver = true;
        }
    }

    private void updateBallPhysics() {
        if (scene != Scene.GAME || paused || gameOver || win) return;

        if (dropping) {
            ballY += (int) ballVelocity;
            ballVelocity -= 1.3;
            if (ballY >= baseBallY) {
                ballY = baseBallY;
                dropping = false;
                ballVelocity = 0;
            }
        } else {
            double t = System.currentTimeMillis() / 140.0;
            ballY = baseBallY + (int) (Math.sin(t) * 8);
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

    private void setScene(Scene nextScene) {
        this.scene = nextScene;
        if (nextScene == Scene.MENU) musicManager.playLoop("MB1.mp3");
        else if (nextScene == Scene.LEVEL_MENU || nextScene == Scene.SETTING) musicManager.playLoop("MB2.mp3");
        else if (nextScene == Scene.GAME) musicManager.playLoop("MB3.mp3");
    }

    private void drawMenu(Graphics2D g2) {
        drawBackground(g2, bgHome, new Color(12, 14, 24));
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 96)); g2.drawString("BALL FALL", WIDTH/2 - 260, 180);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        menuPlay.draw(g2); menuLevel.draw(g2); menuSetting.draw(g2);
    }

    private void drawLevelMenu(Graphics2D g2) {
        drawBackground(g2, bgMenu, new Color(20, 22, 35));
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
        drawBackground(g2, bgMenu, new Color(18, 16, 30));
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("Setting", 190, 80);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        backBtn.draw(g2);
        drawSlider(g2, "Background sound", 200, settings.bgmVolume);
        drawSlider(g2, "Effect sound", 360, settings.sfxVolume);
    }

    private void drawGame(Graphics2D g2) {
        drawBackground(g2, bgGame, new Color(15, 16, 28));

        g2.setColor(new Color(50, 58, 80));
        g2.fillRect(centerX - 16, 70, 32, HEIGHT - 90);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(RING_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Layer l : layers) {
            int ringX = centerX - RING_SIZE / 2;
            int ringY = l.y - RING_SIZE / 2;

            g2.setColor(new Color(0,0,0,80));
            g2.drawArc(ringX + 3, ringY + 3, RING_SIZE, RING_SIZE, 0, 360);

            g2.setColor(l.pbColor);
            g2.drawArc(ringX, ringY, RING_SIZE, RING_SIZE, 0, 360);
            g2.setColor(l.pbColor.brighter());
            g2.drawArc(ringX - 1, ringY - 1, RING_SIZE, RING_SIZE, 210, 110);
            g2.setColor(l.pbColor.darker());
            g2.drawArc(ringX + 1, ringY + 1, RING_SIZE, RING_SIZE, 30, 140);

            g2.setColor(new Color(220, 45, 45));
            g2.drawArc(ringX, ringY, RING_SIZE, RING_SIZE, (int) l.angle, l.dangerArc);
            g2.setColor(new Color(255, 130, 130));
            g2.drawArc(ringX - 1, ringY - 1, RING_SIZE, RING_SIZE, (int) l.angle, Math.max(8, l.dangerArc / 3));
        }
        g2.setStroke(oldStroke);

        g2.setColor(System.currentTimeMillis() < invincibleUntil ? new Color(255, 210, 50) : new Color(240, 240, 245));
        g2.fillOval(centerX - ballR, ballY - ballR, ballR * 2, ballR * 2);

        g2.setColor(new Color(25, 30, 45, 220));
        g2.fillRoundRect(20, 16, 113, 31, 10, 10);
        g2.setColor(new Color(170, 190, 255));
        g2.drawRoundRect(20, 16, 113, 31, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("LEVEL " + currentLevel, 30, 37);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("Broken: " + layersBroken, 220, 42);
        pauseBtn.draw(g2);

        if (System.currentTimeMillis() < invincibleUntil) {
            g2.setColor(new Color(255, 220, 40));
            g2.drawString("INVINCIBLE: " + String.format("%.1f", (invincibleUntil - System.currentTimeMillis()) / 1000.0) + "s", 20, 60);
        }

        if (gameOver) {
            overlay(g2, "GAME OVER", "M: level menu");
            replayBtn.draw(g2);
        }
        if (win) {
            overlay(g2, "YOU WIN", "M: level menu");
            nextLevelBtn.draw(g2);
        }
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
            if (menuPlay.clicked(p)) { resetLevel(progress.lastLevel); setScene(Scene.GAME); }
            else if (menuLevel.clicked(p)) setScene(Scene.LEVEL_MENU);
            else if (menuSetting.clicked(p)) setScene(Scene.SETTING);
        } else if (scene == Scene.LEVEL_MENU) {
            if (backBtn.clicked(p)) setScene(Scene.MENU);
            for (int i = 0; i < levelButtons.size(); i++) if (levelButtons.get(i).clicked(p)) { resetLevel(i + 1); setScene(Scene.GAME); }
        } else if (scene == Scene.SETTING) {
            if (backBtn.clicked(p)) setScene(Scene.MENU);
            updateSlider(p);
        } else {
            if (gameOver && replayBtn.clicked(p)) {
                resetLevel(currentLevel);
            } else if (win && nextLevelBtn.clicked(p)) {
                resetLevel(Math.min(10, currentLevel + 1));
            } else if (paused) {
                if (resumeBtn.clicked(p)) paused = false;
                else if (pauseSettingBtn.clicked(p)) setScene(Scene.SETTING);
                else if (pauseExitBtn.clicked(p)) { paused = false; setScene(Scene.LEVEL_MENU); }
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
            else if (e.getKeyCode() == KeyEvent.VK_M && (gameOver || win)) setScene(Scene.LEVEL_MENU);
            else if (e.getKeyCode() == KeyEvent.VK_N && win) resetLevel(Math.min(10, currentLevel + 1));
        }
        repaint();
    }

    @Override public void actionPerformed(ActionEvent e) {
        updateBallPhysics();
        if (scene == Scene.GAME && !paused && !gameOver && !win) {
            for (Layer l : layers) {
                l.angle = (l.angle + l.rotationSpeed) % 360;
            }
        }
        repaint();
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
