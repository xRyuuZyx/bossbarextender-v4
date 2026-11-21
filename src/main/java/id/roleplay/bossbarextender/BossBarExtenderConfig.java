package id.roleplay.bossbarextender;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BossBarExtenderConfig {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("bossbar-extender.properties");

    // 100% allows the vanilla renderer to use the full scaled screen height.
    private static volatile Settings settings = new Settings(100);

    private BossBarExtenderConfig() {
    }

    public static void load() {
        Properties properties = new Properties();
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.notExists(FILE)) {
                properties.setProperty("max-screen-usage-percent", "100");
                try (Writer writer = Files.newBufferedWriter(FILE)) {
                    properties.store(writer,
                            "BossBar Extender. Set max-screen-usage-percent between 50 and 100.");
                }
            }

            try (Reader reader = Files.newBufferedReader(FILE)) {
                properties.load(reader);
            }

            int screenPercent = clamp(parse(properties, "max-screen-usage-percent", 100), 50, 100);
            settings = new Settings(screenPercent);
            System.out.println("[BossBar Extender] Expanded vanilla BossBar render area to "
                    + screenPercent + "% of the scaled screen height.");
        } catch (IOException exception) {
            System.err.println("[BossBar Extender] Could not read config; using defaults: "
                    + exception.getMessage());
            settings = new Settings(100);
        }
    }

    public static Settings get() {
        return settings;
    }

    private static int parse(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Settings(int maxScreenUsagePercent) {
    }
}
