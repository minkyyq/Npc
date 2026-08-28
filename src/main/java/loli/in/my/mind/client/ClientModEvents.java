package loli.in.my.mind.client;

import loli.in.my.mind.client.renderer.StoryNpcRenderer;
import loli.in.my.mind.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public final class ClientModEvents {
    private ClientModEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientInputHandler.ADVANCE_STORY.get());
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STORY_NPC.get(), StoryNpcRenderer::new);
    }
}
