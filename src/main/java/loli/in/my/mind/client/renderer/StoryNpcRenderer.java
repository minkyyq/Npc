package loli.in.my.mind.client.renderer;

import loli.in.my.mind.client.model.StoryNpcModel;
import loli.in.my.mind.entity.StoryNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class StoryNpcRenderer extends GeoEntityRenderer<StoryNpcEntity> {
    public StoryNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new StoryNpcModel());
        shadowRadius = 0.45F;
    }
}
