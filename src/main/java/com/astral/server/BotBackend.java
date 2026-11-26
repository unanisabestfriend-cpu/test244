package com.astral.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.util.Locale;

/**
 * The ONLY class that talks to HeroBot (modrinth.com/mod/herobot).
 * HeroBot commands: /playerspawn <name> at <pos> in <gamemode> on <dimension>,
 * and /player <name> <action> (move, look, attack, jump, sprint, kill, disconnect, ...).
 * HeroBot has no pathfinding, so BotAI drives bots by issuing these commands every second.
 */
public class BotBackend {
    public static void run(MinecraftServer s, String cmd) {
        s.getCommandManager().parseAndExecute(s.getCommandSource().withSilent(), cmd);
    }

    public static void spawn(MinecraftServer s, String name, Vec3d pos, ServerWorld w) {
        run(s, String.format(Locale.ROOT, "playerspawn %s at %.1f %.1f %.1f in survival on astral:astral", name, pos.x, pos.y, pos.z));
    }
    public static void remove(MinecraftServer s, String name) { BotAI.clear(name); run(s, "player " + name + " disconnect"); }

    public static void walkRandomly(MinecraftServer s, String name) { BotAI.set(name, BotAI.Mode.WANDER, null); }
    public static void stand(MinecraftServer s, String name) { BotAI.set(name, BotAI.Mode.IDLE, null); }
    public static void hunt(MinecraftServer s, String name, ServerPlayerEntity target) { BotAI.set(name, BotAI.Mode.ATTACK, target.getName().getString()); }
    public static void attackBot(MinecraftServer s, String name, String target) { BotAI.set(name, BotAI.Mode.ATTACK, target); }
    public static void follow(MinecraftServer s, String name, ServerPlayerEntity target) { BotAI.set(name, BotAI.Mode.FOLLOW, target.getName().getString()); }
}
