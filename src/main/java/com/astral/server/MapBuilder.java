package com.astral.server;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.*;

/**
 * Builds the 256x256 Astral map once. Run from /astral build (or auto on first load).
 * Cities are procedural: stone-brick plazas, grid streets, houses with varied heights.
 * For hand-made cities, swap buildCity() for a structure-template placement.
 */
public class MapBuilder {
    public static final int Y = 64;

    private static boolean running = false;
    private static int step = 0;
    private static ServerWorld world;
    private static final List<Region> CITIES = new ArrayList<>();

    public static boolean isBuilt(ServerWorld w) { return w.getBlockState(new BlockPos(0, Y, 0)).isOf(Blocks.SMOOTH_STONE); }
    public static boolean isRunning() { return running; }

    /** Starts a gradual build (a few columns per tick) so the server never freezes. */
    public static void start(ServerWorld w, boolean force) {
        if (running || (!force && isBuilt(w))) return;
        running = true; step = 0; world = w;
        CITIES.clear();
        for (Region r : Region.values()) if (r != Region.SPAWN) CITIES.add(r);
    }

    public static void tick() {
        if (!running) return;
        if (step < 128) {                       // terrain: 2 columns of 256 per tick
            for (int dx = 0; dx < 2; dx++) {
                int x = -128 + step * 2 + dx;
                for (int z = -128; z < 128; z++) {
                    boolean edge = x == -128 || x == 127 || z == -128 || z == 127;
                    world.setBlockState(new BlockPos(x, Y - 1, z), Blocks.DIRT.getDefaultState(), 2);
                    world.setBlockState(new BlockPos(x, Y, z), Blocks.GRASS_BLOCK.getDefaultState(), 2);
                    if (edge) for (int y = 1; y <= 12; y++) world.setBlockState(new BlockPos(x, Y + y, z), Blocks.BEDROCK.getDefaultState(), 2);
                }
            }
        } else {
            int r = step - 128;
            if (r < CITIES.size()) buildCity(world, CITIES.get(r));
            else if (r == CITIES.size()) buildObsidianBridge(world);
            else { buildSpawn(world); running = false; }   // spawn plaza is the "finished" marker
        }
        step++;
    }

    private static void buildCity(ServerWorld w, Region r) {
        int big = r.radius;
        for (int dx = -big; dx <= big; dx++) for (int dz = -big; dz <= big; dz++) {
            if (dx*dx + dz*dz > big*big) continue;
            BlockPos g = new BlockPos(r.cx + dx, Y, r.cz + dz);
            boolean street = Math.floorMod(dx, 12) < 3 || Math.floorMod(dz, 12) < 3;
            w.setBlockState(g, (street ? Blocks.STONE_BRICKS : Blocks.COBBLESTONE).getDefaultState(), 2);
            // building plots on the non-street cells
            if (!street && Math.floorMod(dx, 12) == 5 && Math.floorMod(dz, 12) == 5) house(w, g.up(), 5 + Math.floorMod(dx * 31 + dz * 17, 10), r);
        }
        // central tower / landmark
        for (int y = 1; y <= 28; y++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            if (Math.abs(dx) == 2 || Math.abs(dz) == 2) w.setBlockState(new BlockPos(r.cx + dx, Y + y, r.cz + dz), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
    }

    private static void house(ServerWorld w, BlockPos o, int h, Region r) {
        BlockState wall = (r == Region.NEW_MERCHANT_CITY ? Blocks.BIRCH_PLANKS : Blocks.SPRUCE_PLANKS).getDefaultState();
        for (int y = 0; y < h; y++) for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 6; dz++) {
            boolean shell = dx == 0 || dz == 0 || dx == 5 || dz == 5;
            BlockPos p = o.add(dx, y, dz);
            if (shell && !(y == 1 && dx == 2 && dz == 0)) w.setBlockState(p, (y % 4 == 2 && (dx + dz) % 2 == 1) ? Blocks.GLASS_PANE.getDefaultState() : wall, 2);
            else w.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
        }
        for (int dx = 0; dx < 6; dx++) for (int dz = 0; dz < 6; dz++) w.setBlockState(o.add(dx, h, dz), Blocks.STONE_BRICK_SLAB.getDefaultState(), 2);
    }

    private static void buildSpawn(ServerWorld w) {
        for (int dx = -20; dx <= 20; dx++) for (int dz = -20; dz <= 20; dz++)
            if (dx*dx + dz*dz <= 400) w.setBlockState(new BlockPos(dx, Y, dz), Blocks.SMOOTH_STONE.getDefaultState(), 2);
    }

    /** Obsidian bridge from spawn to Capital District. */
    private static void buildObsidianBridge(ServerWorld w) {
        for (int z = -20; z >= -(70 - 30); z--) for (int dx = -2; dx <= 2; dx++) {
            w.setBlockState(new BlockPos(dx, Y, z), Blocks.OBSIDIAN.getDefaultState(), 2);
            if (Math.abs(dx) == 2) w.setBlockState(new BlockPos(dx, Y + 1, z), Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);
        }
    }
}
