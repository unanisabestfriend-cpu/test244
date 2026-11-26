package com.astral.client;

import com.astral.net.Payloads;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.*;

/** Dark panel, category list on the left with green counts, entries on the right - modelled on your reference. */
public class JournalScreen extends Screen {
    private static final int BG = 0xF0101018, GREEN = 0xFF55FF55, ACCENT = 0xFF3A86C8, GOLD = 0xFFFFC040, DIM = 0xFF8A8A99, SEL = 0xFF2A2A33;
    private int selected = 2;
    private final List<String> cats = List.of("Progress", "Collections", "Your story", "Achievements", "Growth", "World", "Quest log", "Stats");

    public JournalScreen() { super(Text.literal("Journal")); }

    private int count(String c) {
        var s = ClientData.sync;
        return switch (c) {
            case "Achievements" -> s.achievements().size();
            case "World" -> s.places().size();
            case "Progress" -> Math.min(44, s.achievements().size() + s.places().size());
            case "Your story" -> Math.min(3, 1 + s.places().size());
            default -> 0;
        };
    }

    @Override public void render(DrawContext g, int mx, int my, float dt) {
        g.fill(0, 0, width, height, BG);
        g.fill(0, 0, width, 3, GREEN);
        g.drawTextWithShadow(textRenderer, "JOURNAL", 12, 10, GREEN);
        int done = Math.min(44, ClientData.sync.achievements().size() + ClientData.sync.places().size());
        String prog = done + " / 44 done";
        g.drawTextWithShadow(textRenderer, prog, width - 12 - textRenderer.getWidth(prog), 10, 0xFFCCCCCC);
        g.fill(12, 24, width - 12, 27, 0xFF222230); g.fill(12, 24, 12 + (width - 24) * done / 44, 27, GREEN);
        g.drawTextWithShadow(textRenderer, "NEXT: Explore the Capital District", 12, 34, GOLD);

        int y = 56;
        for (int i = 0; i < cats.size(); i++, y += 14) {
            if (i == selected) g.fill(10, y - 3, 120, y + 11, SEL);
            g.drawTextWithShadow(textRenderer, cats.get(i), 14, y, i == selected ? 0xFFFFFFFF : DIM);
            String n = String.valueOf(count(cats.get(i)));
            g.drawTextWithShadow(textRenderer, n, 114 - textRenderer.getWidth(n), y, count(cats.get(i)) > 0 ? GREEN : DIM);
        }
        List<String> entries = switch (cats.get(selected)) {
            case "Achievements" -> ClientData.sync.achievements();
            case "World" -> ClientData.sync.places();
            case "Your story" -> List.of("The story of " + (client != null ? client.getSession().getUsername() : "you"),
                "Honor " + ClientData.sync.honor() + " - " + ClientData.sync.faction(), "Played " + ClientData.sync.playMinutes() + " min");
            default -> List.of();
        };
        int ey = 56;
        for (String e : entries) {
            g.fill(132, ey, 134, ey + 24, ACCENT);
            g.drawTextWithShadow(textRenderer, e, 140, ey + 8, 0xFFFFFFFF);
            ey += 30;
        }
        super.render(g, mx, my, dt);
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (mx > 10 && mx < 120) { int i = (int) ((my - 53) / 14); if (i >= 0 && i < cats.size()) selected = i; }
        return super.mouseClicked(click, doubled);
    }
}
