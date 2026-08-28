package loli.in.my.mind.network;

import loli.in.my.mind.story.StoryDirector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class AdvanceStoryPacket {
    public static void encode(AdvanceStoryPacket packet, FriendlyByteBuf buffer) {
    }

    public static AdvanceStoryPacket decode(FriendlyByteBuf buffer) {
        return new AdvanceStoryPacket();
    }

    public static void handle(AdvanceStoryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();

        if (sender != null) {
            context.enqueueWork(() -> StoryDirector.INSTANCE.advance(sender));
        }

        context.setPacketHandled(true);
    }
}
