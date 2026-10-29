package net.kenji.epic_colonies.api.data;

import com.google.gson.JsonParser;
import net.kenji.epic_colonies.EpicColonies;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CitizenTextureData {
    public static final List<ResourceLocation> BIG_EYE_TEXTURES = load("child_bigeyes");
    public static final List<ResourceLocation> LOWER_EYE_TEXTURES = load("child_lowereyes");

    private CitizenTextureData() {}

    public static List<ResourceLocation> load(String fileName) {
        String path = "data/" + EpicColonies.MODID + "/texture_data/" + fileName + ".json";
        try (var stream = CitizenTextureData.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing citizen texture data: " + path);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                var textures = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("valid_textures");
                List<ResourceLocation> locations = new ArrayList<>();
                for (var element : textures) {
                    locations.add(ResourceLocation.fromNamespaceAndPath("minecolonies",
                            "textures/entity/citizen/" + element.getAsJsonObject().get("texture").getAsString()));
                }
                return List.copyOf(locations);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load citizen texture data: " + path, e);
        }
    }
}
