package com.astral.server;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import java.util.Random;

public class Gear {
    private static final Random R = new Random();

    private static void ench(ServerWorld w, ItemStack s, RegistryKey<Enchantment> k, int lvl) {
        var reg = w.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        s.addEnchantment(reg.getOrThrow(k), lvl);
    }

    private static void armor(ServerWorld w, LivingEntity e, boolean netherite, int prot, int unb, boolean mend, boolean max) {
        Item[] items = netherite
            ? new Item[]{Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS}
            : new Item[]{Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            ItemStack s = new ItemStack(items[i]);
            ench(w, s, Enchantments.PROTECTION, prot);
            if (unb > 0) ench(w, s, Enchantments.UNBREAKING, unb);
            if (mend) ench(w, s, Enchantments.MENDING, 1);
            if (max) { // everything except Thorns and the other protection types
                if (i == 0) { ench(w, s, Enchantments.RESPIRATION, 3); ench(w, s, Enchantments.AQUA_AFFINITY, 1); }
                if (i == 2) ench(w, s, Enchantments.SWIFT_SNEAK, 3);
                if (i == 3) { ench(w, s, Enchantments.FEATHER_FALLING, 4); ench(w, s, Enchantments.DEPTH_STRIDER, 3); ench(w, s, Enchantments.SOUL_SPEED, 3); }
            }
            e.equipStack(slots[i], s);
        }
    }

    /** Lawmaker armor by rank 0 (unranked) .. 4 (leader). */
    public static void lawmaker(ServerWorld w, LivingEntity e, int rank) {
        switch (rank) {
            case 0 -> armor(w, e, false, 2, 0, false, false);
            case 1 -> armor(w, e, false, 4, 2, false, false);
            case 2 -> armor(w, e, true, 4, 3, false, false);
            case 3 -> armor(w, e, true, 4, 3, true, false);
            default -> armor(w, e, true, 4, 3, true, true);
        }
        ItemStack sword = new ItemStack(rank >= 2 ? Items.NETHERITE_SWORD : Items.DIAMOND_SWORD);
        ench(w, sword, Enchantments.SHARPNESS, 3 + Math.min(2, rank));
        e.equipStack(EquipmentSlot.MAINHAND, sword);
    }

    /** Parrot's Resistance kit. */
    public static void resistance(ServerWorld w, LivingEntity e) {
        armor(w, e, true, 2, 0, false, false);
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD); ench(w, sword, Enchantments.SHARPNESS, 5);
        ItemStack axe = new ItemStack(Items.NETHERITE_AXE); ench(w, axe, Enchantments.EFFICIENCY, 5); ench(w, axe, Enchantments.SHARPNESS, 5);
        ItemStack pick = new ItemStack(Items.NETHERITE_PICKAXE); ench(w, pick, Enchantments.EFFICIENCY, 5);
        if (e instanceof PlayerEntity p) {
            p.getInventory().insertStack(sword);
            p.getInventory().insertStack(axe);
            p.getInventory().insertStack(pick);
        } else {
            e.equipStack(EquipmentSlot.MAINHAND, sword);
        }
    }

    /** Townsfolk: random gear, or nothing at all. */
    public static void civilian(ServerWorld w, LivingEntity e) {
        int roll = R.nextInt(10);
        if (roll < 4) return;
        if (roll < 7) { e.equipStack(EquipmentSlot.MAINHAND, new ItemStack(R.nextBoolean() ? Items.STICK : Items.WOODEN_AXE)); return; }
        e.equipStack(EquipmentSlot.CHEST, new ItemStack(R.nextBoolean() ? Items.LEATHER_CHESTPLATE : Items.IRON_CHESTPLATE));
        if (R.nextBoolean()) e.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }
}
