package pl.laina.plots.resourcepack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class BundledResourcePackTest {
    private static final Set<String> TEXTURES = Set.of(
            "assets/lainaplots/textures/item/owner_plot.png",
            "assets/lainaplots/textures/item/member_plot.png",
            "assets/lainaplots/textures/item/favorite_owner_plot.png",
            "assets/lainaplots/textures/item/favorite_member_plot.png",
            "assets/lainaplots/textures/item/summary.png"
    );

    @Test
    void containsFiveTransparentSixtyFourPixelIconsAndPaperDispatcher() throws Exception {
        Map<String, byte[]> entries = new HashMap<>();
        try (InputStream resource = BundledResourcePackTest.class.getResourceAsStream("/LainaPlots-Icons.zip")) {
            assertNotNull(resource);
            try (ZipInputStream zip = new ZipInputStream(resource)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        entries.put(entry.getName(), zip.readAllBytes());
                    }
                }
            }
        }

        assertTrue(entries.containsKey("pack.mcmeta"));
        assertTrue(entries.containsKey("assets/minecraft/items/paper.json"));
        for (String texture : TEXTURES) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(entries.get(texture)));
            assertNotNull(image, texture);
            assertEquals(64, image.getWidth(), texture);
            assertEquals(64, image.getHeight(), texture);
            assertTrue(image.getColorModel().hasAlpha(), texture);
        }
    }
}
