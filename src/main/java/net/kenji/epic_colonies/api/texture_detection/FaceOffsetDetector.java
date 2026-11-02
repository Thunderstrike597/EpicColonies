package net.kenji.epic_colonies.api.texture_detection;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FaceOffsetDetector {

    private static final Map<ResourceLocation, Integer> OFFSET_CACHE = new HashMap<>();

    private static final int FACE_X = 8;
    private static final int FACE_Y = 8;
    private static final int FACE_WIDTH = 8;
    private static final int FACE_HEIGHT = 8;

    private static final int NORMAL_EYE_ROW = 4;

    private static final int MIN_ROW = 2;
    private static final int MAX_ROW = FACE_HEIGHT - 1;

    private static final int WHITE_THRESHOLD = 200;

    public static int getFaceOffset(ResourceLocation textureLocation) {
        return OFFSET_CACHE.computeIfAbsent(textureLocation, FaceOffsetDetector::loadAndDetectOffset);
    }

    private static int loadAndDetectOffset(ResourceLocation location) {
        try (NativeImage image = loadImage(location)) {
            return detectOffset(image);
        } catch (IOException e) {

            System.err.println("[FaceOffsetDetector] Failed to load " + location + ": " + e.getMessage());
            return 0;
        }
    }

    private static NativeImage loadImage(ResourceLocation location) throws IOException {
        try (InputStream stream = Minecraft.getInstance()
                .getResourceManager()
                .getResourceOrThrow(location)
                .open()) {
            return NativeImage.read(stream);
        }
    }

    private static int detectOffset(NativeImage image) {

        int bestRow = NORMAL_EYE_ROW;
        int bestCount = 0;

        for (int row = MIN_ROW; row <= MAX_ROW; row++) {
            int whiteCount = countNearWhiteInRow(image, FACE_X, FACE_Y + row, FACE_WIDTH);
            if (whiteCount > bestCount) {
                bestCount = whiteCount;
                bestRow = row;
            }
        }

        if (bestCount == 0) {

            System.err.println("[FaceOffsetDetector] No near-white pixels found in search range "
                    + "(rows " + MIN_ROW + "-" + MAX_ROW + ") - falling back to offset 0. "
                    + "Check WHITE_THRESHOLD and FACE_X/Y/WIDTH/HEIGHT.");
        }

        return bestRow - NORMAL_EYE_ROW;
    }

    private static int countNearWhiteInRow(NativeImage image, int startX, int y, int width) {
        int count = 0;

        for (int x = startX; x < startX + width; x++) {
            int argb = image.getPixelRGBA(x, y);

            int alpha = (argb >> 24) & 0xFF;
            if (alpha == 0) continue;

            int r = argb & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = (argb >> 16) & 0xFF;

            if (r >= WHITE_THRESHOLD && g >= WHITE_THRESHOLD && b >= WHITE_THRESHOLD) {
                count++;
            }
        }

        return count;
    }
}