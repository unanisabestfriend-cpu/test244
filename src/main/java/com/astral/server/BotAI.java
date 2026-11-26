package com.astral.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

/** Tiny brain for HeroBot players: runs once a second per bot using /player commands. */
public class BotAI {
    public enum Mode { IDLE, WANDER, FOLLOW, ATTACK }
    record State(Mode mode, String target) {}
    private static final Map<String, State> STATES = new HashMap<>();
    private static final Random R = new Random();
    private static final Set<String> MOVING = new HashSet<>();

    public static void set(String bot, Mode m, String target) { STATES.put(bot, new State(m, target)); }
    public static void clear(String bot) { STATES.remove(bot); MOVING.remove(bot); }

    public static void tick(MinecraftServer s) {
        if (s.getTicks() % 20 != 0) return;
        for (var it = STATES.entrySet().iterator(); it.hasNext();) {
            var e = it.next(); String n = e.getKey(); State st = e.getValue();
            ServerPlayerEntity bot = s.getPlayerManager().getPlayer(n);
            if (bot == null) continue;                        // still spawning, or gone
            ServerPlayerEntity tgt = st.target() == null ? null : s.getPlayerManager().getPlayer(st.target());
            switch (st.mode()) {
                case IDLE -> { if (MOVING.remove(n)) BotBackend.run(s, "player " + n + " stop"); }
                case WANDER -> {
                    if (R.nextInt(5) != 0) break;             // change mind every ~5 s
                    if (R.nextInt(10) < 3) { BotBackend.run(s, "player " + n + " stop"); MOVING.remove(n); }
                    else {
                        BotBackend.run(s, "player " + n + " look " + (R.nextInt(360) - 180) + " 0");
                        BotBackend.run(s, "player " + n + " autojump true");
                        BotBackend.run(s, "player " + n + " move forward");
                        MOVING.add(n);
                    }
                }
                case FOLLOW -> {
                    if (tgt == null) break;
                    double d = bot.distanceTo(tgt);
                    if (d > 3.5) { BotBackend.run(s, "player " + n + " look upon " + st.target()); BotBackend.run(s, "player " + n + " autojump true");
                        BotBackend.run(s, "player " + n + " move forward"); BotBackend.run(s, d > 10 ? "player " + n + " sprint" : "player " + n + " unsprint"); }
                    else BotBackend.run(s, "player " + n + " stop");
                }
                case ATTACK -> {
                    if (tgt == null) break;
                    double d = bot.distanceTo(tgt);
                    BotBackend.run(s, "player " + n + " look upon " + st.target());
                    if (d > 3.0) { BotBackend.run(s, "player " + n + " attack"); BotBackend.run(s, "player " + n + " autojump true");
                        BotBackend.run(s, "player " + n + " move forward"); BotBackend.run(s, "player " + n + " sprint"); }
                    else { BotBackend.run(s, "player " + n + " move"); BotBackend.run(s, "player " + n + " attack continuous"); }
                }
            }
        }
    }
}
