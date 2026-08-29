package loli.in.my.mind.client;

import loli.in.my.mind.client.camera.ClientCameraController;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientBootstrap {
    private static boolean registered;

    private ClientBootstrap() {
    }

    public static void register(IEventBus modBus) {
        if (registered) {
            return;
        }

        registered = true;
        modBus.addListener(ClientModEvents::registerKeyMappings);
        modBus.addListener(ClientModEvents::registerRenderers);
        MinecraftForge.EVENT_BUS.addListener(ClientInputHandler::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientCameraController::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientCameraController::onCameraAngles);
        MinecraftForge.EVENT_BUS.addListener(ClientCameraController::onComputeFov);
    }
}
