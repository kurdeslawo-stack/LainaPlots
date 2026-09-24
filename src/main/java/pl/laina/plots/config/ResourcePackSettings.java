package pl.laina.plots.config;

import org.bukkit.configuration.file.FileConfiguration;

public record ResourcePackSettings(
        boolean enabled,
        String url,
        boolean required,
        String prompt,
        String fileName,
        boolean serveEnabled,
        String bindAddress,
        int port
) {
    public static ResourcePackSettings load(FileConfiguration config) {
        return new ResourcePackSettings(
                config.getBoolean("resource-pack.enabled", false),
                config.getString("resource-pack.url", ""),
                config.getBoolean("resource-pack.required", false),
                config.getString("resource-pack.prompt", "<green>Ikony menu działek LainaPlots"),
                fileName(config.getString("resource-pack.file", "LainaPlots-Icons.zip")),
                config.getBoolean("resource-pack.serve.enabled", false),
                config.getString("resource-pack.serve.bind-address", "0.0.0.0"),
                Math.max(1, Math.min(65535, config.getInt("resource-pack.serve.port", 8124)))
        );
    }

    private static String fileName(String configured) {
        if (configured == null || configured.isBlank() || configured.contains("/") || configured.contains("\\")) {
            return "LainaPlots-Icons.zip";
        }
        return configured;
    }
}
