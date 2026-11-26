package com.astral.server;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ProfileStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("astral_profiles.json");
    private static final Map<UUID, PlayerProfile> MAP = new HashMap<>();

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (var e : o.entrySet()) MAP.put(UUID.fromString(e.getKey()), GSON.fromJson(e.getValue(), PlayerProfile.class));
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    public static void save() {
        try {
            JsonObject o = new JsonObject();
            MAP.forEach((k, v) -> o.add(k.toString(), GSON.toJsonTree(v)));
            Files.writeString(FILE, GSON.toJson(o));
        } catch (IOException ex) { ex.printStackTrace(); }
    }
    public static PlayerProfile get(UUID id) { return MAP.computeIfAbsent(id, k -> new PlayerProfile()); }
}
