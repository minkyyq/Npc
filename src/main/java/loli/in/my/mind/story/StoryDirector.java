package loli.in.my.mind.story;

import loli.in.my.mind.entity.NpcAnimation;
import loli.in.my.mind.entity.NpcRole;
import loli.in.my.mind.entity.StoryNpcEntity;
import loli.in.my.mind.network.CameraCuePacket;
import loli.in.my.mind.network.ModNetworking;
import loli.in.my.mind.registry.ModEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class StoryDirector {
    public static final StoryDirector INSTANCE = new StoryDirector();
    private int dialogueCooldownTicks;

    private static final List<DialogueStep> DIALOGUE = List.of(
            new DialogueStep(Speaker.FIRST, NpcAnimation.MAIN, "Вот мы и встретились."),
            new DialogueStep(Speaker.SECOND, NpcAnimation.DISTRUST, "Я всё ещё не уверен, что это хорошая идея."),
            new DialogueStep(Speaker.FIRST, NpcAnimation.CORRECTION, "Уточню: мы просто проверим план и сразу вернёмся."),
            new DialogueStep(Speaker.FIRST, NpcAnimation.UNCORRECTION, "Теперь понятнее?"),
            new DialogueStep(Speaker.SECOND, NpcAnimation.RIGHT, "Да, теперь согласен."),
            new DialogueStep(Speaker.FIRST, NpcAnimation.MAIN, "Тогда начинаем.")
    );

    private StoryDirector() {
    }

    public void advance(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }

        StorySavedData data = StorySavedData.get(level);
        if (dialogueCooldownTicks > 0) {
            return;
        }

        switch (data.getPhase()) {
            case NOT_STARTED -> spawnScene(level, data, player);
            case SPAWNED -> startApproach(level, data, player);
            case APPROACHING -> {
            }
            case READY_FOR_DIALOGUE, DIALOGUE -> advanceDialogue(level, data, player);
            case COMPLETE -> restartScene(level, data, player);
        }
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) {
            return;
        }

        if (dialogueCooldownTicks > 0) {
            dialogueCooldownTicks--;
        }

        StorySavedData data = StorySavedData.get(level);
        if (data.getPhase() == StoryPhase.APPROACHING) {
            updateApproach(level, data);
            return;
        }

        if (data.getPhase() == StoryPhase.READY_FOR_DIALOGUE
                || data.getPhase() == StoryPhase.DIALOGUE
                || data.getPhase() == StoryPhase.COMPLETE) {
            NpcPair pair = resolvePair(level, data);
            if (pair != null) {
                faceEachOther(pair);
            }
        }
    }

    private void spawnScene(ServerLevel level, StorySavedData data, ServerPlayer viewer) {
        removeNearbyStoryNpcs(level);

        double groundY = sceneGroundY(level);
        StoryNpcEntity first = createNpc(level, NpcRole.FIRST, -4.0D, groundY, -90.0F);
        StoryNpcEntity second = createNpc(level, NpcRole.SECOND, 4.0D, groundY, 90.0F);

        if (!level.addFreshEntity(first) || !level.addFreshEntity(second)) {
            first.discard();
            second.discard();
            data.reset();
            ModNetworking.sendCameraCue(viewer, CameraCuePacket.clear());
            return;
        }

        data.setNpcIds(first.getUUID(), second.getUUID());
        data.setDialogueIndex(0);
        data.setPhase(StoryPhase.SPAWNED);
        ModNetworking.sendCameraCue(
                viewer,
                CameraCuePacket.staticShot(0.0D, groundY + 1.15D, 0.5D)
        );
    }

    private StoryNpcEntity createNpc(
            ServerLevel level,
            NpcRole role,
            double x,
            double y,
            float yaw
    ) {
        StoryNpcEntity npc = ModEntities.STORY_NPC.get().create(level);
        if (npc == null) {
            throw new IllegalStateException("Unable to create story NPC");
        }

        npc.moveTo(x, y, 0.5D, yaw, 0.0F);
        npc.setYHeadRot(yaw);
        npc.setYBodyRot(yaw);
        npc.setNpcRole(role);
        npc.setNpcAnimation(NpcAnimation.MAIN);
        npc.setInvulnerable(true);
        return npc;
    }

    private void startApproach(ServerLevel level, StorySavedData data, ServerPlayer viewer) {
        NpcPair pair = resolvePair(level, data);
        if (pair == null) {
            data.reset();
            spawnScene(level, data, viewer);
            return;
        }

        pair.first().setNpcAnimation(NpcAnimation.WALK);
        pair.second().setNpcAnimation(NpcAnimation.WALK);
        stopMovement(pair.first());
        stopMovement(pair.second());
        data.setPhase(StoryPhase.APPROACHING);

        double groundY = sceneGroundY(level);
        ModNetworking.sendCameraCue(
                viewer,
                CameraCuePacket.dolly(0.0D, groundY + 1.15D, 0.5D, 80)
        );
    }

    private void updateApproach(ServerLevel level, StorySavedData data) {
        NpcPair pair = resolvePair(level, data);
        if (pair == null) {
            data.reset();
            clearCamera(level);
            return;
        }

        double groundY = sceneGroundY(level);
        Vec3 firstTarget = new Vec3(-0.85D, groundY, 0.5D);
        Vec3 secondTarget = new Vec3(0.85D, groundY, 0.5D);

        boolean firstArrived = moveToward(pair.first(), firstTarget);
        boolean secondArrived = moveToward(pair.second(), secondTarget);
        if (!firstArrived || !secondArrived) {
            return;
        }

        settleAt(pair.first(), firstTarget);
        settleAt(pair.second(), secondTarget);
        faceEachOther(pair);
        data.setPhase(StoryPhase.READY_FOR_DIALOGUE);
    }

    private boolean moveToward(StoryNpcEntity npc, Vec3 target) {
        Vec3 position = npc.position();
        double deltaX = target.x - position.x;
        double deltaZ = target.z - position.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        stopMovement(npc);
        if (horizontalDistance <= 0.04D) {
            npc.moveTo(target.x, target.y, target.z, npc.getYRot(), 0.0F);
            return true;
        }

        double step = 0.04D / horizontalDistance;
        double nextX = position.x + deltaX * step;
        double nextZ = position.z + deltaZ * step;
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        npc.moveTo(nextX, target.y, nextZ, yaw, 0.0F);
        npc.setYHeadRot(yaw);
        npc.setYBodyRot(yaw);
        return false;
    }

    private void settleAt(StoryNpcEntity npc, Vec3 target) {
        stopMovement(npc);
        npc.moveTo(target.x, target.y, target.z, npc.getYRot(), npc.getXRot());
        npc.setNpcAnimation(NpcAnimation.MAIN);
    }

    private void stopMovement(StoryNpcEntity npc) {
        npc.getNavigation().stop();
        npc.setDeltaMovement(Vec3.ZERO);
    }

    private void advanceDialogue(ServerLevel level, StorySavedData data, ServerPlayer viewer) {
        NpcPair pair = resolvePair(level, data);
        if (pair == null) {
            data.reset();
            ModNetworking.sendCameraCue(viewer, CameraCuePacket.clear());
            return;
        }

        int index = data.getDialogueIndex();
        if (index >= DIALOGUE.size()) {
            data.setPhase(StoryPhase.COMPLETE);
            ModNetworking.sendCameraCue(viewer, CameraCuePacket.clear());
            return;
        }

        DialogueStep step = DIALOGUE.get(index);
        pair.first().setNpcAnimation(NpcAnimation.MAIN);
        pair.second().setNpcAnimation(NpcAnimation.MAIN);

        StoryNpcEntity speaker = step.speaker() == Speaker.FIRST ? pair.first() : pair.second();
        speaker.setNpcAnimation(step.animation());
        faceEachOther(pair);

        int nextIndex = index + 1;
        boolean complete = nextIndex >= DIALOGUE.size();
        Vec3 speakerEyes = speaker.getEyePosition();
        CameraCuePacket cameraCue = complete
                ? CameraCuePacket.turnToAndClear(
                        speakerEyes.x,
                        speakerEyes.y,
                        speakerEyes.z,
                        20
                )
                : CameraCuePacket.turnTo(
                        speakerEyes.x,
                        speakerEyes.y,
                        speakerEyes.z,
                        20
                );
        ModNetworking.sendCameraCue(viewer, cameraCue);
        broadcastDialogue(level, speaker.getNpcRole().displayName(), step.text());

        data.setDialogueIndex(nextIndex);
        data.setPhase(complete ? StoryPhase.COMPLETE : StoryPhase.DIALOGUE);
        dialogueCooldownTicks = 24;
    }

    private void restartScene(ServerLevel level, StorySavedData data, ServerPlayer viewer) {
        resetScene(level, data);
        spawnScene(level, data, viewer);
    }

    private void resetScene(ServerLevel level, StorySavedData data) {
        NpcPair pair = resolvePair(level, data);
        if (pair != null) {
            pair.first().discard();
            pair.second().discard();
        }
        removeNearbyStoryNpcs(level);
        data.reset();
    }

    private NpcPair resolvePair(ServerLevel level, StorySavedData data) {
        if (data.getFirstNpcId() == null || data.getSecondNpcId() == null) {
            return null;
        }

        Entity firstEntity = level.getEntity(data.getFirstNpcId());
        Entity secondEntity = level.getEntity(data.getSecondNpcId());
        if (firstEntity instanceof StoryNpcEntity first && secondEntity instanceof StoryNpcEntity second) {
            return new NpcPair(first, second);
        }
        return null;
    }

    private void faceEachOther(NpcPair pair) {
        pair.first().lookAt(EntityAnchorArgument.Anchor.EYES, pair.second().getEyePosition());
        pair.second().lookAt(EntityAnchorArgument.Anchor.EYES, pair.first().getEyePosition());
        pair.first().setYBodyRot(pair.first().getYRot());
        pair.second().setYBodyRot(pair.second().getYRot());
    }

    private void removeNearbyStoryNpcs(ServerLevel level) {
        AABB sceneBounds = new AABB(
                -16.0D,
                level.getMinBuildHeight(),
                -16.0D,
                16.0D,
                level.getMaxBuildHeight(),
                16.0D
        );
        level.getEntitiesOfClass(StoryNpcEntity.class, sceneBounds).forEach(StoryNpcEntity::discard);
    }

    private void clearCamera(ServerLevel level) {
        level.players().forEach(player -> ModNetworking.sendCameraCue(player, CameraCuePacket.clear()));
    }

    private double sceneGroundY(ServerLevel level) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
    }

    private void broadcastDialogue(ServerLevel level, String speaker, String text) {
        Component message = Component.literal("[" + speaker + "]: " + text);
        level.getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    private enum Speaker {
        FIRST,
        SECOND
    }

    private record DialogueStep(Speaker speaker, NpcAnimation animation, String text) {
    }

    private record NpcPair(StoryNpcEntity first, StoryNpcEntity second) {
    }
}
