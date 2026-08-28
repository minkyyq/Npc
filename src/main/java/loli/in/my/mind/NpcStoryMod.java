package loli.in.my.mind;

import loli.in.my.mind.client.ClientBootstrap;
import loli.in.my.mind.network.ModNetworking;
import loli.in.my.mind.registry.ModEntities;
import loli.in.my.mind.story.StoryDirector;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NpcStoryMod.MOD_ID)
public final class NpcStoryMod {
    public static final String MOD_ID = "npcstory";

    public NpcStoryMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModEntities.register(modBus);
        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(StoryDirector.INSTANCE);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBootstrap.register(modBus));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }
}
