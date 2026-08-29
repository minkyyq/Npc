package loli.in.my.mind.client.camera;

import loli.in.my.mind.network.CameraCuePacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;

public final class ClientCameraController {
    private static CameraState state;
    private static int ageTicks;

    private ClientCameraController() {
    }

    public static void play(CameraCuePacket cue) {
        if (cue.mode() == CameraCuePacket.Mode.CLEAR) {
            clear();
            return;
        }

        Vec3 focus = new Vec3(cue.focusX(), cue.focusY(), cue.focusZ());
        Vec3 widePosition = focus.add(new Vec3(0.0D, 0.7D, 5.5D));

        if (cue.mode() == CameraCuePacket.Mode.STATIC) {
            state = new CameraState(widePosition, widePosition, focus, focus, 1, false);
            ageTicks = 1;
            return;
        }

        boolean movingToCloseShot = cue.mode() == CameraCuePacket.Mode.DOLLY;
        Vec3 startPosition = state == null
                ? (movingToCloseShot ? widePosition : focus.add(new Vec3(0.0D, 0.65D, 3.0D)))
                : currentPosition(state, 0.0D);
        Vec3 startFocus = state == null ? focus : currentFocus(state, 0.0D);
        Vec3 endPosition = movingToCloseShot
                ? focus.add(new Vec3(0.0D, 0.65D, 3.0D))
                : startPosition;
        state = new CameraState(
                startPosition,
                endPosition,
                startFocus,
                focus,
                Math.max(1, cue.durationTicks()),
                cue.mode() == CameraCuePacket.Mode.TURN_AND_CLEAR
        );
        ageTicks = 0;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || state == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clear();
            return;
        }

        if (ageTicks < state.durationTicks()) {
            ageTicks++;
        } else if (state.clearWhenDone()) {
            clear();
        }
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CameraState currentState = state;
        if (currentState == null) {
            return;
        }

        Vec3 position = currentPosition(currentState, event.getPartialTick());
        Vec3 focus = currentFocus(currentState, event.getPartialTick());
        Camera camera = event.getCamera();
        camera.setPosition(position.x, position.y, position.z);
        event.setYaw(lookYaw(position, focus));
        event.setPitch(lookPitch(position, focus));
        event.setRoll(0.0F);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (state != null) {
            event.setFOV(55.0D);
        }
    }

    private static Vec3 currentPosition(CameraState currentState, double partialTick) {
        return currentState.startPosition().lerp(
                currentState.endPosition(),
                easedProgress(currentState, partialTick)
        );
    }

    private static Vec3 currentFocus(CameraState currentState, double partialTick) {
        return currentState.startFocus().lerp(
                currentState.endFocus(),
                easedProgress(currentState, partialTick)
        );
    }

    private static double easedProgress(CameraState currentState, double partialTick) {
        double progress = Mth.clamp(
                (ageTicks + partialTick) / currentState.durationTicks(),
                0.0D,
                1.0D
        );
        return progress * progress * (3.0D - 2.0D * progress);
    }

    private static float lookYaw(Vec3 cameraPosition, Vec3 target) {
        Vec3 direction = target.subtract(cameraPosition);
        return (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
    }

    private static float lookPitch(Vec3 cameraPosition, Vec3 target) {
        Vec3 direction = target.subtract(cameraPosition);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return (float) -Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
    }

    private static void clear() {
        state = null;
        ageTicks = 0;
    }

    private record CameraState(
            Vec3 startPosition,
            Vec3 endPosition,
            Vec3 startFocus,
            Vec3 endFocus,
            int durationTicks,
            boolean clearWhenDone
    ) {
    }
}
