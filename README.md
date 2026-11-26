# Astral (Fabric 1.21.11)
Requires on the server: Fabric API + HeroBot (modrinth.com/mod/herobot). Remove Carpet (incompatible with HeroBot).
Build: push to GitHub, Actions builds it (Actions tab -> run -> Artifacts).
Play: first join builds the map (~10 s). Sneak + right-click NPCs to talk. J = Coordinate Book. Green book = Journal.
Commands: /astral faction <lawmakers|resistance|free>, /astral run, ops: /astral build, /astral war start|stop.
Bots: spawned with HeroBot's /playerspawn and driven by /player commands (see server/BotBackend.java, BotAI.java).
