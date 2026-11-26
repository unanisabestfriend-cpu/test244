package com.astral.server;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** Value of each valuable item, in "coins". Used by stagecoaches and merchants. */
public class Prices {
    public static final Map<Item, Integer> VALUE = Map.of(
        Items.COPPER_INGOT, 1, Items.IRON_INGOT, 3, Items.GOLD_INGOT, 6, Items.EMERALD, 8,
        Items.DIAMOND, 20, Items.NETHERITE_INGOT, 100);
    public static int fare(Region dest) {
        return switch (dest) { case NEW_MERCHANT_CITY -> 15; case COUNCIL_OF_KINGS -> 20; case CAPITAL_DISTRICT -> 10; default -> 12; };
    }

    public static int wealth(ServerPlayerEntity p) {
        int have = 0;
        for (ItemStack s : p.getInventory().getMainStacks()) have += VALUE.getOrDefault(s.getItem(), 0) * s.getCount();
        return have;
    }
    /** Takes `need` coins' worth of valuables (may overpay by a few coins). */
    public static boolean pay(ServerPlayerEntity p, int need) {
        if (wealth(p) < need) return false;
        int left = need;
        // spend cheapest items first
        for (int pass = 0; pass < 2 && left > 0; pass++)
            for (ItemStack s : p.getInventory().getMainStacks()) {
                int v = VALUE.getOrDefault(s.getItem(), 0);
                if (v == 0 || (pass == 0 && v > left)) continue;
                while (left > 0 && !s.isEmpty()) { s.decrement(1); left -= v; }
            }
        return true;
    }
}
