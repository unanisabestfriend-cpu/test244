package com.astral.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class Payloads {
    /** S2C: open the Talk menu for this bot. */
    public record OpenTalk(String botName, String role, int trust) implements CustomPayload {
        public static final Id<OpenTalk> ID = new Id<>(Identifier.of("astral", "open_talk"));
        public static final PacketCodec<RegistryByteBuf, OpenTalk> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenTalk::botName, PacketCodecs.STRING, OpenTalk::role, PacketCodecs.INTEGER, OpenTalk::trust, OpenTalk::new);
        public Id<? extends CustomPayload> getId() { return ID; }
    }
    /** C2S: a talk-menu action. action = insult | ask | buddy | chat | coach_<REGION> | join_<FACTION> */
    public record TalkAction(String botName, String action) implements CustomPayload {
        public static final Id<TalkAction> ID = new Id<>(Identifier.of("astral", "talk_action"));
        public static final PacketCodec<RegistryByteBuf, TalkAction> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TalkAction::botName, PacketCodecs.STRING, TalkAction::action, TalkAction::new);
        public Id<? extends CustomPayload> getId() { return ID; }
    }
    /** S2C: profile snapshot for the Journal / Coordinate Book. */
    public record Sync(int honor, int lawRank, String faction, int playMinutes, java.util.List<String> achievements, java.util.List<String> places) implements CustomPayload {
        public static final Id<Sync> ID = new Id<>(Identifier.of("astral", "sync"));
        public static final PacketCodec<RegistryByteBuf, Sync> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, Sync::honor, PacketCodecs.INTEGER, Sync::lawRank, PacketCodecs.STRING, Sync::faction,
            PacketCodecs.INTEGER, Sync::playMinutes,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), Sync::achievements,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), Sync::places, Sync::new);
        public Id<? extends CustomPayload> getId() { return ID; }
    }
}
