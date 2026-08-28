package loli.in.my.mind.network;

import loli.in.my.mind.NpcStoryMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetworking {
    private static SimpleChannel channel;

    private ModNetworking() {
    }

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(NpcStoryMod.MOD_ID, "story"),
                () -> "4",
                "4"::equals,
                "4"::equals
        );

        channel.registerMessage(
                0,
                AdvanceStoryPacket.class,
                AdvanceStoryPacket::encode,
                AdvanceStoryPacket::decode,
                AdvanceStoryPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        channel.registerMessage(
                1,
                CameraCuePacket.class,
                CameraCuePacket::encode,
                CameraCuePacket::decode,
                CameraCuePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendAdvanceRequest() {
        if (channel != null) {
            channel.sendToServer(new AdvanceStoryPacket());
        }
    }

    public static void sendCameraCue(ServerPlayer player, CameraCuePacket cue) {
        if (channel != null) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), cue);
        }
    }
}
