package com.astral.server;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import com.astral.AstralMod;
import java.nio.file.*;
import java.util.*;

/**
 * The Battle of the Council of Kings: Lawmakers vs Parrot's Resistance (led by ParrotX2).
 * IDLE -> WARNING (2 min countdown, announced) -> BATTLE (max 6 min) -> IDLE.
 * Auto-starts every WAR_INTERVAL ticks, or with /astral war start. The winner rules.
 */
public class War {
    public enum Phase { IDLE, WARNING, BATTLE }
    public static Faction ruler = Faction.LAWMAKERS;   // Lawmakers rule at first
    public static Phase phase = Phase.IDLE;
    static final int WAR_INTERVAL = 20 * 60 * 45, WARNING = 20 * 120, MAX_BATTLE = 20 * 60 * 6, SIDE = 14;
    static long nextWar = WAR_INTERVAL, phaseStart;
    static final List<String> law = new ArrayList<>(), res = new ArrayList<>();
    static long clock = 0;
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("astral_war.json");

    public static void load() {
        try { if (Files.exists(FILE)) ruler = Faction.valueOf(JsonParser.parseString(Files.readString(FILE)).getAsJsonObject().get("ruler").getAsString()); }
        catch (Exception ignored) {}
    }
    static void save() { try { Files.writeString(FILE, "{\"ruler\":\"" + ruler + "\"}"); } catch (Exception ignored) {} }

    static void say(MinecraftServer s, String msg) { s.getPlayerManager().broadcast(Text.literal("\u00a76[War] \u00a7f" + msg), false); }

    public static void tick(MinecraftServer s) {
        clock++;
        if (phase == Phase.IDLE && clock >= nextWar && !s.getPlayerManager().getPlayerList().isEmpty()) begin(s);
        if (phase == Phase.WARNING) {
            long left = WARNING - (clock - phaseStart);
            if (left == 20 * 60) say(s, "One minute until the Battle of the Council of Kings!");
            if (left <= 0) fight(s);
        }
        if (phase == Phase.BATTLE && clock % 40 == 0) {
            law.removeIf(n -> !BotManager.alive(s, n));
            res.removeIf(n -> !BotManager.alive(s, n));
            retarget(s);
            if (res.isEmpty()) end(s, Faction.LAWMAKERS);
            else if (law.isEmpty()) end(s, Faction.RESISTANCE);
            else if (clock - phaseStart > MAX_BATTLE) end(s, res.size() >= law.size() ? Faction.RESISTANCE : Faction.LAWMAKERS);
        }
    }

    public static void begin(MinecraftServer s) {
        if (phase != Phase.IDLE) return;
        phase = Phase.WARNING; phaseStart = clock;
        say(s, "ParrotX2 marches on the Council of Kings! The battle begins in 2 minutes. Pick a side - or stay out of it.");
        say(s, "Council of Kings is at " + Region.COUNCIL_OF_KINGS.cx + ", " + Region.COUNCIL_OF_KINGS.cz + ".");
    }

    private static void fight(MinecraftServer s) {
        ServerWorld w = s.getWorld(AstralMod.ASTRAL);
        if (w == null) { phase = Phase.IDLE; return; }
        phase = Phase.BATTLE; phaseStart = clock;
        Region r = Region.COUNCIL_OF_KINGS;
        Random rnd = new Random();
        for (int i = 0; i < SIDE; i++) {
            String ln = "WarLaw" + i, rn = (i == 0) ? "ParrotX2" : "WarRes" + i;
            int rank = i == 0 ? 4 : Math.min(3, i / 4);              // 1 leader, then ranks 0-3
            BotManager.spawnWar(s, w, ln, BotManager.Role.LAWMAKER, rank, new Vec3d(r.cx + 12 + rnd.nextInt(6), MapBuilder.Y + 1, r.cz - 8 + rnd.nextInt(16)), i == 0);
            BotManager.spawnWar(s, w, rn, BotManager.Role.RESISTANCE, 0, new Vec3d(r.cx - 28 + rnd.nextInt(6), MapBuilder.Y + 1, r.cz - 8 + rnd.nextInt(16)), i == 0);
            law.add(ln); res.add(rn);
        }
        say(s, "The battle has begun! Lawmakers hold the east plaza, Parrot's Resistance the west.");
        s.getPlayerManager().getPlayerList().forEach(p -> ProfileStore.get(p.getUuid()).achievements.add("Witnessed the war"));
    }

    private static void retarget(MinecraftServer s) {
        Random rnd = new Random();
        for (String n : law) if (!res.isEmpty()) BotBackend.attackBot(s, n, res.get(rnd.nextInt(res.size())));
        for (String n : res) if (!law.isEmpty()) BotBackend.attackBot(s, n, law.get(rnd.nextInt(law.size())));
    }

    /** winner == null cancels the war. */
    public static void end(MinecraftServer s, Faction winner) {
        for (String n : new ArrayList<>(law)) BotManager.forget(s, n);
        for (String n : new ArrayList<>(res)) BotManager.forget(s, n);
        law.clear(); res.clear();
        phase = Phase.IDLE; nextWar = clock + WAR_INTERVAL;
        if (winner == null) { say(s, "The war has been called off."); return; }
        ruler = winner; save();
        say(s, winner == Faction.RESISTANCE
            ? "Parrot's Resistance has toppled the Lawmakers! ParrotX2's banner flies over Astral."
            : "The Lawmakers crush the uprising. Their rule continues.");
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            PlayerProfile prof = ProfileStore.get(p.getUuid());
            if (prof.faction == winner) {
                prof.addHonor(10); prof.achievements.add("Victor: Battle of the Council of Kings");
                if (winner == Faction.LAWMAKERS && prof.lawRank < 4) prof.lawRank++;
            } else if (prof.faction != Faction.FREE) prof.addHonor(-5);
            AstralMod.sync(p);
        }
    }
}
