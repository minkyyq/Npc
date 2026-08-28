package loli.in.my.mind.network;

import loli.in.my.mind.client.camera.ClientCameraController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CameraCuePacket(
        Mode mode,
        double focusX,
        double focusY,
        double focusZ,
        int durationTicks
) {
    public static CameraCuePacket staticShot(double focusX, double focusY, double focusZ) {
        return new CameraCuePacket(Mode.STATIC, focusX, focusY, focusZ, 0);
    }

    public static CameraCuePacket dolly(double focusX, double focusY, double focusZ, int durationTicks) {
        return new CameraCuePacket(Mode.DOLLY, focusX, focusY, focusZ, durationTicks);
    }

    public static CameraCuePacket turnTo(double focusX, double focusY, double focusZ, int durationTicks) {
        return new CameraCuePacket(Mode.TURN, focusX, focusY, focusZ, durationTicks);
    }

    public static CameraCuePacket turnToAndClear(
            double focusX,
            double focusY,
            double focusZ,
            int durationTicks
    ) {
        return new CameraCuePacket(Mode.TURN_AND_CLEAR, focusX, focusY, focusZ, durationTicks);
    }

    public static CameraCuePacket clear() {
        return new CameraCuePacket(Mode.CLEAR, 0.0D, 0.0D, 0.0D, 0);
    }

    public static void encode(CameraCuePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.mode());
        buffer.writeDouble(packet.focusX());
        buffer.writeDouble(packet.focusY());
        buffer.writeDouble(packet.focusZ());
        buffer.writeVarInt(packet.durationTicks());
    }

    public static CameraCuePacket decode(FriendlyByteBuf buffer) {
        return new CameraCuePacket(
                buffer.readEnum(Mode.class),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt()
        );
    }

    public static void handle(CameraCuePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCameraController.play(packet)
        ));
        context.setPacketHandled(true);
    }

    public enum Mode {
        STATIC,
        DOLLY,
        CLEAR,
        TURN,
        TURN_AND_CLEAR
    }
}
