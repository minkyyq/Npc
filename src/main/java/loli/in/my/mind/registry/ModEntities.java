package loli.in.my.mind.registry;

import loli.in.my.mind.NpcStoryMod;
import loli.in.my.mind.entity.StoryNpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NpcStoryMod.MOD_ID);

    public static final RegistryObject<EntityType<StoryNpcEntity>> STORY_NPC = ENTITY_TYPES.register(
            "story_npc",
            () -> EntityType.Builder.of(StoryNpcEntity::new, MobCategory.MISC)
                    .sized(0.6F, 2.0F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(NpcStoryMod.MOD_ID + ":story_npc")
    );

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(STORY_NPC.get(), StoryNpcEntity.createAttributes().build());
    }
}
