package com.astral;

import com.astral.net.Payloads;
import com.astral.server.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import java.util.*;

public class AstralMod implements ModInitializer {
    public static final String ID = "astral";
    public static final RegistryKey<World> ASTRAL = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(ID, "astral"));
    public static final RegistryKey<Item> JOURNAL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ID, "journal"));
    public static Item JOURNAL;
    /** Set by the client entrypoint so the item can open its screen. */
    private static final Set<UUID> WAITING = new HashSet<>();
    public static Runnable openJournalClient = () -> {};

    @Override public void onInitialize() {
        JOURNAL = Registry.register(Registries.ITEM, JOURNAL_KEY, new Item(new Item.Settings().registryKey(JOURNAL_KEY).maxCount(1)) {
            @Override public ActionResult use(World w, net.minecraft.entity.player.PlayerEntity p, net.minecraft.util.Hand h) {
                if (w.isClient()) openJournalClient.run();
                return ActionResult.SUCCESS;
            }
        });

        PayloadTypeRegistry.playS2C().register(Payloads.OpenTalk.ID, Payloads.OpenTalk.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.Sync.ID, Payloads.Sync.CODEC);
        PayloadTypeRegistry.playC2S().register(Payloads.TalkAction.ID, Payloads.TalkAction.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Payloads.TalkAction.ID, (pl, ctx) ->
            ctx.server().execute(() -> Talk.handle(ctx.player(), pl.botName(), pl.action())));

        ServerLifecycleEvents.SERVER_STARTED.register(s -> { ProfileStore.load(); War.load(); BotBackend.run(s, "herobot botLeaveOnDeath true"); });
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> { BotManager.clear(s); ProfileStore.save(); });
        ServerPlayConnectionEvents.JOIN.register((h, sender, server) -> onJoin(h.player, server));
        ServerTickEvents.END_SERVER_TICK.register(this::tick);

        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() || !player.isSneaking()) return ActionResult.PASS; // sneak + right-click to talk
            var info = BotManager.lookup(entity);
            if (info == null || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            int trust = ProfileStore.get(sp.getUuid()).trust.getOrDefault(info.name(), 0);
            ServerPlayNetworking.send(sp, new Payloads.OpenTalk(info.name(), info.role().name(), trust));
            return ActionResult.SUCCESS;
        });

        CommandRegistrationCallback.EVENT.register((d, reg, env) -> d.register(CommandManager.literal("astral")
            .then(CommandManager.literal("build").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).executes(c -> {
                MapBuilder.start(c.getSource().getServer().getWorld(ASTRAL), true);
                c.getSource().sendFeedback(() -> Text.literal("Building Astral over the next few seconds..."), true); return 1; }))
            .then(CommandManager.literal("faction").then(CommandManager.argument("f", StringArgumentType.word()).executes(c -> {
                var p = c.getSource().getPlayerOrThrow();
                Talk.joinFaction(p, StringArgumentType.getString(c, "f")); return 1; })))
            .then(CommandManager.literal("war").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                .then(CommandManager.literal("start").executes(c -> { War.begin(c.getSource().getServer()); return 1; }))
                .then(CommandManager.literal("stop").executes(c -> { War.end(c.getSource().getServer(), null); return 1; })))
            .then(CommandManager.literal("run").executes(c -> {
                var p = c.getSource().getPlayerOrThrow(); var prof = ProfileStore.get(p.getUuid());
                p.sendMessage(Text.literal(prof.canRunForKing()
                    ? "You are registered as a candidate at the Council of Kings."
                    : "You must play at least 10 minutes to run for king."), false);
                if (prof.canRunForKing()) prof.achievements.add("Candidate for King");
                return 1; }))));
    }

    private void onJoin(ServerPlayerEntity p, MinecraftServer server) {
        ServerWorld astral = server.getWorld(ASTRAL);
        if (astral == null) { p.sendMessage(Text.literal("Astral dimension missing - check the datapack."), false); return; }
        if (!MapBuilder.isBuilt(astral)) {
            MapBuilder.start(astral, false);
            WAITING.add(p.getUuid());
            p.sendMessage(Text.literal("Astral is being built - you'll be teleported in a few seconds..."), false);
        } else p.teleport(astral, 0.5, MapBuilder.Y + 1, 0.5, Set.of(), 0f, 0f, true);
        PlayerProfile prof = ProfileStore.get(p.getUuid());
        if (!prof.gotJournal) { p.giveItemStack(new ItemStack(JOURNAL)); prof.gotJournal = true; prof.achievements.add("Welcome to Astral"); }
        if (!prof.chosenFaction) p.sendMessage(Text.literal("Choose your path: /astral faction lawmakers | resistance | free"), false);
        sync(p);
    }

    private void tick(MinecraftServer server) {
        MapBuilder.tick();
        if (!WAITING.isEmpty()) {
            ServerWorld astral = server.getWorld(ASTRAL);
            if (astral != null && !MapBuilder.isRunning() && MapBuilder.isBuilt(astral)) {
                for (UUID id : WAITING) { ServerPlayerEntity pl = server.getPlayerManager().getPlayer(id);
                    if (pl != null) pl.teleport(astral, 0.5, MapBuilder.Y + 1, 0.5, Set.of(), 0f, 0f, true); }
                WAITING.clear();
            }
        }
        BotAI.tick(server);
        BotManager.tick(server);
        War.tick(server);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            PlayerProfile prof = ProfileStore.get(p.getUuid());
            prof.playTicks++;
            if (server.getTicks() % 100 == 0) {
                Region r = Region.at(p.getBlockPos());
                if (r != null && prof.placesFound.add(r.title)) { prof.achievements.add("Discovered " + r.title); p.sendMessage(Text.literal("Discovered: " + r.title), true); }
                if (prof.faction == Faction.LAWMAKERS && prof.lawRank < 4 && prof.playTicks % (36000) < 100) prof.lawRank++; // +1 rank / 30 min
                sync(p);
            }
        }
        if (server.getTicks() % 6000 == 0) ProfileStore.save();
    }

    public static void sync(ServerPlayerEntity p) {
        PlayerProfile f = ProfileStore.get(p.getUuid());
        ServerPlayNetworking.send(p, new Payloads.Sync(f.honor, f.lawRank, f.faction.name(), (int) (f.playTicks / 1200),
            new ArrayList<>(f.achievements), new ArrayList<>(f.placesFound)));
    }
}
