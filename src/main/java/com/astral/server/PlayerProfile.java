package com.astral.server;

import java.util.*;

/** Per-player saved data. Serialised as JSON by ProfileStore. */
public class PlayerProfile {
    public Faction faction = Faction.FREE;
    public int honor = 0;            // -100..100, RDR2 style
    public int lawRank = 0;          // 0 = unranked .. 4 = leader
    public long playTicks = 0;       // for the "10 minutes to run for king" rule
    public boolean gotJournal = false;
    public boolean chosenFaction = false;
    public Set<String> achievements = new LinkedHashSet<>();
    public Set<String> placesFound = new LinkedHashSet<>();
    public Map<String, Integer> trust = new HashMap<>();

    public boolean canRunForKing() { return playTicks >= 10 * 60 * 20; }
    public boolean wanted() { return honor <= -40; }
    public void addHonor(int d) { honor = Math.max(-100, Math.min(100, honor + d)); }
}
