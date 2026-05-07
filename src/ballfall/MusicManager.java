import java.io.File;
import java.lang.reflect.Method;

public class MusicManager {
    private Object player;
    private String current;

    public void playLoop(String fileName) {
        if (fileName == null || fileName.equals(current)) return;
        stop();
        current = fileName;
        try {
            Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
            Class<?> playerClass = Class.forName("javafx.scene.media.MediaPlayer");

            String uri = new File(fileName).toURI().toString();
            Object media = mediaClass.getConstructor(String.class).newInstance(uri);
            player = playerClass.getConstructor(mediaClass).newInstance(media);

            Method setCycleCount = playerClass.getMethod("setCycleCount", int.class);
            Method play = playerClass.getMethod("play");
            int indefinite = playerClass.getField("INDEFINITE").getInt(null);
            setCycleCount.invoke(player, indefinite);
            play.invoke(player);
        } catch (Exception ignored) {
            player = null;
        }
    }

    public void stop() {
        if (player == null) return;
        try {
            player.getClass().getMethod("stop").invoke(player);
        } catch (Exception ignored) {
        }
        player = null;
    }
}
