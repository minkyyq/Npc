package loli.in.my.mind.client;

import com.mojang.blaze3d.platform.InputConstants;
import loli.in.my.mind.network.ModNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientInputHandler {
    public static final Lazy<KeyMapping> ADVANCE_STORY = Lazy.of(() ->
            new KeyMapping(
                    "key.npcstory.advance",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    "key.categories.npcstory"
            )
    );

    private ClientInputHandler() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END
                || minecraft.player == null
                || minecraft.screen != null) {
            return;
        }

        while (ADVANCE_STORY.get().consumeClick()) {
            ModNetworking.sendAdvanceRequest();
        }
    }
}
