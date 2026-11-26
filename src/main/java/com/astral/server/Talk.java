package com.astral.server;

import com.astral.AstralMod;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import java.util.*;

public class Talk {
    private static final Random R = new Random();
    private static final String[] INTERESTS = {
        "I heard Parrot is gathering an army near the Council of Kings.",
        "Merchants in New Merchant City pay well for diamonds.",
        "The Lawmakers are tightening patrols. Keep your honor high.",
        "Follow the obsidian bridge from spawn to reach the Capital District."};

    public static void handle(ServerPlayerEntity p, String bot, String action) {
        PlayerProfile prof = ProfileStore.get(p.getUuid());
        var server = p.getEntityWorld().getServer();
        var info = BotManager.byName(bot);
        if (info == null) return;
        int trust = prof.trust.getOrDefault(bot, 0);
        switch (action.split("_")[0]) {
            case "insult" -> { prof.addHonor(-2); prof.trust.put(bot, Math.max(-5, trust - 2)); p.sendMessage(Text.literal(bot + ": Watch your tongue."), false); }
            case "ask" -> p.sendMessage(Text.literal(bot + ": " + (trust >= 0 ? INTERESTS[R.nextInt(INTERESTS.length)] : "I've nothing to say to you.")), false);
            case "chat" -> { prof.trust.put(bot, trust + 1); prof.addHonor(1); p.sendMessage(Text.literal(bot + ": Pleasant talk, traveller."), false); }
            case "buddy" -> {
                if (trust >= 5) { BotBackend.follow(server, bot, p); p.sendMessage(Text.literal(bot + " will follow you."), false); }
                else p.sendMessage(Text.literal(bot + ": I don't trust you enough yet. (" + trust + "/5)"), false);
            }
            case "coach" -> coach(p, prof, action.substring(6));
            case "buy" -> Shop.buy(p, prof, Integer.parseInt(action.substring(4)));
            case "join" -> joinFaction(p, action.substring(5));
        }
        AstralMod.sync(p);
    }

    private static void coach(ServerPlayerEntity p, PlayerProfile prof, String regionName) {
        Region dest; try { dest = Region.valueOf(regionName); } catch (Exception e) { return; }
        int need = Prices.fare(dest);
        if (!Prices.pay(p, need)) { p.sendMessage(Text.literal("Coachman: That's " + need + " coins' worth of ingots. You're short."), false); return; }
        ServerWorld w = p.getEntityWorld();
        p.teleport(w, dest.cx + 0.5, MapBuilder.Y + 1, dest.cz + 0.5, Set.of(), 0f, 0f, true);
        prof.achievements.add("Rode the stagecoach to " + dest.title);
    }

    public static void joinFaction(ServerPlayerEntity p, String name) {
        PlayerProfile prof = ProfileStore.get(p.getUuid());
        if (prof.chosenFaction) { p.sendMessage(Text.literal("You've already chosen a path."), false); return; }
        ServerWorld w = p.getEntityWorld();
        switch (name.toLowerCase()) {
            case "lawmakers" -> { prof.faction = Faction.LAWMAKERS; prof.lawRank = 0; Gear.lawmaker(w, p, 0); }
            case "resistance" -> { prof.faction = Faction.RESISTANCE; Gear.resistance(w, p); }
            case "free" -> prof.faction = Faction.FREE;
            default -> { p.sendMessage(Text.literal("Unknown faction."), false); return; }
        }
        prof.chosenFaction = true;
        prof.achievements.add("Joined: " + prof.faction);
        p.sendMessage(Text.literal("You are now: " + prof.faction), false);
    }
}
