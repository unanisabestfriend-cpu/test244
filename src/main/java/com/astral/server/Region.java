package com.astral.server;

import net.minecraft.util.math.BlockPos;

/** The 256x256 Astral map, centred on 0,0 (x,z in -128..127). */
public enum Region {
    SPAWN("Spawn", 0, 0, 20, 0),
    CAPITAL_DISTRICT("Capital District", 0, -70, 30, 1),      // north, reached by obsidian bridge
    NEW_MERCHANT_CITY("New Merchant City", 80, 60, 38, 2),
    COUNCIL_OF_KINGS("Council of Kings", -85, 55, 38, 3),
    KINGDOM_CINDERCREST("Cindercrest", -90, -70, 28, 4),
    KINGDOM_VALEHOLD("Valehold", 95, -60, 28, 5);

    public final String title; public final int cx, cz, radius, id;
    Region(String t, int x, int z, int r, int id) { title = t; cx = x; cz = z; radius = r; this.id = id; }
    public BlockPos center(int y) { return new BlockPos(cx, y, cz); }
    public boolean contains(BlockPos p) { long dx = p.getX() - cx, dz = p.getZ() - cz; return dx*dx + dz*dz <= (long) radius * radius; }
    public static Region at(BlockPos p) { for (Region r : values()) if (r.contains(p)) return r; return null; }
}
