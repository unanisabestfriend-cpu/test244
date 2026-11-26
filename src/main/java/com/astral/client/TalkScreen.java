package com.astral.client;

import com.astral.net.Payloads;
import com.astral.server.Region;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TalkScreen extends Screen {
    private final Payloads.OpenTalk bot;
    public TalkScreen(Payloads.OpenTalk b) { super(Text.literal(b.botName())); bot = b; }

    private void add(int row, String label, String action, boolean close) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            ClientPlayNetworking.send(new Payloads.TalkAction(bot.botName(), action));
            if (close) close();
        }).dimensions(width / 2 - 80, height / 2 - 60 + row * 22, 160, 20).build());
    }

    @Override protected void init() {
        if (bot.role().equals("STAGECOACH")) {
            int r = 0;
            for (Region reg : new Region[]{Region.CAPITAL_DISTRICT, Region.NEW_MERCHANT_CITY, Region.COUNCIL_OF_KINGS})
                add(r++, "Ride to " + reg.title, "coach_" + reg.name(), true);
        } else if (bot.role().equals("MERCHANT")) {
            for (int i = 0; i < com.astral.server.Shop.GOODS.size(); i++) {
                var g = com.astral.server.Shop.GOODS.get(i);
                add(i, g.label() + " - " + g.price() + " coins", "buy_" + i, false);
            }
        } else {
            add(0, "Talk", "chat", false);
            add(1, "Ask about their interests", "ask", false);
            add(2, "Ask to be buddies", "buddy", true);
            add(3, "Insult", "insult", true);
        }
        add(bot.role().equals("MERCHANT") ? com.astral.server.Shop.GOODS.size() : 4, "Leave", "leave", true);
    }

    @Override public void render(DrawContext g, int mx, int my, float dt) {
        g.fill(0, 0, width, height, 0xC0000000);
        g.drawCenteredTextWithShadow(textRenderer, bot.botName() + "  (" + bot.role() + ")   Trust: " + bot.trust(), width / 2, height / 2 - 70, 0xFFFFC040);
        super.render(g, mx, my, dt);
    }
}
