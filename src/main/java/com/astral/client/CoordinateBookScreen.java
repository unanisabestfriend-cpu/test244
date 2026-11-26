package com.astral.client;

import com.astral.server.Region;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.*;

/** Press J. Tabs like the reference: PLACES, BASES, JOBS, REPUTATION, RUMOURS, BOSSES, PEOPLE, OPTIONS, GUIDE. */
public class CoordinateBookScreen extends Screen {
    private static final List<String> TABS = List.of("PLACES", "REPUTATION", "ACHIEVEMENTS", "PEOPLE", "GUIDE");
    private int tab = 0;
    public CoordinateBookScreen() { super(Text.literal("Coordinate Book")); }

    @Override public void render(DrawContext g, int mx, int my, float dt) {
        g.fill(0, 0, width, height, 0xF00C0C14);
        g.fill(0, 0, width, 3, 0xFFFFC040);
        g.drawTextWithShadow(textRenderer, "COORDINATE BOOK", 14, 12, 0xFFFFFFFF);
        String rank = ClientData.sync.faction().equals("FREE") ? "Free Player" : ClientData.sync.faction();
        g.drawTextWithShadow(textRenderer, rank, width - 14 - textRenderer.getWidth(rank), 12, 0xFFCCCCCC);
        int x = 14;
        for (int i = 0; i < TABS.size(); i++) {
            g.drawTextWithShadow(textRenderer, TABS.get(i), x, 32, i == tab ? 0xFFFFC040 : 0xFF8A8A99);
            if (i == tab) g.fill(x, 43, x + textRenderer.getWidth(TABS.get(i)), 45, 0xFFFFC040);
            x += textRenderer.getWidth(TABS.get(i)) + 16;
        }
        int y = 62;
        switch (TABS.get(tab)) {
            case "PLACES" -> { for (Region r : Region.values()) { boolean found = ClientData.sync.places().contains(r.title);
                g.drawTextWithShadow(textRenderer, found ? r.title + "  (" + r.cx + ", " + r.cz + ")" : "???", 14, y, found ? 0xFFFFFFFF : 0xFF555566); y += 14; } }
            case "REPUTATION" -> {
                g.drawTextWithShadow(textRenderer, "Honor: " + ClientData.sync.honor() + (ClientData.sync.honor() <= -40 ? "  - WANTED by the Lawmakers" : ""), 14, y, 0xFFFFFFFF);
                g.drawTextWithShadow(textRenderer, "Faction: " + ClientData.sync.faction() + "  Law rank: " + ClientData.sync.lawRank(), 14, y + 14, 0xFFFFFFFF);
                boolean can = ClientData.sync.playMinutes() >= 10;
                g.drawTextWithShadow(textRenderer, can ? "You may run for king (/astral run)" : "Play 10 min to run for king (" + ClientData.sync.playMinutes() + "/10)", 14, y + 28, can ? 0xFF55FF55 : 0xFFAAAAAA); }
            case "ACHIEVEMENTS" -> { for (String a : ClientData.sync.achievements()) { g.drawTextWithShadow(textRenderer, a, 14, y, 0xFFFFFFFF); y += 12; } }
            case "PEOPLE" -> g.drawTextWithShadow(textRenderer, "Sneak + right-click anyone to talk.", 14, y, 0xFFAAAAAA);
            default -> g.drawTextWithShadow(textRenderer, "Pick a faction, earn honor, avoid the Lawmakers, become King.", 14, y, 0xFFAAAAAA);
        }
        super.render(g, mx, my, dt);
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) {
        if (c.y() > 28 && c.y() < 46) { int x = 14; for (int i = 0; i < TABS.size(); i++) { int w = textRenderer.getWidth(TABS.get(i)); if (c.x() >= x && c.x() <= x + w) tab = i; x += w + 16; } }
        return super.mouseClicked(c, d);
    }
}
