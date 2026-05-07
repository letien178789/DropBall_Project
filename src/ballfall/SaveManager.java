
import java.io.*;
import java.util.Properties;

public class SaveManager {
    private static final String SAVE_FILE = "save_state.properties";

    public static void load(SettingsData settings, ProgressData progress) {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;

        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            settings.bgmVolume = Float.parseFloat(p.getProperty("bgm_volume", "0.5"));
            settings.sfxVolume = Float.parseFloat(p.getProperty("sfx_volume", "0.5"));
            progress.lastLevel = Integer.parseInt(p.getProperty("last_level", "1"));
        } catch (Exception ignored) {
        }
    }

    public static void save(SettingsData settings, ProgressData progress) {
        Properties p = new Properties();
        p.setProperty("bgm_volume", String.valueOf(settings.bgmVolume));
        p.setProperty("sfx_volume", String.valueOf(settings.sfxVolume));
        p.setProperty("last_level", String.valueOf(progress.lastLevel));

        try (FileOutputStream fos = new FileOutputStream(SAVE_FILE)) {
            p.store(fos, "BALL FALL save state");
        } catch (IOException ignored) {
        }
    }
}
