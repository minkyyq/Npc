package loli.in.my.mind.client.model;

import loli.in.my.mind.NpcStoryMod;
import loli.in.my.mind.entity.StoryNpcEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class StoryNpcModel extends GeoModel<StoryNpcEntity> {
    @Override
    public ResourceLocation getModelResource(StoryNpcEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(NpcStoryMod.MOD_ID, "geo/story_npc.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StoryNpcEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(NpcStoryMod.MOD_ID, "textures/entity/story_npc.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StoryNpcEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(NpcStoryMod.MOD_ID, "animations/story_npc.animation.json");
    }
}
