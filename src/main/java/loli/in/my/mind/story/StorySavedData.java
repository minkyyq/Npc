package loli.in.my.mind.story;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

public final class StorySavedData extends SavedData {
    private StoryPhase phase = StoryPhase.NOT_STARTED;
    private int dialogueIndex;
    private UUID firstNpcId;
    private UUID secondNpcId;

    public static StorySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                StorySavedData::load,
                StorySavedData::new,
                "npcstory_scene"
        );
    }

    public static StorySavedData load(CompoundTag tag) {
        StorySavedData data = new StorySavedData();
        data.phase = StoryPhase.byId(tag.getInt("Phase"));
        data.dialogueIndex = Math.max(0, tag.getInt("DialogueIndex"));

        if (tag.hasUUID("FirstNpc")) {
            data.firstNpcId = tag.getUUID("FirstNpc");
        }
        if (tag.hasUUID("SecondNpc")) {
            data.secondNpcId = tag.getUUID("SecondNpc");
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Phase", phase.ordinal());
        tag.putInt("DialogueIndex", dialogueIndex);

        if (firstNpcId != null) {
            tag.putUUID("FirstNpc", firstNpcId);
        }
        if (secondNpcId != null) {
            tag.putUUID("SecondNpc", secondNpcId);
        }

        return tag;
    }

    public StoryPhase getPhase() {
        return phase;
    }

    public void setPhase(StoryPhase phase) {
        this.phase = phase;
        setDirty();
    }

    public int getDialogueIndex() {
        return dialogueIndex;
    }

    public void setDialogueIndex(int dialogueIndex) {
        this.dialogueIndex = dialogueIndex;
        setDirty();
    }

    public UUID getFirstNpcId() {
        return firstNpcId;
    }

    public UUID getSecondNpcId() {
        return secondNpcId;
    }

    public void setNpcIds(UUID firstNpcId, UUID secondNpcId) {
        this.firstNpcId = firstNpcId;
        this.secondNpcId = secondNpcId;
        setDirty();
    }

    public void reset() {
        phase = StoryPhase.NOT_STARTED;
        dialogueIndex = 0;
        firstNpcId = null;
        secondNpcId = null;
        setDirty();
    }
}
