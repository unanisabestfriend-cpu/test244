package com.astral.client;

import com.astral.AstralMod;
import com.astral.net.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AstralClient implements ClientModInitializer {
    private static KeyBinding openBook;

    @Override public void onInitializeClient() {
        // 1.21.9+ key categories are objects, not strings.
        openBook = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.astral.coordinate_book",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyBinding.Category.create(Identifier.of("astral", "astral"))));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openBook.wasPressed()) if (mc.currentScreen == null) mc.setScreen(new CoordinateBookScreen());
        });
        AstralMod.openJournalClient = () -> MinecraftClient.getInstance().setScreen(new JournalScreen());
        ClientPlayNetworking.registerGlobalReceiver(Payloads.Sync.ID, (p, ctx) -> ClientData.sync = p);
        ClientPlayNetworking.registerGlobalReceiver(Payloads.OpenTalk.ID, (p, ctx) ->
            ctx.client().execute(() -> ctx.client().setScreen(new TalkScreen(p))));
    }
}
