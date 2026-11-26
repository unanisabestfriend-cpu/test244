package com.astral.server;

import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.List;

/** New Merchant City traders. Pay with ores/ingots (see Prices). */
public class Shop {
    public record Good(String label, Item item, int count, int price) {}
    public static final List<Good> GOODS = List.of(
        new Good("Bread x8", Items.BREAD, 8, 2),
        new Good("Golden Apple", Items.GOLDEN_APPLE, 1, 12),
        new Good("Iron Sword", Items.IRON_SWORD, 1, 9),
        new Good("Bow + Arrows", Items.BOW, 1, 8),
        new Good("Shield", Items.SHIELD, 1, 6),
        new Good("Diamond Pickaxe", Items.DIAMOND_PICKAXE, 1, 60),
        new Good("Diamond Chestplate", Items.DIAMOND_CHESTPLATE, 1, 100),
        new Good("Ender Pearl", Items.ENDER_PEARL, 1, 10),
        new Good("Elytra", Items.ELYTRA, 1, 400));

    public static void buy(ServerPlayerEntity p, PlayerProfile prof, int idx) {
        if (idx < 0 || idx >= GOODS.size()) return;
        Good g = GOODS.get(idx);
        // Lawmakers' enemies pay double, like a black-market surcharge
        int price = prof.wanted() ? g.price() * 2 : g.price();
        if (!Prices.pay(p, price)) { p.sendMessage(Text.literal("Trader: That's " + price + " coins' worth. Come back with more ore."), false); return; }
        p.giveItemStack(new ItemStack(g.item(), g.count()));
        if (g.item() == Items.BOW) p.giveItemStack(new ItemStack(Items.ARROW, 16));
        prof.achievements.add("First purchase");
        p.sendMessage(Text.literal("Trader: Pleasure doing business. (" + g.label() + ")"), false);
    }
}
